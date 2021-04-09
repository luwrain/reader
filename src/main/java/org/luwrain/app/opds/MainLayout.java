/*
   Copyright 2012-2021 Michael Pozhidaev <msp@luwrain.org>

   This file is part of LUWRAIN.

   LUWRAIN is free software; you can redistribute it and/or
   modify it under the terms of the GNU General Public
   License as published by the Free Software Foundation; either
   version 3 of the License, or (at your option) any later version.

   LUWRAIN is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
   General Public License for more details.
*/

package org.luwrain.app.opds;

import java.net.*;
import java.util.*;
import java.io.*;

import org.luwrain.core.*;
import org.luwrain.core.events.*;
import org.luwrain.core.queries.*;
import org.luwrain.controls.*;
import org.luwrain.app.opds.Opds.Link;
import org.luwrain.app.opds.Opds.Entry;
import org.luwrain.app.base.*;

final class MainLayout extends LayoutBase
{
    private final App app;
    final ListArea librariesArea;
    final ListArea listArea;
    final ListArea detailsArea;

    private final LinkedList<HistoryItem> history = new LinkedList<HistoryItem>();

    MainLayout(App app)
    {
	super(app);
	this.app = app;

	final Actions librariesActions;
	{
	    final ListArea.Params librariesParams = new ListArea.Params();
	    librariesParams.context = getControlContext();
	    librariesParams.model = new ListUtils.ListModel(app.libraries);
	    librariesParams.appearance = new ListUtils.DefaultAppearance(getControlContext());
	    librariesParams.clickHandler = (area, index, obj)->onLibraryClick(obj);
	    librariesParams.name = app.getStrings().librariesAreaName();
	    this.librariesArea = new ListArea(librariesParams){
		    @Override public boolean onSystemEvent(SystemEvent event)
		    {
			NullCheck.notNull(event, "event");
			if (event.getType() != SystemEvent.Type.REGULAR)
			    return super.onSystemEvent(event);
			switch(event.getCode())
			{
			case PROPERTIES:
			    return editLibraryProps();
			default:
			    return super.onSystemEvent(event);
			}
		    }
		};
	    librariesActions = actions(
				       action("new-library", "Подключить новую библиотеку", new InputEvent(InputEvent.Special.INSERT), this::actNewLibrary)
				       );
	}

	final Actions listActions;
	{
	    final ListArea.Params params = new ListArea.Params();
	    params.context = getControlContext();
	    params.model = new ListUtils.ListModel(app.entries);
	    params.appearance = new Appearance(getLuwrain(), app.getStrings());
	    //	params.clickHandler = (area, index, obj)->actions.onListClick( listArea, obj);
	    params.name = app.getStrings().itemsAreaName();
	    this.listArea = new ListArea(params);
	    listActions = actions();
	}

	final Actions detailsActions;
	{
	    final ListArea.Params params = new ListArea.Params();
	    params.context = getControlContext();
	    params.model = new ListUtils.FixedModel();
	    params.appearance = new ListUtils.DefaultAppearance(getControlContext(), Suggestions.CLICKABLE_LIST_ITEM);
	    //	params.clickHandler = (area, index, obj)->onClick(obj);
	    params.name = app.getStrings().detailsAreaName();
	    this.detailsArea = new ListArea(params);
	    detailsActions = actions();
	    //detailsArea.setListClickHandler((area, index, obj)->actions.onLinkClick(obj));
	}

	setAreaLayout(AreaLayout.LEFT_TOP_BOTTOM, librariesArea, librariesActions, listArea, listActions, detailsArea, detailsActions);
    }

    private boolean actNewLibrary()
    {
	final String name = app.getConv().newLibraryName();
	if (name == null || name.trim().isEmpty())
	    return true;
	final RemoteLibrary r = new RemoteLibrary();
	r.title = name.trim();
	r.url = "https://";
	app.libraries.add(r);
	app.saveLibraries();
	return true;
    }

    private boolean editLibraryProps()
    {
	final Object obj = librariesArea.selected();
	if (obj == null || !(obj instanceof RemoteLibrary))
	    return false;
	final RemoteLibrary lib = (RemoteLibrary)obj;
	final LibraryPropsLayout propsLayout = new LibraryPropsLayout(app, lib, ()->{
		app.setAreaLayout(MainLayout.this);
		listArea.refresh();
		getLuwrain().announceActiveArea();
	    });
	app.setAreaLayout(propsLayout);
	getLuwrain().announceActiveArea();
	return true;
    }

    private boolean onLibraryClick(Object obj)
    {
	NullCheck.notNull(obj, "obj");
	if (!(obj instanceof RemoteLibrary))
	    return false;
	final RemoteLibrary library = (RemoteLibrary)obj;
	//	base.clearHistory();
	try {
	    return app.open(new URL(library.url));
	}
	catch(MalformedURLException e)
	{
	    app.message(app.getStrings().badUrl(library.url), Luwrain.MessageType.ERROR);
	    return true;
	}
    }

    private boolean onListClick(ListArea listArea, Object obj)
    {
	NullCheck.notNull(listArea, "listArea");
	if (obj == null || !(obj instanceof Opds.Entry))
	    return false;
	final Opds.Entry entry = (Opds.Entry)obj;
	try {
	    onEntry(entry);
	    listArea.refresh();
	    return true;
	}
	catch (MalformedURLException e)
	{
	    app.message(app.getStrings().badUrl(e.getMessage()), Luwrain.MessageType.ERROR);
	    return true;
	}
    }

    private boolean onListProperties(ListArea detailsArea, Object obj)
    {
	NullCheck.notNull(detailsArea, "detailsArea");
	if (obj == null || !(obj instanceof Opds.Entry))
	    return false;
	final Opds.Entry entry = (Opds.Entry)obj;
	final List<PropertiesItem> items = new LinkedList();
	for(Opds.Link l: entry.links)
	{
	    final URL url = prepareUrl(l.url);
	    if (url != null)
		items.add(new PropertiesItem(url.toString(), l.type));
	}
	final ListUtils.FixedModel model = (ListUtils.FixedModel)detailsArea.getListModel();
	model.setItems(items.toArray(new PropertiesItem[items.size()]));
	setActiveArea(detailsArea);
	return true;
    }

    private boolean onLinkClick(Object obj)
    {
	if (obj == null || !(obj instanceof PropertiesItem))
	    return false;
	final PropertiesItem item = (PropertiesItem)obj;
	launchReader(item.url, item.contentType);
	return true;
    }

    private boolean openBook(Entry entry)
    {
	NullCheck.notNull(entry, "entry");
	final Link link = getSuitableBookLink(entry );
	if (link == null)
	    return false;
	launchReader(prepareUrl(link.url).toString(), link.type);
	return true;
    }

    private void onEntry(Opds.Entry entry) throws MalformedURLException
    {
	NullCheck.notNull(entry, "entry");
	if (openBook(entry))
	    return;
	final Opds.Link catalogLink = Utils.getCatalogLink(entry);
	if (catalogLink == null)
	    return;
	if (!history.isEmpty())
	    history.getLast().selected = entry;
	//	openCatalog(app, new URL(getCurrentUrl(), catalogLink.url));
    }

    private Link getSuitableBookLink(Entry entry)
    {
	NullCheck.notNull(entry, "entry");
	for(Link link:entry.links)
	    if (link.type.toLowerCase().equals(Utils.CONTENT_TYPE_FB2_ZIP))
		return link;
	/*
	  final LinkedList<Opds.Link> s = new LinkedList<Opds.Link>();
	  for(Opds.Link link: entry.links)
	  {
	  if (isCatalog(link) || isImage(link))
	  continue;
	  s.add(link);
	  }
	  final Opds.Link[] suitable = s.toArray(new Opds.Link[s.size()]);
	  if (suitable.length == 1)
	  {
	  openReader(new URL(getCurrentUrl(), suitable[0].url.toString()), suitable[0].type);
	  return;
	  }
	  for(Opds.Link link: suitable)
	  if (link.type.equals("application/fb2+zip") ||
	  link.type.equals("application/fb2"))
	  {
	  openReader(new URL(getCurrentUrl(), link.url.toString()), link.type);
	  return;
	  }
	  luwrain.message(strings.noSuitableLinksInEntry(), Luwrain.MESSAGE_ERROR);
	*/
	return null;
    }

    private URL prepareUrl(String href)
    {
	final URL currentUrl = getCurrentUrl();
	NullCheck.notNull(currentUrl, "currentUrl");
	try {
	    return new URL(currentUrl, href);
	}
	catch(MalformedURLException e)
	{
	    return null;
	}
    }

    private void launchReader(String url, String contentType)
    {
	NullCheck.notEmpty(url, "url");
	NullCheck.notNull(contentType, "contentType");
	getLuwrain().launchApp("reader", new String[]{url, contentType});
    }

    private URL getCurrentUrl()
    {
	return !history.isEmpty()?history.getLast().url:null;
    }

    private Opds.Entry returnBack()
    {
	if (history.size() <= 1)
	    return null;
	history.pollLast();
	//	model.setItems(history.getLast().entries);
	return history.getLast().selected;
    }
}

	/*
						case AreaQuery.UNIREF_HOT_POINT:
			    {
				final Object obj = selected();
				if (obj == null || !(obj instanceof Entry))
				    return false;
				final Entry entry = (Entry)obj;
				final Link link = base.getSuitableBookLink(entry);
				if (link == null)
				    return false;
				final UniRefHotPointQuery unirefQuery = (UniRefHotPointQuery)query;
				unirefQuery.answer("url:" + base.prepareUrl(link.url).toString());
				return true;
*/


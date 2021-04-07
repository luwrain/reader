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
	//	librariesParams.clickHandler = (area, index, obj)->actions.onLibraryClick(listArea, obj);
		librariesParams.name = app.getStrings().librariesAreaName();
	this.librariesArea = new ListArea(librariesParams);
	librariesActions = actions();
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

    private boolean onLibraryClick(Object obj)
    {
	if (obj == null || !(obj instanceof RemoteLibrary))
	    return false;
	final RemoteLibrary library = (RemoteLibrary)obj;
	//	base.clearHistory();
		try {
	app.open(new URL(library.url));
	//	    luwrain.setActiveArea(listArea);
	    return true;
	}
	catch(MalformedURLException e)
	{
	    app.message(app.getStrings().badUrl(library.url), Luwrain.MessageType.ERROR);
	    return true;
	}
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


/*
   Copyright 2012-2018 Michael Pozhidaev <michael.pozhidaev@gmail.com>

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

import java.util.*;
import java.net.*;
import java.util.concurrent.*;
import javax.activation.*;

import org.luwrain.core.*;
import org.luwrain.controls.*;
import org.luwrain.app.opds.Opds.Link;
import org.luwrain.app.opds.Opds.Entry;

final class Base
{
    static private final String CONTENT_TYPE_FB2_ZIP = org.luwrain.doctree.loading.UrlLoader.CONTENT_TYPE_FB2_ZIP;

    static final String PROFILE_CATALOG = "opds-catalog";
    static final String BASE_TYPE_CATALOG = "application/atom+xml";
    static final String PRIMARY_TYPE_IMAGE = "image";

    private final Luwrain luwrain;
    private final Strings strings;
    private FutureTask task = null;
    private final RemoteLibrary[] libraries;
    private final ListUtils.FixedModel model = new ListUtils.FixedModel();
    private final LinkedList<HistoryItem> history = new LinkedList<HistoryItem>();

    Base(Luwrain luwrain, Strings strings)
    {
	NullCheck.notNull(luwrain, "luwrain");
	NullCheck.notNull(strings, "strings");
	this.luwrain = luwrain;
	this.strings = strings;
	this.libraries = loadLibraries();
    }

    boolean openCatalog(App app, URL url)
    {
	NullCheck.notNull(app, "app");
	NullCheck.notNull(url, "url");
	if (isBusy())
	    return false;
	this.task = new FutureTask<Opds.Result>(()->{
		final Opds.Result res = Opds.fetch(url);
		luwrain.runUiSafely(()->onFetchResult(url, app, res));
	    }, null);
	luwrain.executeBkg(task);
	model.clear();
	app.updateAreas();
	return true;
    }

    Opds.Entry returnBack()
    {
	if (isBusy())
	    return null;
	if (history.size() <= 1)
	    return null;
	history.pollLast();
	model.setItems(history.getLast().entries);
	return history.getLast().selected;
    }

    private void onFetchResult(URL url, App app, Opds.Result res)
    {
	NullCheck.notNull(url, "url");
	NullCheck.notNull(app, "app");
	NullCheck.notNull(res, "res");
	this.task = null;
	app.updateAreas();
	if (res.error == Opds.Result.Errors.FETCHING_PROBLEM)
	{
	    luwrain.message("Невозможно подключиться к серверу или данные по указанному адресу не являются правильным OPDS-каталогом", Luwrain.MessageType.ERROR);//FIXME:
	    return;
	}
	if(res.hasEntries())
	{
	    model.setItems(res.getEntries());
	    history.add(new HistoryItem(url, res.getEntries()));
	    luwrain.playSound(Sounds.CLICK);
	}
    }

    void clearHistory()
    {
	history.clear();
    }

    private RemoteLibrary[] loadLibraries()
    {
	final Registry registry = luwrain.getRegistry();
	registry.addDirectory(Settings.LIBRARIES_PATH);
	final List<RemoteLibrary> res = new LinkedList();
	for(String s: registry.getDirectories(Settings.LIBRARIES_PATH))
	{
	    final RemoteLibrary l = new RemoteLibrary(registry, Registry.join(Settings.LIBRARIES_PATH, s));
	    if (!l.url.isEmpty())
		res.add(l);
	}
	final RemoteLibrary[] libraries = res.toArray(new RemoteLibrary[res.size()]);
	Arrays.sort(libraries);
	return libraries;
    }

    boolean isBusy()
    {
	return task != null && !task.isDone();
    }

    URL getCurrentUrl()
    {
	return !history.isEmpty()?history.getLast().url:null;
    }

    void onEntry(App app, Opds.Entry entry) throws MalformedURLException
    {
	NullCheck.notNull(app, "app");
	NullCheck.notNull(entry, "entry");
	if (openBook(entry))
	    return;
	final Opds.Link catalogLink = getCatalogLink(entry);
	if (catalogLink == null)
	    return;
	if (!history.isEmpty())
	    history.getLast().selected = entry;
	openCatalog(app, new URL(getCurrentUrl(), catalogLink.url));
    }

    private boolean openBook(Entry entry)
    {
	NullCheck.notNull(entry, "entry");
	for(Link link:entry.links)
	{
	    if (link.type.toLowerCase().equals(CONTENT_TYPE_FB2_ZIP))
	    {
		launchReader(prepareUrl(link.url).toString(), link.type);
		return true;
	    }
	}
	return false;
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
    }

    void launchReader(String url, String contentType)
    {
	NullCheck.notEmpty(url, "url");
	NullCheck.notNull(contentType, "contentType");
	luwrain.launchApp("reader", new String[]{url, contentType});
    }

    URL prepareUrl(String href)
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

    ListArea.Model getModel()
    {
	return model;
    }

    static Link getCatalogLink(Entry entry)
    {
	NullCheck.notNull(entry, "entry");
	for(Link link: entry.links)
	    if (isCatalog(link))
		return link;
	return null;
    }

    static boolean isCatalogOnly(Entry entry)
    {
	NullCheck.notNull(entry, "entry");
	for(Link link: entry.links)
	    if (!isCatalog(link))
		return false;
	return true;
    }

    static boolean hasCatalogLinks(Entry entry)
    {
	NullCheck.notNull(entry, "entry");
	for(Link link: entry.links)
	    if (isCatalog(link))
		return true;
	return false;
    }

    static boolean hasBooks(Entry entry)
    {
	NullCheck.notNull(entry, "entry");
	for(Link link: entry.links)
	    if (!isCatalog(link) && !isImage(link))
		return true;
	return false;
    }

    static boolean isCatalog(Link link)
    {
	NullCheck.notNull(link, "link");
	if (getTypeProfile(link).toLowerCase().equals(PROFILE_CATALOG))
	    return true;
	return getBaseType(link).equals(BASE_TYPE_CATALOG);
    }

    static boolean isImage(Link link)
    {
	NullCheck.notNull(link, "link");
	return getPrimaryType(link).toLowerCase().trim().equals(PRIMARY_TYPE_IMAGE);
    }

    //Never returns null
    static String getBaseType(Link link)
    {
	NullCheck.notNull(link, "link");
	if (link.type == null)
	    return "";
	try {
	    final MimeType mime = new MimeType(link.type);
	    final String value = mime.getBaseType();
	    return value != null?value:"";
	}
	catch(MimeTypeParseException e)
	{
	    e.printStackTrace();
	    return "";
	}
    }

    //Never returns null
    static String getPrimaryType(Link link)
    {
	NullCheck.notNull(link, "link");
	if (link.type == null)
	    return "";
	try {
	    final MimeType mime = new MimeType(link.type);
	    final String value = mime.getPrimaryType();
	    return value != null?value:"";
	}
	catch(MimeTypeParseException e)
	{
	    e.printStackTrace();
	    return "";
	}
    }

    //Never returns null
    static String getSubType(Link link)
    {
	NullCheck.notNull(link, "link");
	if (link.type == null)
	    return "";
	try {
	    final MimeType mime = new MimeType(link.type);
	    final String value = mime.getSubType();
	    return value != null?value:"";
	}
	catch(MimeTypeParseException e)
	{
	    e.printStackTrace();
	    return "";
	}
    }

    //Never returns null
    static String getTypeProfile(Link link)
    {
	NullCheck.notNull(link, "link");
	if (link.type == null)
	    return "";
	try {
	    final MimeType mime = new MimeType(link.type);
	    final String value = mime.getParameter("profile");
	    return value != null?value:"";
	}
	catch(MimeTypeParseException e)
	{
	    e.printStackTrace();
	    return "";
	}
    }

    private final class LibrariesModel implements ListArea.Model
    {
	@Override public int getItemCount()
	{
	    return libraries.length;
	}
	@Override public Object getItem(int index)
	{
	    return libraries[index];
	}
	@Override public void refresh()
	{
	}
    }
    ListArea.Model getLibrariesModel()
    {
	return new LibrariesModel();
    }
}

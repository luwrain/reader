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
    static private final String CONTENT_TYPE_FB2_ZIP = "application/fb2+zip";

    static final String PROFILE_CATALOG = "opds-catalog";
    static final String BASE_TYPE_CATALOG = "application/atom+xml";
    static final String PRIMARY_TYPE_IMAGE = "image";

    private final Luwrain luwrain = null;
    private final Strings strings = null;
    private FutureTask task = null;
    private final RemoteLibrary[] libraries = null;

    private final LinkedList<HistoryItem> history = new LinkedList<HistoryItem>();

    Opds.Entry returnBack()
    {
	if (isBusy())
	    return null;
	if (history.size() <= 1)
	    return null;
	history.pollLast();
	//	model.setItems(history.getLast().entries);
	return history.getLast().selected;
    }

    void clearHistory()
    {
	history.clear();
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
	//	openCatalog(app, new URL(getCurrentUrl(), catalogLink.url));
    }

    Link getSuitableBookLink(Entry entry)
    {
	NullCheck.notNull(entry, "entry");
	for(Link link:entry.links)
	    if (link.type.toLowerCase().equals(CONTENT_TYPE_FB2_ZIP))
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

    private boolean openBook(Entry entry)
    {
	NullCheck.notNull(entry, "entry");
	final Link link = getSuitableBookLink(entry );
	if (link == null)
	    return false;
		launchReader(prepareUrl(link.url).toString(), link.type);
		return true;
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

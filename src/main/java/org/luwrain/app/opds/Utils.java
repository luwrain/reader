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
import javax.activation.*;


import org.luwrain.core.*;
import org.luwrain.core.events.*;
import org.luwrain.core.queries.*;
import org.luwrain.controls.*;
import org.luwrain.app.opds.Opds.Link;
import org.luwrain.app.opds.Opds.Entry;
import org.luwrain.app.base.*;

final class Utils
{
    static final String
	CONTENT_TYPE_FB2_ZIP = "application/fb2+zip",
	PROFILE_CATALOG = "opds-catalog",
	BASE_TYPE_CATALOG = "application/atom+xml",
	PRIMARY_TYPE_IMAGE = "image";

    


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
}

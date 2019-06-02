/*
   Copyright 2012-2019 Michael Pozhidaev <msp@luwrain.org>
   Copyright 2015-2016 Roman Volovodov <gr.rPman@gmail.com>

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

package org.luwrain.app.reader;

import java.util.*;

import org.luwrain.core.*;
import org.luwrain.util.*;

final class StoredProperties
{
    private final Registry registry;
    private final String path;
    private final Settings.Props sett;
    private final Properties props;

    StoredProperties(Registry registry, String url)
    {
	NullCheck.notNull(registry, "registry");
	NullCheck.notEmpty(url, "url");
	this.registry = registry;
	this.path = Registry.join(Settings.PROPERTIES_PATH, Sha1.getSha1(url, "UTF-8"));
	registry.addDirectory(this.path);
	this.sett = Settings.createProperties(registry, path);
	this.props = Settings.decodeProperties(sett.getProps(""));
    }

    static boolean hasProperties(Registry registry, String url)
    {
	NullCheck.notNull(registry, "registry");
	NullCheck.notEmpty(url, "url");
	return registry.hasDirectory(Registry.join(Settings.PROPERTIES_PATH, Sha1.getSha1(url, "UTF-8")));
    }

    String getCharset()
    {
	final String res = props.getProperty("charset");
	return res != null?res:"";
    }

    void setCharset(String charset)
    {
	NullCheck.notNull(charset, "charset");
	this.props.setProperty("charset", charset);
	save();
    }

    String getParaStyle()
    {
	final String res = props.getProperty("para-style");
	return res != null?res:"";
    }

    void setParaStyle(String paraStyle)
    {
	NullCheck.notNull(paraStyle, "paraStyle");
	this.props.setProperty("para-style", paraStyle);
	save();
    }

    int getBookmarkPos()
    {
	final String res = props.getProperty("bookmark-pos");
	if (res == null || res.trim().isEmpty())
	    return -1;
	try {
	    return Integer.parseInt(res);
	}
	catch(NumberFormatException e)
	{
	    return -1;
	}
    }

    void setBookmarkPos(int pos)
    {
	if (pos < 0)
	    throw new IllegalArgumentException("pos (" + pos + ") may not be negative");
	props.setProperty("bookmark-pos", String.valueOf(pos));
	save();
    }

    private void save()
    {
	this.sett.setProps(Settings.encodeProperties(props));
    }
}

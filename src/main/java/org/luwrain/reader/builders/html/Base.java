/*
   Copyright 2012-2019 Michael Pozhidaev <michael.pozhidaev@gmail.com>
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

package org.luwrain.reader.builders.html;

import java.util.*;

import org.jsoup.nodes.*;

import org.luwrain.core.*;
import org.luwrain.reader.ExtraInfo;

class Base
{
    static final String LOG_COMPONENT = "reader";

    private final LinkedList<ExtraInfo> extraInfoStack = new LinkedList<ExtraInfo>();

    protected void addExtraInfo(Element el)
    {
	NullCheck.notNull(el, "el");
	final ExtraInfo info = new ExtraInfo();
	info.name = el.nodeName();
	final Attributes attrs = el.attributes();
	if (attrs != null)
	    for(Attribute a: attrs.asList())
	    {
		final String key = a.getKey();
		final String value = a.getValue();
		if (key != null && !key.isEmpty() && value != null)
		    info.attrs.put(key, value);
	    }
	if (!extraInfoStack.isEmpty())
	    info.parent = extraInfoStack.getLast(); else
	    info.parent = null;
	extraInfoStack.add(info);
    }

    protected void releaseExtraInfo()
    {
	if (!extraInfoStack.isEmpty())
	    extraInfoStack.pollLast();
    }

    protected ExtraInfo getCurrentExtraInfo()
    {
	return extraInfoStack.isEmpty()?null:extraInfoStack.getLast();
    }

    static protected void collectMeta(Element el, Map<String, String> meta)
    {
	NullCheck.notNull(el, "el");
	NullCheck.notNull(meta, "meta");
	if (el.nodeName().equals("meta"))
	{
	    final String name = el.attr("name");
	    final String content = el.attr("content");
	    if (name != null && !name.isEmpty() && content != null)
		meta.put(name, content);
	}
	if (el.childNodes() != null)
	    for(Node n: el.childNodes())
		if (n instanceof Element)
		    collectMeta((Element)n, meta);
    }
}

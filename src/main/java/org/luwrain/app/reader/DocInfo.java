/*
   Copyright 2012-2015 Michael Pozhidaev <michael.pozhidaev@gmail.com>

   This file is part of the LUWRAIN.

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
import java.net.*;
import org.luwrain.doctree.Factory;

public class DocInfo
{
static public final int LOCAL = 1;;
    static public final int URL = 2;

    static final String DEFAULT_CHARSET = "UTF-8";
    static final int DEFAULT_FORMAT = Factory.TEXT_PARA_INDENT;

    int type = LOCAL;
    String fileName = null;
    int format = Factory.UNRECOGNIZED;;
    String charset = DEFAULT_CHARSET;
    final LinkedList<URL> history = new LinkedList<URL>();

    static int formatByStr(String str)
    {
	if (str == null || str.trim().isEmpty())
	    return Factory.UNRECOGNIZED;
	switch(str.trim())
	{
	case "text-para-indent":
	    return Factory.TEXT_PARA_INDENT;
	case "text-para-empty-line":
	    return Factory.TEXT_PARA_EMPTY_LINE;
	case "text-para-each-line":
	    return Factory.TEXT_PARA_EACH_LINE;
	case "html":
	    return Factory.HTML;
	case "doc":
	    return Factory.DOC;
	case "docx":
	    return Factory.DOCX;
	default:
	    return Factory.UNRECOGNIZED;
	    }
    }
}

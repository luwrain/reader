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
import java.nio.file.*;
import java.net.*;

import org.luwrain.core.NullCheck;

public class DocInfo
{
    enum Type {PATH, URL};

    Type type = Type.PATH;
    Path path = null;
    URL url = null;
    String contentType = "";
    String charset = "";
    final LinkedList<URL> history = new LinkedList<URL>();

    public DocInfo()
    {
    }

    DocInfo(URL url)
    {
	NullCheck.notNull(url, "url");
	type = Type.URL;
	this.url = url;
    }

    DocInfo(Path path)
    {
	NullCheck.notNull(path, "path");
	type = Type.PATH;
	this.path = path;
    }

    public boolean load(String[] args)
    {
	//	System.out.println("here");
	NullCheck.notNullItems(args, "args");
	if (args.length < 1)
	    return false;
	if (args.length == 1)
	{
	    final String value = args[0];
	    if (!value.toLowerCase().startsWith("http://") && !value.toLowerCase().startsWith("https://"))
	    {
		type = Type.PATH;
		path = Paths.get(value);
		return true;
	    }
	    type = Type.URL;
	    try {
		url = new URL(value);
		return true;
	    }
	    catch(MalformedURLException e)
	    {
		e.printStackTrace();
		return false;
	    }
	}
	String value = null;
	//	System.out.println("here");
	for(int i = 0;i < args.length;++i)
	    switch(args[i])
	    {
	    case "--URL":
		type = Type.URL;
		value = takeNext(args, i);
		if (value == null)
		    return false;
		++i;
		//		System.out.println("yurl");
		break;
	    case "--PATH":
		type = Type.PATH;
		value = takeNext(args, i);
		if (value == null)
		    return false;
		++i;
		break;
	    case "--TYPE":
		contentType = takeNext(args, i);
		if (contentType == null)
		    return false;
		++i;
		break;
	    case "--CHARSET":
		charset = takeNext(args, i);
		if (charset == null)
		    return false;
		++i;
		break;
	    default:
		return false;
	    }
	if (value == null)
	    return false;
	//	System.out.println("value=" + value);
	switch(type)
	{
	case PATH:
	    path = Paths.get(value);
	    return true;
	case URL:
	    try {
		url = new URL(value);
		return true;
	    }
	    catch(MalformedURLException e)
	    {
		e.printStackTrace();
		return false;
	    }
	default:
	    return false;
	}
    }

    static private String takeNext(String[] args, int index)
    {
	if (index < 0 || index + 1>= args.length)
	    return null;
	return args[index + 1];
    }
}

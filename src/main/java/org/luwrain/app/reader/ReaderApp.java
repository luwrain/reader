/*
   Copyright 2012-2015 Michael Pozhidaev <michael.pozhidaev@gmail.com>

   This file is part of the Luwrain.

   Luwrain is free software; you can redistribute it and/or
   modify it under the terms of the GNU General Public
   License as published by the Free Software Foundation; either
   version 3 of the License, or (at your option) any later version.

   Luwrain is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
   General Public License for more details.
*/

package org.luwrain.app.reader;

import java.net.*;

import org.luwrain.core.*;
import org.luwrain.app.reader.doctree.Document;
import org.luwrain.app.reader.filters.Filters;

public class ReaderApp implements Application, Actions
{
    public static final String STRINGS_NAME = "luwrain.reader";

    public static final int LOCAL = 1;;
    public static final int URL = 2;

    private Luwrain luwrain;
    private Strings strings;
    private ReaderArea area;
    private Document doc;
    private Filters filters = new Filters();

    private String arg;
    private int argType;

    public ReaderApp()
    {
	arg = null;
	argType = LOCAL;
    }

    public ReaderApp(int type, String arg)
    {
	this.argType = type;
	this.arg = arg;
	if (arg == null)
	    throw new NullPointerException("arg may not be null");
	if (type != LOCAL && type != URL)
	    throw new IllegalArgumentException("type must be either ReaderApp.LOCAL or ReaderApp.URL");
    }

    @Override public boolean onLaunch(Luwrain luwrain)
    {
	Object o = luwrain.i18n().getStrings("luwrain.reader");
	if (o == null || !(o instanceof Strings))
	    return false;
	strings = (Strings)o;
	this.luwrain = luwrain;
	/*
	if (arg != null)
 	{
	    doc = filters.readFromFile(arg, Filters.HTML);
	    if (doc == null)
	    {
		luwrain.message(strings.errorOpeningFile(), Luwrain.MESSAGE_ERROR);
		return false;
	    }
 	}
	*/
	area = new ReaderArea(luwrain, strings, this, filters, doc);
	    if (argType == URL && arg != null)
	try {
	    new Thread(new FetchThread(luwrain, area, new URL(arg))).start();
	}
	catch (MalformedURLException e)
	{
	    e.printStackTrace();
	}
	    return true;
    }

    @Override public String getAppName()
    {
	return "Reader";//FIXME:
    }

    @Override public AreaLayout getAreasToShow()
    {
	return new AreaLayout(area);
    }

    @Override public void closeApp()
    {
	luwrain.closeApp();
    }
}

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

import org.luwrain.core.*;
import org.luwrain.app.reader.doctree.Document;
import org.luwrain.app.reader.filters.Filters;

public class ReaderApp implements Application, Actions
{
    public static final String STRINGS_NAME = "luwrain.reader";

    private Luwrain luwrain;
    private Strings strings;
    private ReaderArea area;
    private Document doc;

    private String arg = null;

    public ReaderApp()
    {
    }

    public ReaderApp(String arg)
    {
	this.arg = arg;
	if (arg == null)
	    throw new NullPointerException("arg may not be null");
    }

    @Override public boolean onLaunch(Luwrain luwrain)
    {
	if (arg != null)
	    System.out.println("reader:launching for " + arg); else
	    System.out.println("reader:launching without arg");
	Object o = luwrain.i18n().getStrings("luwrain.reader");
	if (o == null || !(o instanceof Strings))
	    return false;
	strings = (Strings)o;
	this.luwrain = luwrain;
	if (arg != null)
	{
	    System.out.println("reader:launching filter");
	    doc = Filters.read(arg, Filters.DOC);
	    if (doc == null)
	    {
		System.out.println("reader:filter rejected");
		luwrain.message(strings.errorOpeningFile(), Luwrain.MESSAGE_ERROR);
		return false;
	    }
	}
	area = new ReaderArea(luwrain, strings, this, doc);
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

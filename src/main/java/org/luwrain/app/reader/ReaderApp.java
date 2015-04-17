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

public class ReaderApp implements Application, Actions
{
    private Luwrain luwrain;
    private Strings strings;
    private PreviewArea area;
    private String arg;

    public ReaderApp()
    {
    }

    public ReaderApp(String arg)
    {
	this.arg = arg;
    }

    @Override public boolean onLaunch(Luwrain luwrain)
    {
	Object o = luwrain.i18n().getStrings("luwrain.reader");
	if (o == null || !(o instanceof Strings))
	    return false;
	strings = (Strings)o;
	this.luwrain = luwrain;
	area = new PreviewArea(luwrain, strings, this);
	if (arg != null)
	    if (!handleToPreview(arg))
	    {
		luwrain.message(strings.errorOpeningFile());
		return false;
	    }
	return true;
    }

    @Override public String getAppName()
    {
	return "Reader";//FIXME:
    }

    private boolean handleToPreview(String fileName)
    {
	Filter f = new FilterPoi();
	try {
	f.open(fileName);
	}
	catch(Exception e)
	{
	    e.printStackTrace();
	    Log.error("preview", fileName + ":" + e.getMessage());
	    return false;
	}
	area.setFilter(f);
	return true;
    }

    public AreaLayout getAreasToShow()
    {
	return new AreaLayout(area);
    }

    @Override public void closeApp()
    {
	luwrain.closeApp();
    }
}

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

//TODO:Refresh;

import java.io.File;
import org.luwrain.core.*;
import org.luwrain.core.events.*;
import org.luwrain.controls.*;

public class PreviewArea extends NavigateArea
{
    private Luwrain luwrain;
    private Strings strings;
    private Actions actions;
    private Filter filter;

    public PreviewArea(Luwrain luwrain,
		       Strings strings,
		       Actions actions)
    {
	super(new DefaultControlEnvironment(luwrain));
	this.luwrain = luwrain;
	this.strings = strings;
	this.actions = actions;
    }

    public void setFilter(Filter filter)
    {
	if (filter == null)
	    return;
	this.filter = filter;
	setHotPoint(0, 0);
	luwrain.onAreaNewContent(this);
	luwrain.onAreaNewName(this);
    }

    public int getLineCount()
    {
	if (filter == null)
	    return 1;
	final int count = filter.getLineCount();
	return count >= 1?count:0;
    }

    public String getLine(int index)
    {
	if (filter == null)
	    return "";
	final String value = filter.getLine(index);
	return value != null?value:"";
    }

    public boolean onEnvironmentEvent(EnvironmentEvent event)
    {
	switch(event.getCode())
	{
	case EnvironmentEvent.CLOSE:
	    actions.closeApp();
	    return true;
	case EnvironmentEvent.INTRODUCE:
	    if (filter != null)
		luwrain.say(strings.appName() + " " + getFileName()); else
		luwrain.say(strings.appName());
	    return true;
	default:
	    return false;
	}
    }

    public String getName()
    {
	if (filter == null)
	    return strings.appName();
	return getFileName();
    }

    private String getFileName()
    {
	if (filter == null)
	    return "";
	File f = new File(filter.getFileName());
	return f.getName();
    }
}

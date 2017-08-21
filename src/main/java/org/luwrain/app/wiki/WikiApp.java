/*
   Copyright 2012-2017 Michael Pozhidaev <michael.pozhidaev@gmail.com>

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

package org.luwrain.app.wiki;

import java.net.*;
import java.util.*;
import java.io.*;

import org.luwrain.core.*;
import org.luwrain.core.events.*;
import org.luwrain.controls.*;
import org.luwrain.popups.*;

public class WikiApp implements Application
{
    private Luwrain luwrain = null;
    private Base base = null;
    private Strings strings = null;
    private ConsoleArea2 area;
    //    private HashSet<String> values = new HashSet<String>();

    private final String launchArg;

    public WikiApp()
    {
	launchArg = null;
    }

    public WikiApp(String launchArg)
    {
	NullCheck.notNull(launchArg, "launchArg");
	this.launchArg = launchArg;
    }

    @Override public InitResult onLaunchApp(Luwrain luwrain)
    {
	NullCheck.notNull(luwrain, "luwrain");
	final Object o = luwrain.i18n().getStrings(Strings.NAME);
	if (o == null || !(o instanceof Strings))
	    return new InitResult(InitResult.Type.NO_STRINGS_OBJ, Strings.NAME);
	strings = (Strings)o;

	this.luwrain = luwrain;
	this.base = new Base(luwrain, strings);
	createArea();
	//	if (launchArg != null && !launchArg.trim().isEmpty())
	//	    base.search(luwrain.getProperty("luwrain.lang"), launchArg, this);
	return new InitResult();
    }

    private void createArea()
    {
	final ConsoleArea2.Params params = new ConsoleArea2.Params();
	params.context = new DefaultControlEnvironment(luwrain);
	params.model = base.getModel();
	params.appearance = base.getAppearance();
	params.areaName = strings.appName();
	area = new ConsoleArea2(params){
		@Override public boolean onKeyboardEvent(KeyboardEvent event)
		{
		    NullCheck.notNull(event, "event");
		    if (event.isSpecial() && !event.isModified())
			switch(event.getSpecial())
		    {
		    case ESCAPE:
			closeApp();
			return true;
		    }
		    return super.onKeyboardEvent(event);
		}
		@Override public boolean onEnvironmentEvent(EnvironmentEvent event)
		{
		    NullCheck.notNull(event, "event");
		    if (event.getType() != EnvironmentEvent.Type.REGULAR)
			return super.onEnvironmentEvent(event);
		    switch(event.getCode())
		    {
		    case CLOSE:
closeApp();
			return true;
		    default:
			return super.onEnvironmentEvent(event);
		    }
		}
	    };
	area.setConsoleClickHandler((area,index,obj)->{
		//FIXME:
    /*
	if (obj == null || !(obj instanceof Page))
	    return false;
	final Page page = (Page)obj;
	try {
	    final String url = "https://" + URLEncoder.encode(page.lang) + ".wikipedia.org/wiki/" + URLEncoder.encode(page.title, "UTF-8").replaceAll("\\+", "%20");//Completely unclear why wikipedia doesn't recognize '+' sign
	    luwrain.launchApp("reader", new String[]{url});
	}
	catch (UnsupportedEncodingException e)
	{
	    e.printStackTrace();
	    luwrain.message(e.getMessage(), Luwrain.MESSAGE_ERROR);
	}
	return true;
    */
		return false;
	    });
	area.setConsoleInputHandler((text)->{
	    NullCheck.notNull(text, "text");
	    if (text.trim().isEmpty() || base.isBusy())
return false;
	    base.search("ru", text.trim(), area);
	    return true;
	    });
    }

    @Override public String getAppName()
    {
	return strings.appName();
    }

    @Override public AreaLayout getAreaLayout()
    {
	return new AreaLayout(area);
    }

    @Override public void closeApp()
    {
	luwrain.closeApp();
    }
}

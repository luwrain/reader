/*
   Copyright 2012-2016 Michael Pozhidaev <michael.pozhidaev@gmail.com>

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

package org.luwrain.app.narrator;

import java.net.*;
import java.util.*;
import java.io.*;

import org.luwrain.core.*;
import org.luwrain.core.events.*;
import org.luwrain.controls.*;
import org.luwrain.popups.Popups;

public class NarratorApp implements Application, Actions
{
    static private final String STRINGS_NAME = "luwrain.narrator";

    private final Base base = new Base();
    private Luwrain luwrain;
    private Strings strings;
    private EditArea editArea;
    private SimpleArea progressArea;
    private AreaLayoutSwitch areaLayoutSwitch;

    private String initialText = null;

    public NarratorApp()
    {
    }

    public NarratorApp(String text)
    {
	NullCheck.notNull(text, "text");
	this.initialText = text;
    }

    @Override public boolean onLaunch(Luwrain luwrain)
    {
	Object o = luwrain.i18n().getStrings(STRINGS_NAME);
	if (o == null || !(o instanceof Strings))
	    return false;
	strings = (Strings)o;
	this.luwrain = luwrain;
	areaLayoutSwitch = new AreaLayoutSwitch(luwrain);
	if (!base.init(luwrain, strings))
	    return false;
	createAreas();
	return true;
    }

    @Override public void start()
    {
	if (!base.start(progressArea, editArea.getWholeText()))
	return;//FIXME:Some error message
	areaLayoutSwitch.show(1);
    }

    private void createAreas()
    {
	final Actions a = this;
	final Strings s = strings;

	editArea = new EditArea(new DefaultControlEnvironment(luwrain), strings.appName(), 
				initialText != null?initialText.split("\n", -1):new String[0], null){
		@Override public boolean onEnvironmentEvent(EnvironmentEvent event)
		{
		    NullCheck.notNull(event, "event");
		    switch(event.getCode())
		    {
		    case CLOSE:
			a.closeApp();
			return true;
		    case OK:
			a.start();
			return true;
		    default:
			return super.onEnvironmentEvent(event);
		    }
		}
	    };

	progressArea = new SimpleArea(new DefaultControlEnvironment(luwrain), strings.appName()){
		@Override public boolean onEnvironmentEvent(EnvironmentEvent event)
		{
		    NullCheck.notNull(event, "event");
		    if (event instanceof ProgressLineEvent)
		    {
			onProgressLine(((ProgressLineEvent)event).progressLine());
			return true;
		    }
		    switch(event.getCode())
		    {
		    case CLOSE:
			a.closeApp();
			return true;
		    default:
			return super.onEnvironmentEvent(event);
		    }
		}
		private void onProgressLine(String line)
		{
		    if (line == null)
			return;
		    addLine(line);
		}
	    };

	areaLayoutSwitch.add(new AreaLayout(editArea));
	areaLayoutSwitch.add(new AreaLayout(progressArea));
    }

    @Override public String getAppName()
    {
	return strings.appName();
    }

    @Override public AreaLayout getAreasToShow()
    {
	return areaLayoutSwitch.getCurrentLayout(); 
    }

    @Override public boolean closeApp()
    {
	luwrain.closeApp();
	return true;
    }
}

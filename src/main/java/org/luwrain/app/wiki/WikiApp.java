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

package org.luwrain.app.wiki;

import java.net.*;

import org.luwrain.core.*;
import org.luwrain.core.events.*;
import org.luwrain.controls.*;

public class WikiApp implements Application, Actions
{
    public static final String STRINGS_NAME = "luwrain.wiki";

    private Luwrain luwrain;
    private Strings strings;
    private FetchThread thread;
    private ListArea area;
    private Model model;
    private Appearance appearance;

    @Override public boolean onLaunch(Luwrain luwrain)
    {
	Object o = luwrain.i18n().getStrings(STRINGS_NAME);
	if (o == null || !(o instanceof Strings))
	    return false;
	strings = (Strings)o;
	this.luwrain = luwrain;
	createArea();

	thread = new FetchThread(luwrain, area, "");
	new Thread(thread).start();


	return true;
    }

    private void createArea()
    {
	final Actions a = this;
	final Strings s = strings;

	model = new Model();
	appearance = new Appearance(luwrain, strings);

	final ListClickHandler handler = new ListClickHandler(){
		private Actions actions = a;
		@Override public boolean onListClick(ListArea area,
						     int index,
						     Object item)
		{
		    //FIXME:
		    return false;
		}
	    };

	area = new ListArea(new DefaultControlEnvironment(luwrain),
			    model, appearance,
			    handler, strings.appName()) {
		private Strings strings = s;
		      private Actions actions = a;
		@Override public boolean onKeyboardEvent(KeyboardEvent event)
		{
		    if (event == null)
			throw new NullPointerException("event may not be null");
		    return super.onKeyboardEvent(event);
		}
		@Override public boolean onEnvironmentEvent(EnvironmentEvent event)
		{
		    if (event == null)
			throw new NullPointerException("event may not be null");
		    switch(event.getCode())
		    {
		    case EnvironmentEvent.CLOSE:
			actions.closeApp();
			return true;
		    case EnvironmentEvent.THREAD_SYNC:
			if (event instanceof FetchEvent)
			{
			    final FetchEvent fetchEvent = (FetchEvent)event;
			    actions.showQueryRes(fetchEvent.pages());
			    return true;
			} else
			    return false;
		    }
		    return super.onEnvironmentEvent(event);
		}
	    };
    }

    @Override public void showQueryRes(Page[] pages)
    {
	model.setPages(pages);
	area.refresh();
	luwrain.message(strings.querySuccess(pages.length), Luwrain.MESSAGE_OK);
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

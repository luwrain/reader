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

package org.luwrain.app.wiki;

import java.net.*;
import java.util.*;
import java.io.*;

import org.luwrain.core.*;
import org.luwrain.core.events.*;
import org.luwrain.controls.*;
import org.luwrain.popups.Popups;

public class WikiApp implements Application, Actions
{
    public static final String STRINGS_NAME = "luwrain.wiki";

    private Luwrain luwrain;
    private Strings strings;
    private FetchThread thread;
    private ListArea area;
    private Model model;
    private Appearance appearance;

    private String searchEn;
    private String searchRu;

    @Override public boolean onLaunch(Luwrain luwrain)
    {
	Object o = luwrain.i18n().getStrings(STRINGS_NAME);
	if (o == null || !(o instanceof Strings))
	    return false;
	strings = (Strings)o;
	this.luwrain = luwrain;

	searchEn = strings.searchEn();
	searchRu = strings.searchRu();
	createArea();
	model.setObjects(new String[]{searchRu, searchEn});
	return true;
    }

    private void createArea()
    {
	final Actions a = this;
	final Strings s = strings;
	final String sEn = searchEn;
	final String sRu = searchRu;

	model = new Model();
	appearance = new Appearance(luwrain, strings);

	final ListClickHandler handler = new ListClickHandler(){
		private Actions actions = a;
		private String searchEn = sEn;
		private String searchRu = sRu;
		@Override public boolean onListClick(ListArea area,
						     int index,
						     Object item)
		{
		    if (item == searchEn)
			return actions.search("en");
		    if (item == searchRu)
			return actions.search("ru");
		    if (item instanceof Page)
		    {
			final Page page = (Page)item;
			return actions.openPage(page.lang(), page.title());
		    }
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
			return actions.closeApp();
		    case EnvironmentEvent.THREAD_SYNC:
			if (event instanceof FetchEvent)
			{
			    final FetchEvent fetchEvent = (FetchEvent)event;
			    if (fetchEvent.code() != FetchEvent.SUCCESS)
			    {
				luwrain.message(strings.errorSearching(), Luwrain.MESSAGE_ERROR);
				return true;
			    }
			    actions.showQueryRes(fetchEvent.pages());
			    return true;
			} else
			    return false;
		    }
		    return super.onEnvironmentEvent(event);
		}
	    };
    }

    @Override public boolean search(String lang)
    {
	if (thread != null && !thread.done())
	    return false;
	final String query = Popups.simple(luwrain, strings.queryPopupName(), strings.queryPopupPrefix(), "");
	if (query == null || query.trim().isEmpty())
	    return true;
	thread = new FetchThread(luwrain, area, lang, query);
	new Thread(thread).start();
	return true;
    }

    @Override public boolean openPage(String lang, String title)
    {
	try {
	    final String url = "https://" + lang + ".wikipedia.org/wiki/" + URLEncoder.encode(title, "UTF-8").replaceAll("\\+", "%20");//Completely unclear why wikipedia doesn't recognize '+' sign
	    System.out.println("opening " + url);
	    luwrain.launchApp("reader", new String[]{"--URL", url});
	}
	catch (UnsupportedEncodingException e)
	{
	    e.printStackTrace();
	    return false;
	}
	return true;
    }

    @Override public void showQueryRes(Page[] pages)
    {
	if (pages == null || pages.length < 1)
	{
	    luwrain.message(strings.nothingFound(), Luwrain.MESSAGE_DONE);
	    return;
	}
	final LinkedList res = new LinkedList();
	res.add(strings.queryResults());
	for(Page p: pages)
	    res.add(p);
	res.add("");
	res.add(searchRu);
	res.add(searchEn);
	model.setObjects(res.toArray(new Object[res.size()]));
	area.refresh();
	area.resetState(false);
	luwrain.message(strings.querySuccess(pages.length), Luwrain.MESSAGE_DONE);
    }

    @Override public String getAppName()
    {
	return strings.appName();
    }

    @Override public AreaLayout getAreasToShow()
    {
	return new AreaLayout(area);
    }

    @Override public boolean closeApp()
    {
	if (thread != null && !thread.done())
	    return false;
	luwrain.closeApp();
	return true;
    }
}

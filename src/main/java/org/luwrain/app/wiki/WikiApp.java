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

package org.luwrain.app.wiki;

import java.net.*;
import java.util.*;
import java.io.*;

import org.luwrain.core.*;
import org.luwrain.core.events.*;
import org.luwrain.controls.*;
import org.luwrain.popups.*;

public class WikiApp implements Application, Actions
{
    static private final String STRINGS_NAME = "luwrain.wiki";

    private final Base base = new Base();
    private Luwrain luwrain;
    private Strings strings;
    private ListArea area;
    private HashSet<String> values = new HashSet<String>();

    @Override public boolean onLaunch(Luwrain luwrain)
    {
	final Object o = luwrain.i18n().getStrings(STRINGS_NAME);
	if (o == null || !(o instanceof Strings))
	    return false;
	strings = (Strings)o;
	if (!base.init(luwrain, strings))
	    return false;
	this.luwrain = luwrain;
	createArea();
	return true;
    }

    private void createArea()
    {
	final Actions actions = this;

	final ListArea.Params params = new ListArea.Params();
	params.environment = new DefaultControlEnvironment(luwrain);
	params.model = base.getModel();
	params.appearance = base.getAppearance();
	params.clickHandler = (area, index, obj)->actions.openPage(obj);
	params.name = strings.appName();

	area = new ListArea(params){
		@Override public boolean onEnvironmentEvent(EnvironmentEvent event)
		{
		    NullCheck.notNull(event, "event");
		    switch(event.getCode())
		    {
		    case CLOSE:
			return actions.closeApp();
		    case ACTION:
			return actions.onActionEvent(event);
		    default:
			return super.onEnvironmentEvent(event);
		    }
		}
		@Override public Action[] getAreaActions()
		{
		    return actions.getAreaActions();
		}
		@Override protected String noContentStr()
		{
		    return strings.noContent();
		}
	    };
    }

    public boolean search(String lang)
    {
	if (base.isBusy())
	    return false;
	final String query = Popups.fixedEditList(luwrain, strings.queryPopupName(), strings.queryPopupPrefix(), "", values.toArray(new String[values.size()]));
	if (query == null || query.trim().isEmpty())
	    return true;
	values.add(query);
	base.search(lang, query, this);
	return true;
    }

    @Override public boolean openPage(Object obj)
    {
	if (obj == null || !(obj instanceof Page))
	    return false;
	final Page page = (Page)obj;
	try {
	    final String url = "https://" + URLEncoder.encode(page.lang()) + ".wikipedia.org/wiki/" + URLEncoder.encode(page.title(), "UTF-8").replaceAll("\\+", "%20");//Completely unclear why wikipedia doesn't recognize '+' sign
	    luwrain.launchApp("reader", new String[]{"--URL", url});
	}
	catch (UnsupportedEncodingException e)
	{
	    e.printStackTrace();
	    luwrain.message(e.getMessage(), Luwrain.MESSAGE_ERROR);
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
	base.getModel().setItems(pages);
	area.refresh();
	area.resetState(false);
	luwrain.message(strings.querySuccess(pages.length), Luwrain.MESSAGE_DONE);
    }

    @Override public String getAppName()
    {
	return strings.appName();
    }

    @Override public Action[] getAreaActions()
    {
	return new Action[]{
	    new Action("search-ru", strings.searchRu()),
	    new Action("search-en", strings.searchEn()),
	};
    }

    @Override public boolean onActionEvent(EnvironmentEvent event)
    {
	NullCheck.notNull(event, "event");
	if (ActionEvent.isAction(event, "search-en"))
	    return search("en");
	if (ActionEvent.isAction(event, "search-ru"))
	    return search("ru");
	return false;
    }

    @Override public AreaLayout getAreasToShow()
    {
	return new AreaLayout(area);
    }

    @Override public boolean closeApp()
    {
	luwrain.closeApp();
	return true;
    }
}

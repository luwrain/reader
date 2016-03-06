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

package org.luwrain.app.reader;

import java.io.File;
import java.net.*;
import java.util.LinkedList;

import org.luwrain.core.*;
import org.luwrain.core.events.*;
import org.luwrain.controls.*;
import org.luwrain.util.*;
import org.luwrain.doctree.*;
import org.luwrain.popups.Popups;

class ReaderArea extends DocTreeArea
{
    static public final int MIN_VISIBLE_WIDTH = 10;

    private Luwrain luwrain;
    private Strings strings;
    private Actions actions;

    ReaderArea(Luwrain luwrain, Strings strings,
	       Actions actions)
    {
	super(new DefaultControlEnvironment(luwrain), new Introduction(new DefaultControlEnvironment(luwrain), strings), null);
	NullCheck.notNull(luwrain, "luwrain");
	NullCheck.notNull(strings, "strings");
	NullCheck.notNull(actions, "actions");
	this.luwrain = luwrain;
	this.strings = strings;
	this.actions = actions;
    }

    @Override public boolean onKeyboardEvent(KeyboardEvent event)
    {
	NullCheck.notNull(event, "event");
	if (event.isSpecial() && event.withShiftOnly())
	    switch(event.getSpecial())
	{
	case ENTER:
	    return actions.showDocInfo();
	}
	if (event.isSpecial() && !event.isModified())
	    switch(event.getSpecial())
	    {
	case TAB:
	    actions.goToNotesArea();
	    return true;
	    case ENTER:
		if (hasHref())
		    return actions.jumpByHref(getHref());
		return false;
	    case BACKSPACE:
		return onBackspace(event);
	    }
	return super.onKeyboardEvent(event);
    }

    @Override public boolean onEnvironmentEvent(EnvironmentEvent event)
    {
	NullCheck.notNull(event, "event");
	switch(event.getCode())
	{
	case ACTION:
	    return actions.onAreaAction(event);
	case CLOSE:
	    actions.closeApp();
	    return true;
	default:
	    return super.onEnvironmentEvent(event);
	}
    }

    @Override public Action[] getAreaActions()
    {
	return actions.areaActions();
    }

    @Override public String getAreaName()
    {
	final Document doc = getDocument();
	return doc != null?doc.getTitle():strings.appName();
    }

    private boolean onBackspace(KeyboardEvent event)
    {
	/*
	if (docInfo.history.size() < 2)
	    return false;
	docInfo.history.pollLast();
	startFetching(docInfo.history.pollLast());
	return true;
	*/
	return false;
    }

    void onFetchedDoc(Result res)
    {
	NullCheck.notNull(res, "res");
	actions.onNewResult(res);
    }

    @Override protected String noContentStr()
    {
	return strings.noContent(actions.fetchingInProgress());
    }

    private int getSuitableWidth()
    {
	final int areaWidth = luwrain.getAreaVisibleWidth(this);
	final int screenWidth = luwrain.getScreenWidth();
	int width = areaWidth;
	if (width < MIN_VISIBLE_WIDTH)
	    width = screenWidth;
	if (width < MIN_VISIBLE_WIDTH)
	    width = MIN_VISIBLE_WIDTH;
	return width;
    }
}

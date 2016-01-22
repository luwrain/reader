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
    private DocInfo docInfo;

    ReaderArea(Luwrain luwrain, Strings strings,
	       Actions actions)
    {
	super(new DefaultControlEnvironment(luwrain), new Introduction(new DefaultControlEnvironment(luwrain), strings), null);
	this.luwrain = luwrain;
	this.strings = strings;
	this.actions = actions;
	NullCheck.notNull(luwrain, "luwrain");
	NullCheck.notNull(strings, "strings");
	NullCheck.notNull(actions, "actions");
    }

    @Override public boolean onKeyboardEvent(KeyboardEvent event)
    {
	NullCheck.notNull(event, "event");
	if (event.isCommand() && !event.isModified())
	    switch(event.getCommand())
	    {
	    case KeyboardEvent.ENTER:
		return onEnter(event);
	    case KeyboardEvent.BACKSPACE:
		return onBackspace(event);
	    }
	return super.onKeyboardEvent(event);
    }

    @Override public boolean onEnvironmentEvent(EnvironmentEvent event)
    {
	NullCheck.notNull(event, "event");
	switch(event.getCode())
	{
	case EnvironmentEvent.ACTION:
	    if (ActionEvent.isAction(event, "open-in-narrator"))
	    {
		actions.openInNarrator();
	    }
		/*
	    if (ActionEvent.isAction(event, "open"))
	    {
		onOpenDoc();
		return true;
	    }
	    if (ActionEvent.isAction(event, "new-format"))
	    {
		onNewFormat();
		return true;
	    }
	    if (ActionEvent.isAction(event, "new-charset"))
	    {
		onNewCharset();
		return true;
	    }
		*/
	    return false;
	case EnvironmentEvent.CLOSE:
	    actions.closeApp();
	    return true;
	default:
	    return super.onEnvironmentEvent(event);
	}
    }

    @Override public Action[] getAreaActions()
    {
	return new Action[]{
	    new Action("open-in-narrator", strings.contextMenuOpenInNarrator())
	    //	    new Action("open", "Открыть документ"),//FIXME:
	    //	    new Action("new-format", "Сменить формат"),//FIXME:
	    //	    new Action("new-charset", "Сменить кодировку"),//FIXME:
	};
    }

    @Override public String getAreaName()
    {
	final Document doc = getDocument();
	return doc != null?doc.getTitle():strings.appName();
    }

    private boolean onEnter(KeyboardEvent event)
    {
	final String href = getHref();
	return actions.jumpByHref(href);
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

    void onFetchedDoc(Document newDoc)
    {
	NullCheck.notNull(newDoc, "newDoc");
	newDoc.buildView(luwrain.getAreaVisibleWidth(this));
	setDocument(newDoc);
	actions.onNewDocument(newDoc);
	luwrain.silence();
	luwrain.playSound(Sounds.INTRO_REGULAR);
	luwrain.say(newDoc.getTitle());
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

    private int chooseFormat()
    {
	/*
	final String[] formats = FormatsList.getSupportedFormatsList();
	final String[] formatsStr = new String[formats.length];
	for(int i = 0;i < formats.length;++i)
	{
	    final int pos = formats[i].indexOf(":");
	    if (pos < 0 || pos + 1 >= formats[i].length())
	    {
		formatsStr[i] = formats[i];
		continue;
	    }
	    formatsStr[i] = formats[i].substring(pos + 1);
	}
	final Object selected = Popups.fixedList(luwrain, "Выберите формат для просмотра:", formatsStr, 0);//FIXME:
	if (selected == null)
	    return Factory.UNRECOGNIZED;
	String format = null;
	for(int i = 0;i < formatsStr.length;++i)
	    if (selected == formatsStr[i])
		format = formats[i];
	if (format == null)
	    return Factory.UNRECOGNIZED;
	final int pos = format.indexOf(":");
	if (pos < 0 || pos + 1>= format.length())
	    return Factory.UNRECOGNIZED;
	luwrain.message(format.substring(0, pos));
	return DocInfo.formatByStr(format.substring(0, pos));
	*/
	return 0;
    }
}

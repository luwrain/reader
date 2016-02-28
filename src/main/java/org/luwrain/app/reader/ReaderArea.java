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
		return openUrl(false);
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
	    if (ActionEvent.isAction(event, "open-in-narrator"))
	    {
		actions.openInNarrator();
		return true;
	    }

	    if (ActionEvent.isAction(event, "doc-mode"))
	    {
		actions.docMode();
		return true;
	    }

	    if (ActionEvent.isAction(event, "book-mode"))
	    {
		actions.bookMode();
		return true;
	    }

	    if (ActionEvent.isAction(event, "open-url"))
		return openUrl(true);



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
	case CLOSE:
	    actions.closeApp();
	    return true;
	default:
	    return super.onEnvironmentEvent(event);
	}
    }

    @Override public Action[] getAreaActions()
    {
	return new Action[]{
	    new Action("open-file", strings.actionTitle("open-file"), new KeyboardEvent(KeyboardEvent.Special.F5)),
	    new Action("open-url", strings.actionTitle("open-url"), new KeyboardEvent(KeyboardEvent.Special.F6)),
	    new Action("open-in-narrator", strings.actionTitle("open-in-narrator"), new KeyboardEvent(KeyboardEvent.Special.F8)),
	    new Action("change-format", strings.actionTitle("change-format"), new KeyboardEvent(KeyboardEvent.Special.F9)),
	    new Action("change-charset", strings.actionTitle("change-charset"), new KeyboardEvent(KeyboardEvent.Special.F10)),
	    new Action("book-mode", strings.actionTitle("book-mode")),
	    new Action("bdoc-mode", strings.actionTitle("doc-mode")),
	    new Action("info", strings.actionTitle("info"), new KeyboardEvent(KeyboardEvent.Special.F8)),
	};
    }

    @Override public String getAreaName()
    {
	final Document doc = getDocument();
	return doc != null?doc.getTitle():strings.appName();
    }

    private boolean openUrl(boolean openPopup)
    {
	if (!openPopup && !hasHref())
	    return false;
	String href = hasHref()?getHref():"";
	if (openPopup)
	{
	    final String res = Popups.simple(luwrain, "Открыть URL", "Введите адрес ссылки:", href);
	    if (res == null)
		return true;
	    href = res;
	}
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

    void onFetchedDoc(Result res)
    {
	NullCheck.notNull(res, "res");
	if (res.type() != Result.Type.OK)
	{
	    actions.showErrorPage(res);
	    return;
	}
	Document newDoc = res.doc();
	newDoc.buildView(luwrain.getAreaVisibleWidth(this));
	setDocument(newDoc);
	actions.onNewDocument(res);
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

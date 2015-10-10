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
	       Actions actions, Document document,
	       DocInfo docInfo)
    {
	super(new DefaultControlEnvironment(luwrain), new Introduction(new DefaultControlEnvironment(luwrain), strings), document);
	this.luwrain = luwrain;
	this.strings = strings;
	this.actions = actions;
	this.docInfo = docInfo;
	NullCheck.notNull(luwrain, "luwrain");
	NullCheck.notNull(strings, "strings");
	NullCheck.notNull(actions, "actions");
	NullCheck.notNull(docInfo, "docInfo");
    }



    @Override public boolean onEnvironmentEvent(EnvironmentEvent event)
    {
	NullCheck.notNull(event, "event");
	switch(event.getCode())
	{
	case EnvironmentEvent.ACTION:
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
	    return false;
	case EnvironmentEvent.THREAD_SYNC:
	    return onThreadSync(event);
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
	    new Action("open", "Открыть документ"),//FIXME:
	    new Action("new-format", "Сменить формат"),//FIXME:
	    new Action("new-charset", "Сменить кодировку"),//FIXME:
	};
    }

    @Override public String getAreaName()
    {
	final Document doc = getDocument();
	return doc != null?doc.getTitle():strings.appName();
    }

    private void onOpenDoc()
    {
	final File f = Popups.file(luwrain, "Просмотр файла", "Выберите файл для просмотра", 
				   luwrain.launchContext().userHomeDirAsFile(), 0, 0);
	if (f == null)
	    return;
	if (f.isDirectory())
	{
	    luwrain.message("Просмотр невозможен для каталогов", Luwrain.MESSAGE_ERROR);
	    return;
	}
	int format = Factory.suggestFormat(f.getAbsolutePath());
	if (format == Factory.UNRECOGNIZED)
	    format = DocInfo.DEFAULT_FORMAT;
	final Document doc = Factory.loadFromFile(format, f.getAbsolutePath(), getSuitableWidth(), DocInfo.DEFAULT_CHARSET);
	if (doc == null)
	{
	    luwrain.message("Во время открытия файла произошла непредвиденная ошибка", Luwrain.MESSAGE_ERROR);
	    return;
	}
setDocument(doc);
	docInfo.charset = DocInfo.DEFAULT_CHARSET;
	docInfo.format = format;
	docInfo.fileName = f.getAbsolutePath();

    }

    void onNewFormat()
    {


	if (getDocument() == null)
	{
	    luwrain.message("Нет открытого документа", Luwrain.MESSAGE_ERROR);//FIXME:
	    return;
	}
final int res = chooseFormat();
if (res == Factory.UNRECOGNIZED)
    return;
	final Document doc = Factory.loadFromFile(res, docInfo.fileName, getSuitableWidth(), docInfo.charset);
	if (doc == null)
	{
	    luwrain.message("Не удалось перечитать документ с новыми параметрами", Luwrain.MESSAGE_ERROR);
	    return;
	}
	setDocument(doc);
	docInfo.format = res;
    }

    private void onNewCharset()
    {
	if (getDocument() == null)
	{
	    luwrain.message("Нет открытого документа", Luwrain.MESSAGE_ERROR);//FIXME:
	    return;
	}
	final String[] charsets = new String[]{//FIXME:
		"UTF-8",
		    "KOI8-R",
		    "windows-1251",
		//FIXME:		    "IBMM866",
		    "x-MacCyrillic",
		    };
	final String res = (String)Popups.fixedList(luwrain, "Выберите новую кодировку:", charsets, 0); 
	if (res == null)
	    return;
	final Document doc = Factory.loadFromFile(docInfo.format, docInfo.fileName, getSuitableWidth(), res);
	if (doc == null)
	{
	    luwrain.message("Не удалось перечитать документ с новыми параметрами", Luwrain.MESSAGE_ERROR);
	    return;
	}
	setDocument(doc);
	docInfo.charset = res;
    }

    private boolean onThreadSync(EnvironmentEvent event)
    {
	if (event instanceof FetchEvent)
	{
	    final FetchEvent fetchEvent = (FetchEvent)event;
	    if (fetchEvent.getFetchCode() == FetchEvent.FAILED)
	    {
		luwrain.message(strings.errorFetching(), Luwrain.MESSAGE_ERROR);
		return true;
	    }
	    final Document doc = Factory.loadFromText(Factory.HTML, fetchEvent.getText(), getSuitableWidth());
	    if (doc != null)
	    {
		setDocument(doc);
		luwrain.playSound(Sounds.MESSAGE_DONE);
	    }  else
		luwrain.message("problem parsing", Luwrain.MESSAGE_ERROR);//FIXME:
	    return true;
	}
	return false;
    }

    @Override protected void noContentMessage()
    {
	luwrain.hint(strings.noContent(), Hints.NO_CONTENT);
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
    }
}

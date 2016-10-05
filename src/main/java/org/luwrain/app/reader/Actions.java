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

import java.util.*;
import java.nio.charset.*;
import java.net.*;
import javax.activation.*;

import org.luwrain.core.*;
import org.luwrain.core.events.*;
import org.luwrain.popups.Popups;
import org.luwrain.doctree.*;
import org.luwrain.doctree.loading.*;

class Actions
{
    static public final SortedMap<String, Charset> AVAILABLE_CHARSETS = Charset.availableCharsets();

    static Action[] getReaderAreaActions(Strings strings, boolean hasDocument,
					 ReaderApp.Modes mode)
    {
	NullCheck.notNull(strings, "strings");
	NullCheck.notNull(mode, "mode");
	final LinkedList<Action> res = new LinkedList<Action>();
	if (hasDocument)
	{
	    res.add(new Action("play-audio", strings.actionPlayAudio(), new KeyboardEvent(KeyboardEvent.Special.F7)));
	    res.add(new Action("open-in-narrator", strings.actionOpenInNarrator(), new KeyboardEvent(KeyboardEvent.Special.F8)));

	    res.add(new Action("save-bookmark", strings.actionSaveBookmark(), new KeyboardEvent(KeyboardEvent.Special.F2)));
	    res.add(new Action("restore-bookmark", strings.actionRestoreBookmark(), new KeyboardEvent(KeyboardEvent.Special.F2, EnumSet.of(KeyboardEvent.Modifiers.SHIFT))));
	    switch(mode)
	    {
	    case DOC:
		res.add(new Action("change-format", strings.actionChangeFormat(), new KeyboardEvent(KeyboardEvent.Special.F9)));
		res.add(new Action("change-charset", strings.actionChangeCharset(), new KeyboardEvent(KeyboardEvent.Special.F10)));
		res.add(new Action("show-notes", strings.actionShowNotes(), new KeyboardEvent(KeyboardEvent.Special.F6)));
		break;
	    case DOC_NOTES:
		res.add(new Action("change-format", strings.actionChangeFormat(), new KeyboardEvent(KeyboardEvent.Special.F9)));
		res.add(new Action("change-charset", strings.actionChangeCharset(), new KeyboardEvent(KeyboardEvent.Special.F10)));
		res.add(new Action("hide-notes", strings.actionHideNotes(), new KeyboardEvent(KeyboardEvent.Special.F6)));
		break;
	    case BOOK:
		res.add(new Action("show-sections-tree", strings.actionShowSectionsTree(), new KeyboardEvent(KeyboardEvent.Special.F5)));
		res.add(new Action("show-notes", strings.actionShowNotes(), new KeyboardEvent(KeyboardEvent.Special.F6)));
		break;
	    case BOOK_TREE_ONLY:
		res.add(new Action("hide-sections-tree", strings.actionHideSectionsTree(), new KeyboardEvent(KeyboardEvent.Special.F5)));
		res.add(new Action("show-notes", strings.actionShowNotes(), new KeyboardEvent(KeyboardEvent.Special.F6)));
		break;
	    case BOOK_NOTES_ONLY:
		res.add(new Action("show-sections-tree", strings.actionShowSectionsTree(), new KeyboardEvent(KeyboardEvent.Special.F5)));
		res.add(new Action("hide-notes", strings.actionHideNotes(), new KeyboardEvent(KeyboardEvent.Special.F6)));
		break;
	    case BOOK_TREE_NOTES:
		res.add(new Action("hide-sections-tree", strings.actionHideSectionsTree(), new KeyboardEvent(KeyboardEvent.Special.F5)));
		res.add(new Action("hide-notes", strings.actionHideNotes(), new KeyboardEvent(KeyboardEvent.Special.F6)));
		break;
	    }
	}
	res.add(new Action("open-file", strings.actionOpenFile(), new KeyboardEvent(KeyboardEvent.Special.F3, EnumSet.of(KeyboardEvent.Modifiers.SHIFT))));
	res.add(new Action("open-url", strings.actionOpenUrl(), new KeyboardEvent(KeyboardEvent.Special.F4, EnumSet.of(KeyboardEvent.Modifiers.SHIFT))));
	return res.toArray(new Action[res.size()]);
    }

    static boolean onSaveBookmark(Luwrain luwrain, Strings strings, DoctreeArea area)
    {
	NullCheck.notNull(luwrain, "luwrain");
	NullCheck.notNull(strings, "strings");
	NullCheck.notNull(area, "area");
	final int value = area.getCurrentRowIndex();
	if (value < 0)
	    return false;
	final URL url = area.getUrl();
	if (url == null)
	    return false;
	Settings.setBookmark(luwrain.getRegistry(), url.toString(), value);
	luwrain.message(strings.bookmarkSaved(), Luwrain.MESSAGE_OK);
	return true;
    }

    static boolean onRestoreBookmark(Luwrain luwrain, Strings strings, DoctreeArea area)
    {
	NullCheck.notNull(luwrain, "luwrain");
	NullCheck.notNull(strings, "strings");
	NullCheck.notNull(area, "area");
	final URL url = area.getUrl();
	if (url == null)
	    return false;
final int value = Settings.getBookmark(luwrain.getRegistry(), url.toString());
if (value < 0 || !area.setCurrentRowIndex(value))
{
    luwrain.message(strings.noBookmark(), Luwrain.MESSAGE_ERROR);
    return true;
}
luwrain.playSound(Sounds.DONE);
	return true;
    }

    static boolean onChangeFormat(ReaderApp app, Luwrain luwrain,
				  Strings strings, Base base)
    {
	NullCheck.notNull(app, "app");
	NullCheck.notNull(luwrain, "luwrain");
	NullCheck.notNull(strings, "strings");
	NullCheck.notNull(base, "base");
	if (base.fetchingInProgress())
	    return false;
	final String[] formats;
	try {
	    formats = (String[])luwrain.getSharedObject(org.luwrain.doctree.Extension.FORMATS_OBJECT_NAME);
	}
	catch(Exception e)
	{
	    Log.error("reader", "no formats shared object:" + e.getClass().getName() + ":" + e.getMessage());
	    e.printStackTrace();
	    return false;
	}
	final URL url = base.getCurrentUrl();
	if (url == null)
	    return false;
	final String contentType = base.getCurrentContentType();
	final String chosen = (String)Popups.fixedList(luwrain, strings.changeFormatPopupName(), formats);
	if (chosen == null || chosen.isEmpty())
	    return true;
	if (contentType.isEmpty())
	{
	    base.open(app, url, chosen);
	    return true;
	}
	try {
	    final MimeType mime = new MimeType(contentType);
	    final String charset = mime.getParameter("charset");
	    if (charset == null || charset.isEmpty())
	    {
		base.open(app, url, chosen);
		return true;
	    }
	    final MimeType newMime = new MimeType(chosen);
	    newMime.setParameter("charset", charset);
	    base.open(app, url, newMime.toString());
	    return true;
	}
	catch(MimeTypeParseException e)
	{
	    Log.warning("reader", "unable to parse current content type \'" + contentType + "\':" + e.getMessage());
	    e.printStackTrace();
	    base.open(app, url, chosen);
	    return true;
	}
    }

    static boolean onChangeCharset(ReaderApp app, Luwrain luwrain,
				   Strings strings, Base base)
    {
	NullCheck.notNull(app, "app");
	NullCheck.notNull(luwrain, "luwrain");
	NullCheck.notNull(strings, "strings");
	NullCheck.notNull(base, "base");
	if (base.fetchingInProgress())
	    return false;
	final URL url = base.getCurrentUrl();
	if (url == null)
	    return false;
	final String contentType = base.getCurrentContentType();
	if (contentType.isEmpty())
	    return false;
	final String chosen = (String)Popups.fixedList(luwrain, strings.changeCharsetPopupName(), charsets(luwrain.getRegistry()));
	if (chosen == null || chosen.isEmpty())
	    return true;
	try {
	    final MimeType newMime = new MimeType(contentType);
	    newMime.setParameter("charset", chosen);
	    base.open(app, url, newMime.toString());
	    return true;
	}
	catch(MimeTypeParseException e)
	{
	    luwrain.crash(e);
	    return true;
	}
    }

    static Action[] getTreeAreaActions(Strings strings, boolean hasDocument,
				       ReaderApp.Modes mode)
    {
	NullCheck.notNull(strings, "strings");
	NullCheck.notNull(mode, "mode");
	return getReaderAreaActions(strings, hasDocument, mode);
    }

    static Action[] getNotesAreaActions(Strings strings, boolean hasDocument,
					ReaderApp.Modes mode)
    {
	NullCheck.notNull(strings, "strings");
	NullCheck.notNull(mode, "mode");
	final LinkedList<Action> res = new LinkedList<Action>();
	res.add(new Action("add-note", strings.actionAddNote(), new KeyboardEvent(KeyboardEvent.Special.INSERT)));
	for(Action a: getReaderAreaActions(strings, hasDocument, mode))
	    res.add(a);
	return res.toArray(new Action[res.size()]);
    }

    static boolean onPlayAudio(Base base, DoctreeArea area)
    {
	NullCheck.notNull(base, "base");
	NullCheck.notNull(area, "area");
	if (!base.isInBookMode())
	    return false;
	final String[] ids = area.getHtmlIds();
	if (ids == null || ids.length < 1)
	    return false;
	return base.playAudio(area, ids);
    }

    static String[] charsets(Registry registry)
    {
	NullCheck.notNull(registry, "registry");
	final String[] settings = org.luwrain.core.Settings.getI18nCharsets(registry);
	final LinkedList<String> res = new LinkedList<String>();
	for(Map.Entry<String, Charset> e: AVAILABLE_CHARSETS.entrySet())
	{
	    int i;
	    for (i = 0;i < settings.length;++i)
		if (e.getKey().toLowerCase().trim().equals(settings[i].toLowerCase().trim()))
		    break;
	    if (i < settings.length)
		res.add(e.getKey());
	}
	return res.toArray(new String[res.size()]);
    }
}

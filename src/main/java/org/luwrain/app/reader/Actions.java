/*
   Copyright 2012-2019 Michael Pozhidaev <msp@luwrain.org>
   Copyright 2015-2016 Roman Volovodov <gr.rPman@gmail.com>

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

package org.luwrain.app.reader;

import java.util.*;
import java.io.*;
import java.nio.file.*;
import java.nio.charset.*;
import java.net.*;
import javax.activation.*;

import org.luwrain.core.*;
import org.luwrain.core.events.*;
import org.luwrain.util.*;
import org.luwrain.popups.Popups;
import org.luwrain.controls.reader.*;

import org.luwrain.app.reader.Base.ParaStyle;

final class Actions
{
    static public final SortedMap<String, Charset> AVAILABLE_CHARSETS = Charset.availableCharsets();

    private final Luwrain luwrain;
    private final Strings strings;
    private final Base base;
    final Conversations conv;

    Actions( Base base)
    {
	NullCheck.notNull(base, "base");
	this.luwrain = base.luwrain;
	this.base = base;
	this.strings = base.strings;
	this.conv = new Conversations(luwrain, strings);
    }

boolean onOpenUrl(String currentHref)
    {
	NullCheck.notNull(currentHref, "currentHref");
	if (base.isBusy())
	    return false;
	final URL url = conv.urlToOpen(currentHref);
	if (url == null)
	    return true;
	    base.openInitial(url, "");
	    return true;
    }

    boolean onOpenFile()
    {
	if (base.isBusy())
	    return false;
	final File res = conv.fileToOpen();
	if (res == null)
	return false;
	base.openInitial(Urls.toUrl(res), "");
	return true;
    }

    boolean onChangeTextParaStyle()
    {
	if (base.isBusy() || !base.hasDocument())
	    return false;
	final ParaStyle paraStyle = conv.textParaStyle();
	if (paraStyle == null)
	return true;
	base.changeTextParaStyle(paraStyle);
	return true;
    }

    boolean onSaveBookmark(ReaderArea area)
    {
	NullCheck.notNull(area, "area");
	if (base.isBusy() || !base.hasDocument())
	    return false;
	final int value = area.getCurrentRowIndex();
	if (value < 0)
	    return false;
	final StoredProperties props = base.getStoredProps();
	if (props == null)
	    return false;
	props.setBookmarkPos(value);
	luwrain.message(strings.bookmarkSaved(), Luwrain.MessageType.OK);
	return true;
    }

    boolean onRestoreBookmark(ReaderArea area)
    {
	/*
	NullCheck.notNull(area, "area");
	final String url = area.getDocUrl();
	if (url == null || url.isEmpty())
	    return false;
final int value = Settings.getBookmark(luwrain.getRegistry(), url);
if (value < 0 || !area.setCurrentRowIndex(value))
{
    luwrain.message(strings.noBookmark(), Luwrain.MessageType.ERROR);
    return true;
}
luwrain.playSound(Sounds.DONE);
	*/
	return true;
    }

    boolean onChangeCharset()
    {
	if (!base.hasDocument() || base.isBusy())
	    return false;
	final String chosen = (String)Popups.fixedList(luwrain, strings.changeCharsetPopupName(), charsets(luwrain.getRegistry()));
	if (chosen == null || chosen.isEmpty())
	    return true;
	base.changeCharset(chosen);
	return true;
    }

    boolean onPlayAudio(ReaderArea area)
    {
	NullCheck.notNull(area, "area");
	if (!base.isInBookMode())
	    return false;
	final String[] ids = area.getHtmlIds();
	if (ids == null || ids.length == 0)
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

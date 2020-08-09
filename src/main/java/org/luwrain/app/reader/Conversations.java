/*
   Copyright 2012-2020 Michael Pozhidaev <msp@luwrain.org>
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
import java.nio.charset.*;
import java.net.*;

import org.luwrain.core.*;
import org.luwrain.popups.Popups;


final class Conversations
{
    static public final SortedMap<String, Charset> AVAILABLE_CHARSETS = Charset.availableCharsets();
    static final LinkedList<String> enteredUrls = new LinkedList<String>();

    private final Luwrain luwrain;
    private final Strings strings;

    Conversations(Luwrain luwrain, Strings strings)
    {
	NullCheck.notNull(luwrain, "luwrain");
	NullCheck.notNull(strings, "strings");
	this.luwrain = luwrain;
	this.strings = strings;
    }

    URL urlToOpen(String currentHref)
    {
	NullCheck.notNull(currentHref, "currentHref");
	final String res = Popups.fixedEditList(luwrain, strings.openUrlPopupName(), strings.openUrlPopupPrefix(), currentHref.isEmpty()?"http://":currentHref, 
						enteredUrls.toArray(new String[enteredUrls.size()]));
	if (res == null)
	    return null;
	enteredUrls.add(res);
	try {
	    return new URL(res);
	}
	catch(MalformedURLException e)
	{
	    return null;
	}
    }

    File fileToOpen()
    {
	return Popups.existingFile(luwrain, strings.openPathPopupName(), strings.openPathPopupPrefix());
    }

    String newNote()
    {
	return Popups.textNotEmpty(luwrain, strings.addNotePopupName(), strings.addNotePopupPrefix(), "");
    }

    /*
    ParaStyle textParaStyle()
    {
	final String emptyLines = "Разбиение по пустым строкам";
	final String indent = "Разбиение по отступам строк";
	final String eachLine = "Одна строка - один параграф";
	final Object o = Popups.fixedList(luwrain, "Тип форматирования параграфов:", new Object[]{emptyLines, indent, eachLine});
	if (o == null)
	    return null;
	if (o == emptyLines)
	    return ParaStyle.EMPTY_LINES;
	if (o == indent)
	    return ParaStyle.INDENT;
	if (o == eachLine)
	    return ParaStyle.EACH_LINE;
	return null;
    }
    */
}

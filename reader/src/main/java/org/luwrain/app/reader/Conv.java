// SPDX-License-Identifier: BUSL-1.1
// Copyright 2012-2026 Michael Pozhidaev <msp@luwrain.org>

package org.luwrain.app.reader;

import java.util.*;
import java.io.*;
import java.nio.charset.*;
import java.net.*;

import org.luwrain.core.*;
import org.luwrain.popups.Popups;

import static java.util.Objects.*;

final class Conv
{
    static public final SortedMap<String, Charset> AVAILABLE_CHARSETS = Charset.availableCharsets();
    static final LinkedList<String> enteredUrls = new LinkedList<String>();

    private final Luwrain luwrain;
    private final Strings strings;

    Conversations(Luwrain luwrain, Strings strings)
    {
	requireNonNull(luwrain, "luwrain can't be null");
	requireNonNull(strings, "strings can't be null");
	this.luwrain = luwrain;
	this.strings = strings;
    }

    URL urlToOpen(String currentHref)
    {
	requireNonNull(currentHref, "currentHref can't be null");
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
	return Popups.existingFile(luwrain, strings.openPathPopupPrefix());
    }

    String newNote()
    {
	return Popups.textNotEmpty(luwrain, strings.addNotePopupName(), strings.addNotePopupPrefix(), "");
    }

    boolean confirmLocalBookDeleting(String title)
    {
	requireNonNull(title, "title can't be null");
	return Popups.confirmDefaultYes(luwrain, strings.localRepoDeletePopupName(), strings.localRepoDeletePopupText(title));
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

/*
   Copyright 2012-2018 Michael Pozhidaev <michael.pozhidaev@gmail.com>

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
import java.util.concurrent.*;
import java.net.*;
import java.io.*;
import java.nio.file.*;

import org.luwrain.core.*;
import org.luwrain.controls.*;
import org.luwrain.popups.Popups;
import org.luwrain.reader.*;
import org.luwrain.controls.reader.*;
import org.luwrain.app.reader.books.*;
import org.luwrain.player.*;
import org.luwrain.app.reader.formats.*;

final class ErrorAreaBase
{
    /*
    private final Luwrain luwrain;
    private final Strings strings;

    Base(Luwrain luwrain, Strings strings,
	 Runnable successNotification, Runnable errorNotification)
    {
	NullCheck.notNull(luwrain, "luwrain");
	NullCheck.notNull(strings, "strings");
	NullCheck.notNull(successNotification, "successNotification");
	NullCheck.notNull(errorNotification, "errorNotification");
	this.luwrain = luwrain;
	this.strings = strings;
	this.successNotification = successNotification;
	this.errorNotification = errorNotification;
	this.audioPlaying = new AudioPlaying();
	if (!audioPlaying.init(luwrain))
	    audioPlaying = null;
	bookTreeModelSource = new BookTreeModelSource(strings.bookTreeRoot(), new Book.Section[0]);
	bookTreeModel = new CachedTreeModel(bookTreeModelSource);
    }

    void prepareErrorText(UrlLoader.Result res, MutableLines lines)
    {
	lines.addLine(strings.errorAreaIntro());
	switch(res.type)
	{
	case UNKNOWN_HOST:
	    lines.addLine(strings.unknownHost(res.getProperty("host")));
	    break;
	case HTTP_ERROR:
	    lines.addLine(strings.httpError(res.getProperty("httpcode")));
	    break;
	case FETCHING_ERROR:
	    lines.addLine(strings.fetchingError(res.getProperty("descr")));
	    break;
	case UNDETERMINED_CONTENT_TYPE:
	    lines.addLine(strings.undeterminedContentType());
	    break;
	case UNRECOGNIZED_FORMAT:
	    lines.addLine(strings.unrecognizedFormat(res.getProperty("contenttype")));
	    break;
	}
	if (!res.getProperty("url").isEmpty())
	    lines.addLine(strings.propertiesAreaUrl(res.getProperty("url")));
	if (!res.getProperty("format").isEmpty())
	    lines.addLine(strings.propertiesAreaFormat(res.getProperty("format")));
	if (!res.getProperty("charset").isEmpty())
	    lines.addLine(strings.propertiesAreaCharset(res.getProperty("charset")));
    }

    boolean fillDocProperties(MutableLines lines)
    {
	NullCheck.notNull(lines, "lines");
	if (history.isEmpty())
	    return false;
	final HistoryItem item = history.getLast();
	lines.beginLinesTrans();
	lines.addLine(strings.propertiesAreaUrl(item.url));
	lines.addLine(strings.propertiesAreaContentType(item.contentType));
	lines.addLine(strings.propertiesAreaFormat(item.format));
	lines.addLine(strings.propertiesAreaCharset(item.charset));
	lines.addLine("");
	lines.endLinesTrans();
	return true;
    }

    boolean playAudio(DocumentArea area, String[] ids)
    {
	NullCheck.notNull(area, "area");
	NullCheck.notNullItems(ids, "ids");
	if (!isInBookMode())
	    return false;
	if (audioPlaying == null)
	return false;
	return audioPlaying.playAudio(res.book, res.doc, area, ids);
    }

    boolean stopAudio()
    {
	if (audioPlaying == null)
	    return false;
	audioPlaying.stop();
	return true;
    }

    private URL getNotesUrl()
    {
	if (isInBookMode())
	    return res.book.getStartingDocument().getUrl();
	return res.doc.getUrl();
    }

    void updateNotesModel()
    {
	if (res.doc == null && res.book == null)
	    return;
	final URL url = getNotesUrl();
	notesModel.setItems(Settings.getNotes(luwrain.getRegistry(), url.toString()));
    }

    boolean addNote(int pos)
    {
	NullCheck.notNull(res.doc, "res.doc");
	final String text = Popups.simple(luwrain, strings.addNotePopupName(), strings.addNotePopupPrefix(), "");
	if (text == null)
	    return false;
	Settings.addNote(luwrain.getRegistry(), getNotesUrl().toString(), res.doc.getUrl().toString(), pos, text, "");
	updateNotesModel();
	return true;
    }

    void deleteNote(Note note)
    {
	NullCheck.notNull(note, "note");
	Settings.deleteNote(luwrain.getRegistry(), getNotesUrl().toString(), note.num);
	updateNotesModel();
    }

    boolean hasDocument()
    {
	return res != null && res.doc!= null;
    }

    Document getDocument()
    {
	return res.doc;
    }

    boolean isBusy()
    {
	return task != null && !task.isDone();
    }

    boolean isInBookMode()
    {
	return res != null && res.book != null;
    }

    String getContentType()
    {
	if (!hasDocument())
	    return "";
	final String r = res.doc.getProperty("contenttype");
	return r != null?r:"";
    }

    URL getCurrentUrl()
    {
	return res.doc != null?res.doc.getUrl():null;
    }

    boolean openInNarrator()
    {
	final NarratorTextVisitor visitor = new NarratorTextVisitor();
	Visitor.walk(res.doc.getRoot(), visitor);
	luwrain.launchApp("narrator", new String[]{"--TEXT", visitor.toString()});
	return true;
    }

    TreeArea.Model getTreeModel()
    {
	return bookTreeModel;
    }

ListArea.Model getNotesModel()
    {
	return notesModel;
    }

    UrlLoader.Result getResult()
    {
	return res;
    }

    

    UrlLoader.Result getErrorRes()
    {
	return errorRes;
    }

    static String getHref(DocumentArea area)
    {
	NullCheck.notNull(area, "area");
	final Run run = area.getCurrentRun();
	if (run == null)
	    return "";
	return run.href();//Never returns null
    }

    static boolean hasHref(DocumentArea area)
    {
	NullCheck.notNull(area, "area");
	return !getHref(area).isEmpty();
    }

    static private TextFiles.ParaStyle translateParaStyle(String str)
    {
	NullCheck.notNull(str, "str");
	switch(str)
	{
	case "EMPTY_LINES":
	    return TextFiles.ParaStyle.EMPTY_LINES;
	case "INDENT":
	    return TextFiles.ParaStyle.INDENT;
	case "EACH_LINE":
	    return TextFiles.ParaStyle.EACH_LINE;
	default:
	    return null;
	}
    }
    */
}

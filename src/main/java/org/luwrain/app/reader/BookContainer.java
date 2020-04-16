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
import java.net.*;

import org.luwrain.core.*;
import org.luwrain.reader.*;
import org.luwrain.app.reader.books.*;
import org.luwrain.player.*;
import org.luwrain.template.*;

/*
	if (savedPosition > 0)
	    res.doc.setProperty(Document.DEFAULT_ITERATOR_INDEX_PROPERTY, "" + savedPosition);
*/

final class BookContainer
{
    private final App app;
    private final Book book;
    private Document doc = null;
        private final LinkedList<HistoryItem> history = new LinkedList();
        private Book.Section[] sections = new Book.Section[0];

    BookContainer(App app, Book book)
    {
	NullCheck.notNull(app, "app");
	NullCheck.notNull(book, "book");
	this.app = app;
	this.book = book;
	this.doc = this.book.getStartingDocument();
    }

    boolean jump(String href, org.luwrain.controls.reader.ReaderArea readerArea, int newRowNum, Runnable onSuccess)
    {
	NullCheck.notEmpty(href, "href");
	NullCheck.notNull(readerArea, "readerArea");
	NullCheck.notNull(onSuccess, "onSuccess");
	final App.TaskId taskId = app.newTaskId();
	return app.runTask(taskId, ()->{	
		final Document doc;
		try {
		    doc = book.getDocument(href);
		}
		catch(IOException e)
		{
		    app.showErrorLayout(e);
		    return;
		}
		if (doc == null)//should not happen, all errors must be indicated through exceptions
	    return;
	if (doc != this.doc)
	{
	    final int currentRowNum = readerArea.getCurrentRowIndex();
	    if (currentRowNum >= 0 && !history.isEmpty())
		history.getLast().lastRowIndex = currentRowNum;
	    history.add(new HistoryItem(doc));
	}
	if (newRowNum >= 0)
	    doc.setProperty(Document.DEFAULT_ITERATOR_INDEX_PROPERTY, String.valueOf(newRowNum));
	app.finishedTask(taskId, ()->{
			this.doc = doc;
			onSuccess.run();
	    });
	    });
	    }


        boolean changeCharset(String newCharset)
    {
	NullCheck.notNull(newCharset, "newCharset");
	/*
	final UrlLoader urlLoader;
	try {
	    urlLoader = new UrlLoader(luwrain, res.doc.getUrl());
	}
	catch(MalformedURLException e)
	{
	    luwrain.crash(e);
	    return false;
	}
		if (storedProps == null)
	    storedProps = new StoredProperties(luwrain.getRegistry(), res.doc.getUrl().toString());
		storedProps.setCharset(newCharset);
	urlLoader.setCharset(newCharset);
	final ParaStyle paraStyle = translateParaStyle(storedProps.getParaStyle());
	if (paraStyle != null)
	    urlLoader.setTxtParaStyle(paraStyle);
	task = createTask(urlLoader);
	luwrain.executeBkg(task);
	*/
	return true;
    }

    boolean onPrevDoc()
    {
	if (history.isEmpty())
	    return false;
	final HistoryItem item = history.pollLast();
	/*
	    res.doc = item.doc;
successNotification.run();
	return true;
	*/
	return true;
    }

    /*
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
    */

    boolean playAudio(org.luwrain.controls.reader.ReaderArea area, String[] ids)
    {
	NullCheck.notNull(area, "area");
	NullCheck.notNullItems(ids, "ids");
	final AudioPlaying audioPlaying = app.getAudioPlaying();
	if (audioPlaying == null)
	return false;
	return audioPlaying.playAudio(this.book, this.doc, area, ids);
    }

    boolean stopAudio()
    {
	final AudioPlaying audioPlaying = app.getAudioPlaying();
	if (audioPlaying == null)
	    return false;
	return audioPlaying.stop();
    }

    /*
    private URL getNotesUrl()
    {
	if (isInBookMode())
	    return res.book.getStartingDocument().getUrl();
	return res.doc.getUrl();
    }
    */



    /*
    String getDocHash()
    {
	if (!hasDocument())
	    return "";
	final String res = getDocument().getProperty("hash");
	return res != null?res:"";
    }
    */

    /*
    String getContentType()
    {
	if (!hasDocument())
	    return "";
	final String r = res.doc.getProperty("contenttype");
	return r != null?r:"";
    }
    */

    Document getDocument()
    {
	return this.doc;
    }

    Book.Section[] getSections()
    {
	return this.book.getBookSections();
    }
}

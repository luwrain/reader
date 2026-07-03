// SPDX-License-Identifier: BUSL-1.1
// Copyright 2012-2026 Michael Pozhidaev <msp@luwrain.org>

package org.luwrain.app.reader;

import java.util.*;
import java.io.*;
import java.net.*;

import org.luwrain.core.*;
import org.luwrain.controls.reader.*;
import org.luwrain.io.bookdoc.*;
import org.luwrain.io.bookdoc.view.*;
import org.luwrain.app.reader.books.*;
import org.luwrain.player.*;
import org.luwrain.app.base.*;

import org.luwrain.app.reader.books.Book;

import static java.util.Objects.*;

final class BookContainer
{
    private final App app;
    private final Book book;
    final String bookId;
    final Notes notes;
    private final LinkedList<HistoryItem> history = new LinkedList<>();
    private Book.Section[] sections = new Book.Section[0];
    private Doc doc = null;

    BookContainer(App app, Book book, String bookId)
    {
	requireNonNull(app, "app can't be null");
	requireNonNull(book, "book can't be null");
	NullCheck.notEmpty(bookId, "bookId");
	this.app = app;
	this.book = book;
	this.bookId = bookId;
	this.notes = new Notes(app, bookId);
	try {
	    this.doc = book.getDefaultDocument();
	}
	catch (Exception e)
	{
	    this.doc = null;
	    app.showErrorLayout(e);
	    return;
	}
	if (doc != null)
	{
	    final String title = doc.getProperty(Doc.PROP_TITLE);
	    app.setAppName(title != null ? title : "");
	}
    }

    boolean jump(String href, ReaderArea readerArea, int newRowNum, Runnable onSuccess)
    {
	NullCheck.notEmpty(href, "href");
	requireNonNull(readerArea, "readerArea can't be null");
	requireNonNull(onSuccess, "onSuccess can't be null");
	if (app.isBusy())
	    return false;
	final App.TaskId taskId = app.newTaskId();
	return app.runTask(taskId, ()->{	
		final Doc doc;
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
		    history.add(new HistoryItem(this.doc));
		    final int currentRowNum = readerArea.getCurrentRowIndex();
		    if (currentRowNum >= 0)
			history.getLast().lastRowIndex = currentRowNum;
		}
		if (newRowNum >= 0)
		    doc.setProperty(View.DEFAULT_ITERATOR_INDEX_PROPERTY, String.valueOf(newRowNum));
		app.finishedTask(taskId, ()->{
			this.doc = doc;
			final String title = doc.getProperty(Doc.PROP_TITLE);
			this.app.setAppName(title != null ? title : "");
			onSuccess.run();
		    });
	    });
    }

    boolean onPrevDoc(Runnable onSuccess)
    {
	if (history.isEmpty())
	    return false;
	final HistoryItem item = history.pollLast();
	this.doc = item.doc;
	this.doc.setProperty(View.DEFAULT_ITERATOR_INDEX_PROPERTY, String.valueOf(item.lastRowIndex));
	final String title = doc.getProperty(Doc.PROP_TITLE);
	this.app.setAppName(title != null ? title : "");
	onSuccess.run();
	return true;
    }

    boolean changeCharset(String newCharset)
    {
	requireNonNull(newCharset, "newCharset can't be null");
	return true;
    }

    boolean playAudio(org.luwrain.controls.reader.ReaderArea area, String[] ids)
    {
	requireNonNull(area, "area can't be null");
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

    Doc getDocument()
    {
	return this.doc;
    }

    Set<Book.Flags> getBookFlags()
    {
	return this.book.getBookFlags();
    }

    Book.Section[] getSections()
    {
	final Book.Section[] res = this.book.getBookSections();
	return res != null?res:new Book.Section[0];
    }

    static final class HistoryItem
    {
	final Doc doc;
	final String url;
	final String contentType;
	final String format;
	final String charset;
	int startingRowIndex;
	int lastRowIndex;
	HistoryItem(Doc doc)
	{
	    requireNonNull(doc, "doc can't be null");
	    this.doc = doc;
	    url = doc.getProperty(Doc.PROP_URL);
	    contentType = doc.getProperty("contenttype");
	    charset = doc.getProperty("charset");
	    format = doc.getProperty("format");
	}
    }
}

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
import java.util.concurrent.*;
import java.net.*;
import java.io.*;
import java.nio.file.*;

import org.luwrain.core.*;
import org.luwrain.controls.*;
import org.luwrain.popups.Popups;
import org.luwrain.doctree.*;
import org.luwrain.controls.doctree.*;
import org.luwrain.doctree.loading.*;
import org.luwrain.player.*;

class Base
{
    static private final String DEFAULT_ENCODING = "UTF-8";

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Luwrain luwrain;
    private final Strings strings;
    private FutureTask task;
    private AudioPlaying audioPlaying;
    private BookTreeModelSource bookTreeModelSource;
    private CachedTreeModel bookTreeModel;
    private final ListUtils.FixedModel notesModel = new ListUtils.FixedModel();

    private Book book;
    private Document currentDoc = null;
    private final LinkedList<HistoryItem> history = new LinkedList<HistoryItem>();

    Base(Luwrain luwrain, Strings strings)
    {
	NullCheck.notNull(luwrain, "luwrain");
	NullCheck.notNull(strings, "strings");
	this.luwrain = luwrain;
	this.strings = strings;
	this.audioPlaying = new AudioPlaying();
	if (!audioPlaying.init(luwrain))
	{
	    Log.warning("reader", "unable to initialize audio playing (likely no system player), no audio listening is available");
	    audioPlaying = null;
	}
	bookTreeModelSource = new BookTreeModelSource(strings.bookTreeRoot(), new Book.Section[0]);
	bookTreeModel = new CachedTreeModel(bookTreeModelSource);
    }

    boolean open(ReaderApp app, URL url, String contentType)
    {
	NullCheck.notNull(app, "app");
	NullCheck.notNull(url, "url");
	NullCheck.notNull(contentType, "contentType");
	if (isInBookMode())
	{
	    luwrain.launchApp("reader", new String[]{url.toString()});
	    return true;
	}
	if (task != null && !task.isDone())
	    return false;
	final int currentRowIndex = app.getCurrentRowIndex();
	if (!history .isEmpty() && currentRowIndex >= 0)
	    history.getLast().startingRowIndex = currentRowIndex;
	task = createTask(app, url, contentType);
	Log.debug("reader", "executing new task for fetching " + url);
	executor.execute(task);
	return true;
    }

    boolean jumpByHrefInNonBook(ReaderApp app, String href)
    {
	NullCheck.notNull(app, "app");
	NullCheck.notEmpty(href, "href");
	if (isInBookMode() || fetchingInProgress())
	    return false;
	final URL url;
	try {
	    url = new URL(href);
	}
	catch(MalformedURLException e)
	{
	    luwrain.message(strings.badUrl() + href, Luwrain.MessageType.ERROR);
	    return true;
	}
	if (!open(app, url, ""))
	    return false;
	luwrain.message(strings.fetching() + " " + href);
	return true;
    }

    boolean onPrevDocInNonBook(ReaderApp app)
    {
	NullCheck.notNull(app, "app");
	if (isInBookMode() || fetchingInProgress())
	    return false;
	if (history.size() < 2)
	    return false;
	history.pollLast();
	final HistoryItem item = history.pollLast();
	final URL url;
	try {
	    url = new URL(item.url);
	}
	catch(MalformedURLException e)
	{
	    luwrain.message(strings.badUrl() + item.url, Luwrain.MessageType.ERROR);
	    return true;
	}
	if (!open(app, url, ""))
	    return false;
	luwrain.message(strings.fetching() + " " + item.url);
	return true;
    }

    Document jumpByHrefInBook(String href, int lastPos, int newDesiredPos)
    {
	NullCheck.notEmpty(href, "href");
	if (!isInBookMode() || fetchingInProgress())
	    return null;
	final Document doc = book.getDocument(href);
	if (doc == null)
	    return null;
	if (doc != currentDoc)
	{
	    if (lastPos >= 0 && !history.isEmpty())
		history.getLast().lastRowIndex = lastPos;
	    history.add(new HistoryItem(doc));
	}
	this.currentDoc = doc;
	if (newDesiredPos >= 0)
	    currentDoc.setProperty(Document.DEFAULT_ITERATOR_INDEX_PROPERTY, "" + newDesiredPos);
	//	doc.commit();
	return doc;
    }

    Document onPrevDocInBook()
    {
	if (!isInBookMode() || fetchingInProgress())
	    return null;
	if (history.size() < 2)
	    return null;
	history.pollLast();
	final HistoryItem item = history.getLast();
	final Document doc = book.getDocument(item.url);
	if (doc == null)
	    return null;
	this.currentDoc = doc;
	if (item.lastRowIndex >= 0)
	    this.currentDoc.setProperty(Document.DEFAULT_ITERATOR_INDEX_PROPERTY, "" + item.lastRowIndex);
	return doc;
    }

    //Returns the document to be shown in readerArea
    Document acceptNewSuccessfulResult(Book book, Document doc)
    {
	if (book != null && this.book != book)
	{
	    //Opening new book
	    Log.debug("reader", "new book detected, opening");
	    this.book = book;
	    bookTreeModelSource.setSections(book.getBookSections());
	    Log.debug("doctree", "" + book.getBookSections().length + " book section provided");
	    this.currentDoc = book.getStartingDocument();
	    history.clear();
	} else
	    this.currentDoc = doc;
	NullCheck.notNull(currentDoc, "currentDoc");
	history.add(new HistoryItem(currentDoc));
	if (currentDoc.getProperty("url").matches("http://www\\.google\\.ru/search.*"))
	    Visitor.walk(currentDoc.getRoot(), new org.luwrain.app.reader.filters.GDotCom());
	final int savedPosition = Settings.getBookmark(luwrain.getRegistry(), currentDoc.getUrl().toString());
	if (savedPosition > 0)
	    currentDoc.setProperty(Document.DEFAULT_ITERATOR_INDEX_PROPERTY, "" + savedPosition);
	currentDoc.commit();
	return currentDoc;
    }

    private FutureTask createTask(ReaderApp app, URL url, String contentType)
    {
	NullCheck.notNull(app, "app");
	NullCheck.notNull(url, "url");
    NullCheck.notNull(contentType, "contentType");
	return new FutureTask(()->{
		try {
		    final UrlLoader urlLoader = new UrlLoader(url, contentType);
		    final UrlLoader.Result res = urlLoader.load();
		    Log.debug("reader", "UrlLoader finished");
		if (res != null)
		{
		    Log.debug("reader", "UrlLoader result not null, sending back to the application");
		    luwrain.runInMainThread(()->app.onNewResult(res));
		}
		}
		catch(Exception e)
		{
		    Log.error("reader", "unable to fetch " + url + ":" + e.getClass().getName() + ":" + e.getMessage());
		    luwrain.crash(e);
		}
	}, null);
    }

    void prepareErrorText(UrlLoader.Result res, MutableLines lines)
    {
	lines.addLine(strings.errorAreaIntro());
	switch(res.type())
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

    boolean playAudio(DoctreeArea area, String[] ids)
    {
	NullCheck.notNull(area, "area");
	NullCheck.notNullItems(ids, "ids");
	if (!isInBookMode())
	    return false;
	if (audioPlaying == null)
	return false;
	return audioPlaying.playAudio(book, currentDoc, area, ids);
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
	    return book.getStartingDocument().getUrl();
	return currentDoc.getUrl();
    }

    void updateNotesModel()
    {
	if (currentDoc == null && book == null)
	    return;
	final URL url = getNotesUrl();
	notesModel.setItems(Settings.getNotes(luwrain.getRegistry(), url.toString()));
    }

    boolean addNote(int pos)
    {
	NullCheck.notNull(currentDoc, "currentDoc");
	final String text = Popups.simple(luwrain, strings.addNotePopupName(), strings.addNotePopupPrefix(), "");
	if (text == null)
	    return false;
	Settings.addNote(luwrain.getRegistry(), getNotesUrl().toString(), currentDoc.getUrl().toString(), pos, text, "");
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
	return currentDoc != null;
    }

    Document currentDoc()
    {
	return currentDoc;
    }

    boolean fetchingInProgress()
    {
	return task != null && !task.isDone();
    }

    boolean isInBookMode()
    {
	return book != null;
    }

    String getCurrentContentType()
    {
	if (currentDoc == null)
	    return "";
	final String res = currentDoc.getProperty("contenttype");
	return res != null?res:"";
    }

    URL getCurrentUrl()
    {
	return currentDoc != null?currentDoc.getUrl():null;
    }

    boolean openInNarrator()
    {
	final NarratorTextVisitor visitor = new NarratorTextVisitor();
	Visitor.walk(currentDoc.getRoot(), visitor);
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

    static String getHref(DoctreeArea area)
    {
	NullCheck.notNull(area, "area");
	final Run run = area.getCurrentRun();
	if (run == null)
	    return "";
	return run.href();//Never returns null
    }

    static boolean hasHref(DoctreeArea area)
    {
	NullCheck.notNull(area, "area");
	return !getHref(area).isEmpty();
    }
}

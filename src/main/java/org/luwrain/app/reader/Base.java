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
import org.luwrain.doctree.loading.*;
import org.luwrain.player.*;

class Base
{
    static private final String DEFAULT_ENCODING = "UTF-8";

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private FutureTask task;
    private Luwrain luwrain;
    private Strings strings;
    private AudioPlaying audioPlaying = null;
    private BookTreeModelSource bookTreeModelSource;
    private CachedTreeModel bookTreeModel;
    private final FixedListModel notesModel = new FixedListModel();

    private Book book;
    private Document currentDoc = null;
    private final LinkedList<HistoryItem> history = new LinkedList<HistoryItem>();

    private final LinkedList<String> enteredUrls = new LinkedList<String>();

    boolean init(Luwrain luwrain, Strings strings)
    {
	NullCheck.notNull(luwrain, "luwrain");
	NullCheck.notNull(strings, "strings");
	this.luwrain = luwrain;
	this.strings = strings;
	audioPlaying = new AudioPlaying();
	if (!audioPlaying.init(luwrain))
	{
	    Log.warning("reader", "unable to initialize audio playing (likely no system player), no audio listening is available");
	    audioPlaying = null;
	}
	bookTreeModelSource = new BookTreeModelSource(strings.bookTreeRoot(), new Book.Section[0]);
	bookTreeModel = new CachedTreeModel(bookTreeModelSource);
	return true;
    }

    boolean open(ReaderApp app, URL url, String contentType)
    {
	NullCheck.notNull(app, "app");
	NullCheck.notNull(url, "url");
	NullCheck.notNull(contentType, "contentType");
	if (task != null && !task.isDone())
	    return false;
	final int currentRowIndex = app.getCurrentRowIndex();
	if (!history .isEmpty() && currentRowIndex >= 0)
	    history.getLast().startingRowIndex = currentRowIndex;
	task = createTask(app, url, contentType);
	executor.execute(task);
	return true;
    }

    boolean openInNarrator()
    {
	final NarratorTextVisitor visitor = new NarratorTextVisitor();
	Visitor.walk(currentDoc.getRoot(), visitor);
	luwrain.launchApp("narrator", new String[]{"--TEXT", visitor.toString()});
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
	    luwrain.message(strings.badUrl() + href, Luwrain.MESSAGE_ERROR);
	    return true;
	}
	if (!open(app, url, ""))
	    return false;
	luwrain.message(strings.fetching() + " " + href);
	return true;
    }

    Document jumpByHrefInBook(String href, int width)
    {
	NullCheck.notEmpty(href, "href");
	Log.debug("reader", "opening href in book mode:" + href);
	if (!isInBookMode() || fetchingInProgress())
	    return null;
	final Document doc = book.getDocument(href);
	if (doc == null)
	    return null;
	Log.debug("reader", "book provided new document for this href");
	this.currentDoc = doc;
	this.currentDoc.buildView(width);
	history.add(new HistoryItem(doc));
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
	final Document doc = book.getDocument(item.url());
	if (doc == null)
	    return null;
	this.currentDoc = doc;
	return doc;
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
	URL url = null;
	try {
	    url = new URL(item.url());
	}
	catch(MalformedURLException e)
	{
	    luwrain.message(strings.badUrl() + item.url(), Luwrain.MESSAGE_ERROR);
	    return true;
	}
	if (!open(app, url, ""))
	    return false;
	luwrain.message(strings.fetching() + " " + item.url());
	return true;
    }

    //Returns the document to be shown in readerArea
    Document acceptNewSuccessfulResult(Book book, Document doc,
				       int docWidth)
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
	System.out.println("saved:" + savedPosition);
	if (savedPosition > 0)
	    currentDoc.setProperty(Document.DEFAULT_ITERATOR_INDEX_PROPERTY, "" + savedPosition);
	try {
	    currentDoc.buildView(docWidth);
	}
	catch(Exception e)
	{
	    luwrain.crash(e);
	}
	return currentDoc;
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
	lines.addLine(strings.propertiesAreaUrl(item.url()));
		      lines.addLine(strings.propertiesAreaContentType(item.contentType()));
				    lines.addLine(strings.propertiesAreaFormat(item.format()));
						  lines.addLine(strings.propertiesAreaCharset(item.charset()));
	lines.addLine("");
	lines.endLinesTrans();

	/*
	    lines.addLine(strings.infoPageField("title") + ": " + currentDoc.getTitle());
	    lines.addLine(strings.infoPageField("url") + ": " + currentDoc.getUrl());
	*/

	    /*
	final Map<String, String> attr = currentDoc.getInfoAttr();
	for(Map.Entry<String, String> e: attr.entrySet())
	    if (!strings.infoPageField(e.getKey()).isEmpty())
		lines.addLine(strings.infoPageField(e.getKey()) + ": " + e.getValue());
	*/
	return true;
    }

    boolean openNew(ReaderApp app, boolean openUrl, String currentHref)
    {
	NullCheck.notNull(app, "app");
	if (fetchingInProgress())
	    return false;
	if (openUrl)
	{
	    final String res = Popups.fixedEditList(luwrain, strings.openUrlPopupName(), strings.openUrlPopupPrefix(), currentHref.isEmpty()?"http://":currentHref, 
						    enteredUrls.toArray(new String[enteredUrls.size()]));
	    if (res == null)
		return true;
	    enteredUrls.add(res);
	    URL url;
	    try {
		url = new URL(res);
	    }
	    catch(MalformedURLException e)
	    {
		e.printStackTrace(); 
		luwrain.message(strings.badUrl() + res, Luwrain.MESSAGE_ERROR);
		return true;
	    }
	    open(app, url, "");
	    return true;
	}
	final Path path = Popups.path(luwrain, strings.openPathPopupName(), strings.openPathPopupPrefix(),
				      luwrain.getPathProperty("luwrain.dir.userhome"),
				      (pathToCheck)->{
					  if (Files.isDirectory(pathToCheck))
					  {
					      luwrain.message(strings.pathToOpenMayNotBeDirectory(), Luwrain.MESSAGE_ERROR);
					      return false;
					  }
					  return true;
				      });
	return false;
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
		if (res != null)
		    luwrain.runInMainThread(()->app.onNewResult(res));
		}
		catch(IOException e)
		{
		    luwrain.crash(e);
		}
	}, null);
    }

    boolean playAudio(DoctreeArea area, String[] ids)
    {
	return false;
    }

    boolean addNote()
    {
	if (!isInBookMode() || !hasDocument())
	    return false;
	final String text = Popups.simple(luwrain, strings.addNotePopupName(), strings.addNotePopupPrefix(), "");
	if (text == null)
	    return false;
	final Book.Note note = book.createNote(currentDoc, 0);
	if (note == null)
	    return false;
	note.setText(text);
	book.addNote(note);
	notesModel.setItems(book.getNotes());
	return true;
    }

    boolean jumpByNote(ReaderApp app, Book.Note note)
    {
	NullCheck.notNull(app, "app");
	NullCheck.notNull(note, "note");
	if (!isInBookMode())
	    return false;
final String href = book.getHrefOfNoteDoc(note);
if (href.isEmpty())
    return false;
return jumpByHrefInNonBook(app, href);
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

static private class HistoryItem
{
    String url;
    private String contentType;
    private String format;
    private String charset;
    int startingRowIndex;

    HistoryItem(Document doc)
    {
	NullCheck.notNull(doc, "doc");
	url = doc.getProperty("url");
	contentType = doc.getProperty("contenttype");
	charset = doc.getProperty("charset");
	format = doc.getProperty("format");
    }

    String url() {return url;}
    String contentType() {return contentType;}
    String charset() {return charset;}
    String format() {return format;}
}
}

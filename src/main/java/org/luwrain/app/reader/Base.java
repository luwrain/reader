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

class Base implements Listener
{
    static private final String DEFAULT_ENCODING = "UTF-8";

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private FutureTask task;
    private Luwrain luwrain;
    private Strings strings;
    private Player player;
    private AudioFollowingHandler audioFollowingHandler = null;
    private Playlist currentPlaylist = null;
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
	player = (Player)luwrain.getSharedObject(Player.SHARED_OBJECT_NAME);
	if (player != null)
	    player.addListener(this); else
	    Log.warning("reader", "player object is null, no audio listening is available");
	bookTreeModelSource = new BookTreeModelSource(strings.bookTreeRoot(), new Book.Section[0]);
	bookTreeModel = new CachedTreeModel(bookTreeModelSource);
	return true;
    }

    boolean open(Actions actions, URL url, String contentType)
    {
	NullCheck.notNull(actions, "actions");
	NullCheck.notNull(url, "url");
	NullCheck.notNull(contentType, "contentType");
	if (task != null && !task.isDone())
	    return false;
	final int currentRowIndex = actions.getCurrentRowIndex();
	if (!history .isEmpty() && currentRowIndex >= 0)
	    history.getLast().startingRowIndex = currentRowIndex;
	task = createTask(actions, url, contentType);
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

    Document jumpByHrefInBook(String href)
    {
	NullCheck.notEmpty(href, "href");
	if (!isInBookMode() || fetchingInProgress())
	    return null;
	final Document doc = book.getDocument(href);
	if (doc == null)
	    return null;
	this.currentDoc = doc;
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

    boolean jumpByHrefInNonBook(Actions actions, String href)
    {
	NullCheck.notNull(actions, "actions");
	NullCheck.notEmpty(href, "href");
	if (isInBookMode() || fetchingInProgress())
	    return false;
	URL url = null;
	try {
	    url = new URL(href);
	}
	catch(MalformedURLException e)
	{
	    luwrain.message(strings.badUrl() + href, Luwrain.MESSAGE_ERROR);
	    return true;
	}
	if (!open(actions, url, ""))
	    return false;
	luwrain.message(strings.fetching() + " " + href);
	return true;
    }

    boolean onPrevDocInNonBook(Actions actions)
    {
	NullCheck.notNull(actions, "actions");
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
	if (!open(actions, url, ""))
	    return false;
	luwrain.message(strings.fetching() + " " + item.url());
	return true;
    }


    private FutureTask createTask(Actions actions, URL url, String contentType)
    {
	NullCheck.notNull(actions, "actions");
	NullCheck.notNull(url, "url");
    NullCheck.notNull(contentType, "contentType");
	return new FutureTask(()->{
		try {
		    final UrlLoader urlLoader = new UrlLoader(url, contentType);
		    final UrlLoader.Result res = urlLoader.load();
		if (res != null)
		    luwrain.runInMainThread(()->actions.onNewResult(res));
		}
		catch(IOException e)
		{
		    luwrain.crash(e);
		}
	}, null);
    }

    //Returns the document to be shown in readerArea
    Document acceptNewSuccessfulResult(Book book, Document doc,
				       int docWidth)
    {
	if (book != null && this.book != book)
	{
	    //Opening new book
	    this.book = book;
	    bookTreeModelSource.setSections(book.getBookSections());
	    this.currentDoc = book.getStartingDocument();
	    history.clear();
	} else
	this.currentDoc = doc;
	NullCheck.notNull(currentDoc, "currentDoc");
	history.add(new HistoryItem(doc));
	try {
	    Visitor.walk(currentDoc.getRoot(), new org.luwrain.app.reader.filters.GDotCom());
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

    boolean openNew(Actions actions, boolean openUrl, String currentHref)
    {
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
	    open(actions, url, "");
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

    private int chooseFormat()
    {
	/*
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
	*/
	return 0;
    }

    boolean anotherFormat()
    {
	return true;
    }

    boolean anotherCharset()
    {
	return true;
    }

    boolean playAudio(DoctreeArea area, String[] ids)
    {
	NullCheck.notNull(area, "area");
	NullCheck.notNullItems(ids, "ids");
	if (currentDoc == null || currentDoc.getUrl() == null)
	{
	    Log.debug("reader", "no current document or no associated URL to play audio by IDs");
	    return false;
	}
	    final URL url = currentDoc.getUrl();
	    for(String id: ids)
	    {
		final AudioInfo audioInfo = book.findAudioForId(url.toString() + "#" + id);
		if (audioInfo != null)
		{
		    audioFollowingHandler = new AudioFollowingHandler(area);
		    currentPlaylist = new SingleLocalFilePlaylist(audioInfo.src());
		    player.play(currentPlaylist, 0, audioInfo.beginPosMsec());
		    //		    luwrain.message("" + audioInfo.startPosMsec());
		}
		break;
	    }
	    return true;
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

    boolean jumpByNote(Actions actions, Book.Note note)
    {
	NullCheck.notNull(note, "note");
	if (!isInBookMode())
	    return false;
final String href = book.getHrefOfNoteDoc(note);
if (href.isEmpty())
    return false;
return jumpByHrefInNonBook(actions, href);
    }

    @Override public void onNewPlaylist(Playlist playlist)
    {
    }

    @Override public void onNewTrack(Playlist playlist, int trackNum)
    {
    }

    @Override public void onTrackTime(Playlist playlist, int trackNum,  long msec)
    {
	if (!isInBookMode())
	    return;
	if (currentDoc == null)
	{
	    Log.warning("reader", "player listening notification with currentDoc equal to null");
	    return;
	}
	if (currentDoc.getUrl() == null)
	{
	    Log.warning("reader", "player listening notification with the URL of the current document equal to null");
	    return;
	}
	    if (playlist != currentPlaylist)
		return;
	if (playlist.getPlaylistItems() == null || trackNum >= playlist.getPlaylistItems().length)
	    return;
	final String track = playlist.getPlaylistItems()[trackNum];
	final String link = book.findTextForAudio(track, msec);
	if (link == null)
	    return;
	URL url = null;
	URL docUrl = null;
	try {
	    url = new URL(link);
	    docUrl = new URL(url.getProtocol(), url.getHost(), url.getPort(), url.getFile());
	}
	catch(MalformedURLException e)
	{
	    e.printStackTrace();
	    return;
	}
	if (!currentDoc.getUrl().equals(docUrl))
	    return;
	if (url.getRef().isEmpty())
	{
	    Log.warning("reader", "the book provides the link to corresponding text with an empty \'id\' on audio listening");
	    return;
	}
	final AudioFollowingVisitor visitor = new AudioFollowingVisitor(url.getRef());
	Visitor.walk(currentDoc.getRoot(), visitor);
	final Run resultingRun = visitor.result();
	if (resultingRun == null)
	    return;
	if (audioFollowingHandler.prevRun == resultingRun)
	    return;
	//	luwrain.message(resultingRun.toString());
	luwrain.runInMainThread(()->audioFollowingHandler.area.findRun(resultingRun));
	audioFollowingHandler.prevRun = resultingRun;
    }

    @Override public void onPlayerStop()
    {
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

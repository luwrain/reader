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
//import java.io.*;
//import java.nio.file.*;

import org.luwrain.core.*;
import org.luwrain.controls.*;
import org.luwrain.popups.Popups;
import org.luwrain.doctree.*;
import org.luwrain.player.*;

class Base implements Listener
{
    static private final String DEFAULT_ENCODING = "UTF-8";

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
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
    private FutureTask task;

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

    TreeArea.Model getTreeModel()
    {
	return bookTreeModel;
    }

    public ListArea.Model getNotesModel()
    {
	return notesModel;
    }

    boolean fetch(Actions actions, DocInfo docInfo)
    {
	NullCheck.notNull(actions, "actions");
	NullCheck.notNull(docInfo, "docInfo");
	if (task != null && !task.isDone())
	    return false;
	task = createTask(actions, docInfo);
	executor.execute(task);
	return true;
    }

    void openInNarrator()
    {
	final NarratorTextVisitor visitor = new NarratorTextVisitor();
	Visitor.walk(currentDoc.getRoot(), visitor);
	luwrain.launchApp("narrator", new String[]{"--TEXT", visitor.toString()});
    }

    boolean jumpByHref(Actions actions, String href)
    {
	NullCheck.notNull(actions, "actions");
	NullCheck.notNull(href, "href");
	if (fetchingInProgress())
	    return false;
	if (isInBookMode())
	{
	    final Document doc = book.getDocument(href);
	    if (doc == null)
	    {
		luwrain.launchApp("reader", new String[]{href});
		return true;
	    }
	    setDocument(actions, doc);
	    return true;
	}
	URL url = null;
	try {
	    url = new URL(href);
	}
	    catch(MalformedURLException e)
	    {
		e.printStackTrace();
		luwrain.message(strings.badUrl(href), Luwrain.MESSAGE_ERROR);
		return true;
	    }
	    if (!fetch(actions, new DocInfo(url)))
		return false;
	    luwrain.message(strings.fetching(href));
	    return true;
	}

    boolean fetchingInProgress()
    {
	return task != null && !task.isDone();
    }

    private FutureTask createTask(Actions actions, DocInfo docInfo)
    {
	NullCheck.notNull(actions, "actions");
	NullCheck.notNull(docInfo, "docInfo");
	return new FutureTask(()->{
		Result res = null;
		try {
		    switch(docInfo.type)
		    {
		    case URL:
			res = Factory.fromUrl(docInfo.url, docInfo.contentType, docInfo.charset);
			break;
		    case PATH:
			res = Factory.fromPath(docInfo.path, docInfo.contentType, docInfo.charset);
			break;
		    }
		}
		catch(Exception e)
		{
		    luwrain.crash(e);
		}
		final Result finalRes = res;
		if (finalRes != null)
		    luwrain.runInMainThread(()->actions.onNewResult(finalRes));
	}, null);
    }

    Document acceptNewSuccessfulResult(Result res)
    {
	NullCheck.notNull(res, "res");
	if (res.book() != null)
	{
	    book = res.book();
	    bookTreeModelSource.setSections(book.getBookSections());
	    currentDoc = book.getStartingDocument();
	    return currentDoc;
	}
	currentDoc = res.doc();
	//	bookTreeModelSource.setDocuments(new Document[]{currentDoc});
	return currentDoc;
    }

    boolean isInBookMode()
    {
	return book != null;
    }

    Document currentDoc()
    {
	return currentDoc;
    }

    void prepareErrorText(Result res, MutableLines lines)
    {
	lines.addLine("Code: " + res.type().toString());
	if (res.type() == Result.Type.HTTP_ERROR)
	    lines.addLine("HTTP code: " + res.code());
	lines.addLine("Address: " + res.resultAddr());
	lines.addLine("Format: " + res.format());
	lines.addLine("Charset: " + res.charset());
	lines.addLine("");
    }

    void prepareDocInfoText(MutableLines lines)
    {
	NullCheck.notNull(lines, "lines");
	if (currentDoc == null)
	    return;
	if (currentDoc.getTitle() != null)
	    lines.addLine(strings.infoPageField("title") + ": " + currentDoc.getTitle());

	if (currentDoc.getUrl() != null)
	    lines.addLine(strings.infoPageField("url") + ": " + currentDoc.getUrl());


	final Map<String, String> attr = currentDoc.getInfoAttr();
	for(Map.Entry<String, String> e: attr.entrySet())
	    if (!strings.infoPageField(e.getKey()).isEmpty())
		lines.addLine(strings.infoPageField(e.getKey()) + ": " + e.getValue());
    }


    boolean openNew(boolean url, String currentHref)
    {
	if (url)
	{
	    final String res = Popups.simple(luwrain, "Открыть URL", "Введите адрес ссылки:", currentHref);
	    return true;
	}
	/*
	if (!openPopup && !hasHref())
	    return false;
	String href = hasHref()?getHref():"";
	if (openPopup)
	{
	    final String res = Popups.simple(luwrain, "Открыть URL", "Введите адрес ссылки:", href);
	    if (res == null)
		return true;
	    href = res;
	}
	return actions.jumpByHref(href);
	*/
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

    boolean playAudio(DocTreeArea area, String[] ids)
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

    void setDocument(Actions actions, Document doc)
    {
	NullCheck.notNull(actions, "actions");
	NullCheck.notNull(doc, "doc");
	doc.buildView(actions.getReaderAreaVisibleWidth());
	actions.setDocument(doc);
	currentDoc = doc;
	luwrain.silence();
	luwrain.playSound(Sounds.INTRO_REGULAR);
	luwrain.say(doc.getTitle());
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
}

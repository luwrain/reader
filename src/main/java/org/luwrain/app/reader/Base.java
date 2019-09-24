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
import java.util.concurrent.*;
import java.util.function.*;
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

final class Base
{
    static final String LOG_COMPONENT = "reader";
    static private final String DEFAULT_ENCODING = "UTF-8";

enum ParaStyle {
	EMPTY_LINES,
	EACH_LINE,
	INDENT};


    final Luwrain luwrain;
    final Strings strings;
        private final AudioPlaying audioPlaying;

        private final Runnable successNotification;
    private final Runnable newBookNotification;
    private final BiConsumer errorHandler;

    private FutureTask task = null;
        private final LinkedList<HistoryItem> history = new LinkedList();

    private UrlLoader.Result res = null;
        private StoredProperties storedProps = null;
        private Book.Section[] sections = new Book.Section[0];
    private final ListUtils.FixedModel notesModel = new ListUtils.FixedModel();

    Base(Luwrain luwrain, Strings strings,
	 Runnable successNotification, Runnable newBookNotification, BiConsumer errorHandler)
    {
	NullCheck.notNull(luwrain, "luwrain");
	NullCheck.notNull(strings, "strings");
	NullCheck.notNull(successNotification, "successNotification");
	NullCheck.notNull(newBookNotification, "newBookNotification");
	NullCheck.notNull(errorHandler, "errorHandler");
	this.luwrain = luwrain;
	this.strings = strings;
	this.successNotification = successNotification;
	this.newBookNotification = newBookNotification;
	this.errorHandler = errorHandler;
	final AudioPlaying a = new AudioPlaying(luwrain);
		this.audioPlaying = a.isLoaded()?a:null;
    }

    boolean openInitial(URL url, String contentType)
    {
	NullCheck.notNull(url, "url");
	NullCheck.notNull(contentType, "contentType");
	if (isInBookMode())
	{
	    luwrain.launchApp("reader", new String[]{url.toString()});
	    return true;
	}
	if (isBusy())
	    return false;
	final UrlLoader urlLoader;
	try {
	    urlLoader = new UrlLoader(luwrain, url);
	}
	catch(MalformedURLException e)
	{
	    luwrain.crash(e);
	    return false;
	    	}
	if (StoredProperties.hasProperties(luwrain.getRegistry(), url.toString()))
	{
	    final StoredProperties props = new StoredProperties(luwrain.getRegistry(), url.toString());
	    if (!props.getCharset().isEmpty())
		urlLoader.setCharset(props.getCharset());
	    	final ParaStyle paraStyle = translateParaStyle(props.getParaStyle());
		if (paraStyle != null)
		    urlLoader.setTxtParaStyle(paraStyle);
	}
	if (!contentType.isEmpty())
	    urlLoader.setContentType(contentType);
	task = createTask(urlLoader);
	luwrain.executeBkg(task);
	return true;
    }

        boolean changeCharset(String newCharset)
    {
	NullCheck.notNull(newCharset, "newCharset");
	if (isInBookMode() || isBusy() || !hasDocument())
	    return false;
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
	return true;
    }

    boolean changeTextParaStyle(ParaStyle newParaStyle)
    {
	NullCheck.notNull(newParaStyle, "newParaStyle");
	if (isInBookMode() || isBusy() || !hasDocument())
	    return false;
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
	storedProps.setParaStyle(newParaStyle.toString());
	urlLoader.setTxtParaStyle(newParaStyle);
	if (!storedProps.getCharset().isEmpty())
	    urlLoader.setCharset(storedProps.getCharset());
	task = createTask(urlLoader);
	luwrain.executeBkg(task);
	return true;
    }



    boolean jumpByHrefInNonBook(String href, int currentRowIndex)
    {
	NullCheck.notEmpty(href, "href");
	if (isInBookMode())
	    throw new RuntimeException("May not be in book mode");
	NullCheck.notNull(res.doc, "res.doc");
	    if (isBusy())
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
	history.add(new HistoryItem(res.doc));
	if (!openInitial(url, ""/*, currentRowIndex*/))
	    return false;
	luwrain.message(strings.fetching() + " " + href, Luwrain.MessageType.NONE);
	return true;
    }

    boolean onPrevDoc()
    {
	if (isBusy())
	    return false;
	if (history.isEmpty())
	    return false;
	final HistoryItem item = history.pollLast();
	    res.doc = item.doc;
successNotification.run();
	return true;
    }

    Document jumpByHrefInBook(String href, int lastPos, int newDesiredPos)
    {
	NullCheck.notEmpty(href, "href");
	if (!isInBookMode() || isBusy())
	    return null;
	final Document doc = res.book.getDocument(href);
	if (doc == null)
	    return null;
	if (doc != res.doc)
	{
	    if (lastPos >= 0 && !history.isEmpty())
		history.getLast().lastRowIndex = lastPos;
	    history.add(new HistoryItem(doc));
	}
	res.doc = doc;
	if (newDesiredPos >= 0)
	    res.doc.setProperty(Document.DEFAULT_ITERATOR_INDEX_PROPERTY, "" + newDesiredPos);
	return doc;
    }

    private FutureTask createTask(UrlLoader urlLoader)
    {
	NullCheck.notNull(urlLoader, "urlLoader");
	return new FutureTask(()->{
		try {
		    final UrlLoader.Result r = urlLoader.load();
		    if (r != null)
			luwrain.runUiSafely(()->{
				task = null; //the Strong mark that the work is done
				    onNewLoadingRes(r);
				    successNotification.run();
			    });
		}
		catch(Throwable e)
		{
		    Log.error("reader", "unable to fetch:" + e.getClass().getName() + ":" + e.getMessage());
		    final Properties props = new Properties();
		    props.setProperty("url", urlLoader.requestedUrl.toString());
		    props.setProperty("contentType", urlLoader.getContentType());
		    props.setProperty("charset", urlLoader.getCharset());
		    luwrain.runUiSafely(()->errorHandler.accept(props, e));
		}
	}, null);
    }

        private void onNewLoadingRes(UrlLoader.Result newRes)
    {
	NullCheck.notNull(newRes, "newRes");
	if (isInBookMode() && res.book != null)
	    throw new RuntimeException("Cannot open the new book being in book mode");
	this.res = newRes;
	if (res.book != null)
	{
	    this.sections = res.book.getBookSections();
	    res.doc = res.book.getStartingDocument();
	    history.clear();
	    newBookNotification.run();
	}
	NullCheck.notNull(res.doc, "res.doc");
	if (res.doc.getProperty("url").matches("http://www\\.google\\.ru/search.*"))
	    Visitor.walk(res.doc.getRoot(), new org.luwrain.app.reader.filters.GDotCom());
	if (StoredProperties.hasProperties(luwrain.getRegistry(), res.doc.getUrl().toString ()))
	{
	    this.storedProps = new StoredProperties(luwrain.getRegistry(), res.doc.getUrl().toString());
	    final int savedPosition = storedProps.getBookmarkPos();
	if (savedPosition > 0)
	    res.doc.setProperty(Document.DEFAULT_ITERATOR_INDEX_PROPERTY, "" + savedPosition);
	}
	res.doc.commit();
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

    boolean playAudio(ReaderArea area, String[] ids)
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
	return audioPlaying.stop();
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

    String getDocHash()
    {
	if (!hasDocument())
	    return "";
	final String res = getDocument().getProperty("hash");
	return res != null?res:"";
    }

        boolean hasStoredProps()
    {
	return storedProps != null;
    }

    StoredProperties getStoredProps()
	    {
	if (!hasDocument())
	    return null;
	if (storedProps == null)
	    this.storedProps = new StoredProperties(luwrain.getRegistry(), res.doc.getUrl().toString());
	return storedProps;
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

    TreeArea.Params createTreeParams(TreeArea.ClickHandler clickHandler)
    {
	NullCheck.notNull(clickHandler, "clickHandler");

	final TreeArea.Params params = new TreeArea.Params();
	params.context = new DefaultControlContext(luwrain);
	params.model = new CachedTreeModel(new BookTreeModelSource(strings.bookTreeRoot()));
	params.name = strings.treeAreaName();
	params.clickHandler = clickHandler;
	return params;
    }

    ListArea.Params createNotesListParams(ListArea.ClickHandler clickHandler)
    {
	NullCheck.notNull(clickHandler, "clickHandler");
	final ListArea.Params params = new ListArea.Params();
	params.context = new DefaultControlContext(luwrain);
	params.model = notesModel;
	params.appearance = new ListUtils.DefaultAppearance(params.context, Suggestions.LIST_ITEM);
	params.clickHandler = clickHandler;
	params.name = strings.notesAreaName();
	return params;
    }

    UrlLoader.Result getResult()
    {
	return res;
    }

    static String getHref(ReaderArea area)
    {
	NullCheck.notNull(area, "area");
	final Run run = area.getCurrentRun();
	if (run == null)
	    return "";
	return run.href();//Never returns null
    }

    static boolean hasHref(ReaderArea area)
    {
	NullCheck.notNull(area, "area");
	return !getHref(area).isEmpty();
    }

    static private ParaStyle translateParaStyle(String str)
    {
	NullCheck.notNull(str, "str");
	switch(str)
	{
	case "EMPTY_LINES":
	    return ParaStyle.EMPTY_LINES;
	case "INDENT":
	    return ParaStyle.INDENT;
	case "EACH_LINE":
	    return ParaStyle.EACH_LINE;
	default:
	    return null;
	}
    }

    private final class BookTreeModelSource implements CachedTreeModelSource
{
    private final String root;
    BookTreeModelSource(String root)
    {
	NullCheck.notNull(root, "root");
	this.root = root;
    }
    @Override public Object getRoot()
    {
	return root;
    }
    @Override public Object[] getChildObjs(Object obj)
    {
	final List res = new LinkedList();
	if (obj == root)
	{
	    for(Book.Section s: sections)
		if (s.level == 1)
		    res.add(s);
	} else
	{
	    int i = 0;
	    for(i = 0;i < sections.length;++i)
		if (sections[i] == obj)
		    break;
	    if (i < sections.length)
	    {
		final Book.Section sect = sections[i];
		for(int k = i + 1;k < sections.length;++k)
		{
		    if (sections[k].level <= sect.level)
			break;
		    if (sections[k].level == sect.level + 1)
			res.add(sections[k]);
		}
	    }
	}
	return res.toArray(new Object[res.size()]);
    }
}
}

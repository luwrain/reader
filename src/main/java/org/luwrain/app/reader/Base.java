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
import org.luwrain.doctree.*;

class Base
{
    static private final String DEFAULT_ENCODING = "UTF-8";

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private Luwrain luwrain;
    private Strings strings;
    private BookTreeModelSource bookTreeModelSource;
    private CachedTreeModel bookTreeModel;
    private final FixedListModel notesModel = new FixedListModel();
    private Result currentDoc = null;
    private FutureTask task;

    boolean init(Luwrain luwrain, Strings strings)
    {
	NullCheck.notNull(luwrain, "luwrain");
	NullCheck.notNull(strings, "strings");
	this.luwrain = luwrain;
	this.strings = strings;
	bookTreeModelSource = new BookTreeModelSource(strings.bookTreeRoot(), new Document[0]);
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

    boolean fetch(ReaderArea area, DocInfo docInfo)
    {
	NullCheck.notNull(area, "area");
	NullCheck.notNull(docInfo, "docInfo");
	if (task != null && !task.isDone())
	    return false;
	task = createTask(area, docInfo);
	executor.execute(task);
	return true;
    }

    void openInNarrator()
    {
	final NarratorTextVisitor visitor = new NarratorTextVisitor();
	Visitor.walk(currentDoc.doc().getRoot(), visitor);
	luwrain.launchApp("narrator", new String[]{"--TEXT", visitor.toString()});
    }

    boolean jumpByHref(ReaderArea area, String href)
    {
	NullCheck.notNull(area, "area");
	NullCheck.notNull(href, "href");
	if (fetchingInProgress())
	    return false;
	try {
	    if (!fetch(area, new DocInfo(new URL(href))))
		return false;
	    luwrain.message(strings.fetching(href));
	    return true;
	}
	catch(MalformedURLException e)
	{
	    e.printStackTrace();
	    luwrain.message(strings.badUrl(href), Luwrain.MESSAGE_ERROR);
	    return true;
	}
    }

    boolean fetchingInProgress()
    {
	return task != null && !task.isDone();
    }

    private FutureTask createTask(ReaderArea area, DocInfo docInfo)
    {
	NullCheck.notNull(area, "area");
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
		    e.printStackTrace();
		    luwrain.runInMainThread(()->luwrain.message(e.getMessage(), Luwrain.MESSAGE_ERROR));
		}
		final Result finalRes = res;
		if (finalRes != null)
		    luwrain.runInMainThread(()->area.onFetchedDoc(finalRes));
	}, null);
    }

    void acceptNewCurrentDoc(Result res)
    {
	NullCheck.notNull(res, "res");
	currentDoc = res;
	bookTreeModelSource.setDocuments(new Document[]{res.doc()});
    }

    Result currentDoc()
    {
	return currentDoc;
    }

    void prepareInfoText(Result res, MutableLines lines)
    {
	lines.addLine("Code: " + res.type().toString());
	if (res.type() == Result.Type.HTTP_ERROR)
	    lines.addLine("HTTP code: " + res.code());
	lines.addLine("Address: " + res.resultAddr());
	lines.addLine("Format: " + res.format());
	lines.addLine("Charset: " + res.charset());
	lines.addLine("");
    }
}

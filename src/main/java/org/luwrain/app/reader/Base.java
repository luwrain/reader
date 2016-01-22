/*
   Copyright 2012-2015 Michael Pozhidaev <michael.pozhidaev@gmail.com>

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
//import org.luwrain.util.MlReader;
import org.luwrain.doctree.*;

class Base
{
    static private final String DEFAULT_ENCODING = "UTF-8";

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private Luwrain luwrain;
    private Strings strings;
    private Document currentDoc = null;
    private FutureTask task;

    boolean init(Luwrain luwrain, Strings strings)
    {
	this.luwrain = luwrain;
	this.strings = strings;
	NullCheck.notNull(luwrain, "luwrain");
	NullCheck.notNull(strings, "strings");
	return true;
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
	Visitor.walk(currentDoc.getRoot(), visitor);
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
		Document doc = null;
		switch(docInfo.type)
		{
		case URL:
		    //		    System.out.println("url");
doc = Factory.fromUrl(docInfo.url, docInfo.contentType, docInfo.charset);
break;
		case PATH:
		    //		    System.out.println("path");
		    doc = Factory.fromPath(docInfo.path, docInfo.contentType, docInfo.charset);
break;
		}
		final Document finalDoc = doc;
		if (finalDoc != null)
		    luwrain.runInMainThread(()->area.onFetchedDoc(finalDoc)); else									 
		    luwrain.runInMainThread(()->luwrain.message("Не удалось доставить документ"));
	}, null);
    }

    void acceptNewCurrentDoc(Document doc)
    {
	NullCheck.notNull(doc, "doc");
	currentDoc = doc;
    }
}

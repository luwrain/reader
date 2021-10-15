/*
   Copyright 2012-2021 Michael Pozhidaev <msp@luwrain.org>
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

import org.luwrain.core.*;
import org.luwrain.core.events.*;
import org.luwrain.controls.*;
import org.luwrain.app.base.*;

import org.luwrain.io.api.books.v1.*;
import org.luwrain.io.api.books.v1.collection.*;

final class RemoteBooksLayout extends LayoutBase implements ListArea.ClickHandler
{
    static private final String LOG_COMPONENT = App.LOG_COMPONENT;

    private App app;
    final ListArea listArea;

    RemoteBooksLayout(App app)
    {
	super(app);
	this.app = app;
	this.listArea = new ListArea(listParams((params)->{
		    params.name = "Ваша коллекция";
		    params.model = new ListUtils.ArrayModel(()->app.getRemoteBooks());
		    params.appearance = new ListUtils.DefaultAppearance(params.context);
		    params.clickHandler = this;
		}));
	setAreaLayout(listArea, actions());
    }

    @Override public boolean onListClick(ListArea listArea, int index, Object obj)
    {
	NullCheck.notNull(obj, "obj");
	if (app.isBusy() || !(obj instanceof Book))
	    return false ;
	final Book remoteBook = (Book)obj;
	if (remoteBook.getId() == null || remoteBook.getId().isEmpty())
	{
	    app.message("Выбрананя книга не имеет идентификатора. Её невозможно загрузить для прослушивания.", Luwrain.MessageType.ERROR);
	    return true;
	}
	if (app.getLocalRepo().hasBook(remoteBook))
	{
final File mainFile = app.getLocalRepo().findDaisyMainFile(remoteBook);
if (mainFile != null)
{
app.open(mainFile.toURI());
	    return true;
}
	}
	app.cancelled = false;
	final App.TaskId taskId = app.newTaskId();
	return app.runTask(taskId, ()->{
		try {
		    final Book book = app.getBooks().book().id(remoteBook.getId()).accessToken(app.getAccessToken()).exec();
		    final File tmpFile = File.createTempFile(".lwr-reader-daisy-download-", ".zip");
		    Log.debug(LOG_COMPONENT, "starting downloading to " + tmpFile.getAbsolutePath());
		    try {
			final Download download = app.getBooks().download(book);
			try (final BufferedOutputStream os = new BufferedOutputStream(new FileOutputStream(tmpFile))) {
			    download.downloadDaisy(os, app.getBooksDownloadListener(), app.getAccessToken());
			    os.flush();
			}
			if (!app.cancelled)
			{
			    Log.debug(LOG_COMPONENT, "unpacking and saving");
			    app.getLocalRepo().addDaisy(remoteBook, tmpFile);
			} else
			    Log.debug(LOG_COMPONENT, "not saving, the downloading is cancelled");
		    }
		    finally {
			tmpFile.delete();
		    }
		}
		catch(IOException e)
		{
		    app.getLuwrain().crash(e);
		    return;
		}
		app.finishedTask(taskId, ()->{
			Log.debug(LOG_COMPONENT, "reading the newly downloaded book");
			final File mainFile = app.getLocalRepo().findDaisyMainFile(remoteBook);
			if (mainFile == null)
			{
			    app.message("Доставленная книга имеет некорректную структуру. Её воспроизведение невозможно.");
			    return;
			}
			Log.debug("proba", "opening " + mainFile.getAbsolutePath());
app.open(mainFile.toURI());
		    });
	    });
    }
}

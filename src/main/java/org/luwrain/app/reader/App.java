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
import java.net.*;
import java.io.*;

import org.luwrain.core.*;
import org.luwrain.core.events.*;
import org.luwrain.controls.*;
import org.luwrain.popups.Popups;
import org.luwrain.reader.*;
import org.luwrain.controls.reader.*;
import org.luwrain.app.reader.books.*;
import org.luwrain.player.*;
import org.luwrain.app.base.*;

final class App extends AppBase<Strings>
{
    static final String LOG_COMPONENT = "reader";
    static private final String DEFAULT_ENCODING = "UTF-8";

    boolean cancelled = false;
    private final String arg;
    private Conversations conv = null;
    private Settings sett = null;
    private LocalRepoMetadata localRepoMetadata = null;
    private LocalRepo localRepo = null;
    private AudioPlaying audioPlaying = null;
    private final org.luwrain.io.api.books.v1.Books books;
    private org.luwrain.io.api.books.v1.Book[] remoteBooks = new org.luwrain.io.api.books.v1.Book[0];
    private String accessToken = "";
    private Attributes attr = null;

    private BookContainer bookContainer = null;

    private MainLayout mainLayout = null;
    private StartingLayout startingLayout = null;
    private RemoteBooksLayout remoteBooksLayout = null;
    private LocalRepoLayout localRepoLayout = null;

    App()
    {
	this(null);
    }

    App(String arg)
    {
	super(Strings.NAME, Strings.class, "luwrain.reader");
	this.arg = arg;
	this.books = new org.luwrain.io.api.books.v1.Factory().newInstance();
    }

    @Override protected boolean onAppInit()
    {
	this.sett = Settings.create(getLuwrain());
	this.localRepoMetadata = new LocalRepoMetadata(sett);
	this.localRepo = new LocalRepo(this.localRepoMetadata);
	this.conv = new Conversations(getLuwrain(), getStrings());
	this.attr = new Attributes(getLuwrain().getRegistry());
	this.audioPlaying = new AudioPlaying(getLuwrain());
	if (!audioPlaying.isLoaded())
	    this.audioPlaying = null;
	this.startingLayout = new StartingLayout(this);
	this.remoteBooksLayout = new RemoteBooksLayout(this);
	this.localRepoLayout = new LocalRepoLayout(this);
	setAppName(getStrings().appName());
	try {
	    if (arg != null && !arg.isEmpty())
		open(new URI(arg));
	}
	catch(URISyntaxException e)
	{
	    showErrorLayout(e);
	}
	return true;
    }

    void open(URI uri)
    {
	NullCheck.notNull(uri, "uri");
	final TaskId taskId = newTaskId();
	runTask(taskId, ()->{
		final Book book;
		try {
		    book = new BookFactory().newBook(getLuwrain(), uri.toString());
		}
		catch(IOException e)
		{
		    finishedTask(taskId, ()->showErrorLayout(e));
		    return;
		}
		finishedTask(taskId, ()->{
			this.bookContainer = new BookContainer(this, book);
			this.mainLayout = new MainLayout(this);
			getLayout().setBasicLayout(mainLayout.getLayout());
			mainLayout.updateInitial();
		    });
	    });
	{
	}
    }

    boolean stopAudio()
    {
	if (this.audioPlaying == null)
	    return false;
	if (!this.audioPlaying.stop())
	    return false;
	getLuwrain().playSound(Sounds.PLAYING);
	return true;
    }

    void layout(AreaLayout layout)
    {
	NullCheck.notNull(layout, "layout");
	getLayout().setBasicLayout(layout);
    }

    Layouts layouts()
    {
	return new Layouts(){
	    @Override public void remoteBooks()
	    {
		getLayout().setBasicLayout(remoteBooksLayout.getLayout());
		remoteBooksLayout.listArea.refresh();
		getLuwrain().announceActiveArea();
	    }
	    	    @Override public void localRepo()
	    {
		getLayout().setBasicLayout(localRepoLayout.getLayout());
		localRepoLayout.listArea.refresh();
		getLuwrain().announceActiveArea();
	    }
	};
    }

    void showErrorLayout(Throwable e)
    {
	NullCheck.notNull(e, "e");
	final ErrorLayout errorLayout;
	if (mainLayout != null)
	    errorLayout = new ErrorLayout(this, e, ()->{
		    layout(mainLayout.getLayout());
		    getLuwrain().announceActiveArea();
		}); else
	    errorLayout = new ErrorLayout(this, e, null);
	layout(errorLayout.getLayout());
	getLuwrain().playSound(Sounds.ERROR);
    }

    @Override public void onCancelledTask()
    {
	this.cancelled = true;
    }

    @Override public boolean onEscape(InputEvent event)
    {
	closeApp();
	return true;
    }

    @Override public AreaLayout getDefaultAreaLayout()
    {
	return this.startingLayout.getLayout();
    }

    @Override public void closeApp()
    {
	if (audioPlaying != null)
	    audioPlaying.stop();
	super.closeApp();
    }

    @Override public void setAppName(String name)
    {
	NullCheck.notNull(name, "name");
	super.setAppName(!name.isEmpty()?name:getStrings().appName());
    }
    Conversations getConv()
    {
	return this.conv;
    }

    Attributes getAttr()
    {
	return this.attr;
    }

    AudioPlaying getAudioPlaying()
    {
	return this.audioPlaying;
    }

    org.luwrain.io.api.books.v1.Books getBooks()
    {
	return this.books;
    }

    org.luwrain.io.api.books.v1.Book[] getRemoteBooks()
    {
	return this.remoteBooks;
    }

    org.luwrain.io.api.books.v1.Download.Listener getBooksDownloadListener()
    {
	return new org.luwrain.io.api.books.v1.Download.Listener(){
	    @Override public boolean interrupting()
	    {
		return cancelled;
	    }
	    @Override public void processed(int chunkBytes, long totalBytes)
	    {
	    }
	};
    }

    void setRemoteBooks(org.luwrain.io.api.books.v1.Book[] remoteBooks)
    {
	NullCheck.notNullItems(remoteBooks, "remoteBooks");
	this.remoteBooks = remoteBooks;
    }

    String getAccessToken()
    {
	return accessToken;
    }

    void setAccessToken(String accessToken)
    {
	NullCheck.notEmpty(accessToken, "accessToken");
	this.accessToken = accessToken;
    }

    BookContainer getBookContainer()
    {
	return this.bookContainer;
    }

    LocalRepo getLocalRepo()
    {
	return localRepo;
    }

    interface Layouts
    {
	void remoteBooks();
	void localRepo();
    }
}

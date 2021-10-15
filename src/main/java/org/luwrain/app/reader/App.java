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

public final class App extends AppBase<Strings>
{
    static final String LOG_COMPONENT = "reader";
    static private final String DEFAULT_ENCODING = "UTF-8";

    boolean cancelled = false;
    private final String arg;
    private Conversations conv = null;
    private Settings sett = null;
    private LocalRepoMetadata localRepoMetadata = null;
    private LocalRepo localRepo = null;
    private Attributes attributes = null;
    private AudioPlaying audioPlaying = null;
    private final org.luwrain.io.api.books.v1.Books books;
    private org.luwrain.io.api.books.v1.Book[] remoteBooks = new org.luwrain.io.api.books.v1.Book[0];
    private String accessToken = "";

    private BookContainer bookContainer = null;

    private MainLayout mainLayout = null;
    private StartingLayout startingLayout = null;
    private RemoteBooksLayout remoteBooksLayout = null;
    private LocalRepoLayout localRepoLayout = null;

    public App() { this(null); }
    public App(String arg)
    {
	super(Strings.NAME, Strings.class, "luwrain.reader");
	this.arg = arg;
	this.books = new org.luwrain.io.api.books.v1.Factory().newInstance();
    }

    @Override protected AreaLayout onAppInit()
    {
	final Standalone standalone = new Standalone("lwr-books", "LWRBooks");
	if (standalone.isStandalone())
	{
	    this.sett = new StandaloneSettings(standalone.getDataDir());
	    this.localRepoMetadata = new LocalRepoMetadata(sett);
	    this.localRepo = new LocalRepo(this.localRepoMetadata, new File(standalone.getDataDir(), "repo"));
	} else
	{
	    this.sett = Settings.create(getLuwrain());	    
	    this.localRepoMetadata = new LocalRepoMetadata(sett);
	    this.localRepo = new LocalRepo(this.localRepoMetadata, new File(getLuwrain().getAppDataDir("luwrain.reader").toFile(), "repo"));
}
    	    	    this.attributes = new Attributes(sett);
	this.conv = new Conversations(getLuwrain(), getStrings());
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
	return startingLayout.getAreaLayout();
    }

    void open(URI uri)
    {
	NullCheck.notNull(uri, "uri");
	final TaskId taskId = newTaskId();
	runTask(taskId, ()->{
		final Book book = new BookFactory().newBook(getLuwrain(), uri.toString());
		finishedTask(taskId, ()->{
			this.bookContainer = new BookContainer(this, book, org.luwrain.util.Sha1.getSha1(uri.toString(), "UTF-8"));
			this.mainLayout = new MainLayout(this);
			setAreaLayout(mainLayout);
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


    @Override public void onException(Throwable e)
    {
	showErrorLayout(e);
    }

    void showErrorLayout(Throwable e)
    {
	NullCheck.notNull(e, "e");
	final ErrorLayout errorLayout;
	if (mainLayout != null)
	    errorLayout = new ErrorLayout(this, e, ()->{
		    setAreaLayout(mainLayout);
		    getLuwrain().announceActiveArea();
		    return true;
		}); else
	    errorLayout = new ErrorLayout(this, e, null);
	setAreaLayout(errorLayout);
	getLuwrain().playSound(Sounds.ERROR);
    }

    @Override public void onCancelledTask()
    {
	this.cancelled = true;
    }

    @Override public boolean onEscape()
    {
	closeApp();
	return true;
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

    Attributes getAttributes() { return this.attributes; }
    Conversations getConv() { return this.conv; }
    AudioPlaying getAudioPlaying() { return this.audioPlaying; }
    org.luwrain.io.api.books.v1.Books getBooks() { return this.books; }
    org.luwrain.io.api.books.v1.Book[] getRemoteBooks() { return this.remoteBooks; }

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

    Layouts layouts()
    {
	return new Layouts(){
	    @Override public void remoteBooks()
	    {
		setAreaLayout(remoteBooksLayout);
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

    interface Layouts
    {
	void remoteBooks();
	void localRepo();
    }
}

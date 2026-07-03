// SPDX-License-Identifier: BUSL-1.1
// Copyright 2012-2026 Michael Pozhidaev <msp@luwrain.org>

package org.luwrain.app.reader;

import java.util.*;
import java.net.*;
import java.io.*;

import org.luwrain.core.*;
import org.luwrain.controls.*;
import org.luwrain.controls.reader.*;
import org.luwrain.app.reader.books.*;
import org.luwrain.player.*;
import org.luwrain.app.base.*;

import static java.util.Objects.*;

public final class App extends AppBase<Strings>
{
    static final String LOG_COMPONENT = "reader";
    static private final String DEFAULT_ENCODING = "UTF-8";

    boolean cancelled = false;
    private final String arg;
    Conv conv = null;
    Config conf;
    private LocalRepo localRepo = null;
    private Attributes attributes = null;
    private AudioPlaying audioPlaying = null;

    private BookContainer bookContainer = null;

    private MainLayout mainLayout = null;
    private LocalRepoLayout localRepoLayout = null;

    public App() { this(null); }
    public App(String arg)
    {
	super(Strings.class, "luwrain.reader");
	this.arg = arg;
    }

    @Override protected AreaLayout onAppInit()
    {
	this.conf = requireNonNullElse(getLuwrain().loadConf(Config.class), new Config());
	this.localRepo = new LocalRepo(this, new File(getLuwrain().getAppDataDir("luwrain.reader").toFile(), "repo"));
	this.attributes = new Attributes(this);
	this.conv = new Conv(getLuwrain(), getStrings());
	this.audioPlaying = new AudioPlaying(getLuwrain());
	if (!audioPlaying.isLoaded())
	    this.audioPlaying = null;
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
	return localRepoLayout.getAreaLayout();
    }

    void open(URI uri)
    {
	requireNonNull(uri, "uri can't be null");
	final TaskId taskId = newTaskId();
	runTask(taskId, ()->{
		try {
		    final var book = new BookFactory().newBook(getLuwrain(), uri.toString());
		    final String bookId = org.luwrain.util.Sha1.getSha1(uri.toString(), "UTF-8");
		    finishedTask(taskId, ()->{
			    this.bookContainer = new BookContainer(this, book, bookId);
			    this.mainLayout = new MainLayout(this);
			    setAreaLayout(mainLayout);
			    mainLayout.updateInitial();
			});
		}
		catch (Exception e)
		{
		    finishedTask(taskId, ()->showErrorLayout(e));
		}
	    });
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
	requireNonNull(e, "e can't be null");
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
	requireNonNull(name, "name can't be null");
	super.setAppName(!name.isEmpty()?name:getStrings().appName());
    }

    Attributes getAttributes() { return this.attributes; }
    Conv getConv() { return this.conv; }
    AudioPlaying getAudioPlaying() { return this.audioPlaying; }

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
	    @Override public void localRepo()
	    {
		setAreaLayout(localRepoLayout);
		localRepoLayout.listArea.refresh();
		getLuwrain().announceActiveArea();
	    }
	};
    }

    interface Layouts
    {
	void localRepo();
    }
}

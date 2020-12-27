/*
   Copyright 2012-2020 Michael Pozhidaev <msp@luwrain.org>
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

    private final String arg;
    private Conversations conv = null;
    private BookContainer bookContainer = null;
        private AudioPlaying audioPlaying = null;
    private StartingLayout startingLayout = null;
    private Attributes attr = null;

    App()
    {
	this(null);
    }

    App(String arg)
    {
	super(Strings.NAME, Strings.class, "luwrain.reader");
	this.arg = arg;
    }

    @Override protected boolean onAppInit()
    {
	this.conv = new Conversations(getLuwrain(), getStrings());
	this.attr = new Attributes(getLuwrain().getRegistry());
	this.audioPlaying = new AudioPlaying(getLuwrain());
	if (!audioPlaying.isLoaded())
		this.audioPlaying = null;
	this.startingLayout = new StartingLayout(this);
		setAppName(getStrings().appName());
	if (arg != null && !arg.isEmpty())
	    open(arg);
		return true;
		    }

    void open(String url)
    {
	NullCheck.notEmpty(url, "url");
	final TaskId taskId = newTaskId();
	runTask(taskId, ()->{
		final Book book;
		try {
		    book = new BookFactory().newBook(getLuwrain(), url);
		}
		catch(IOException e)
		{
		    getLuwrain().crash(e);
		    return;
		}
		finishedTask(taskId, ()->{
			this.bookContainer = new BookContainer(this, book);
			final MainLayout mainLayout = new MainLayout(this);
			getLayout().setBasicLayout(mainLayout.getLayout());
			mainLayout.updateInitial();
		});
	    });
	{
	}
    }

    /*
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
    */


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

    void showErrorLayout(Exception e)
    {
    }

    Conversations conv()
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

    BookContainer getBookContainer()
    {
	return this.bookContainer;
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
}

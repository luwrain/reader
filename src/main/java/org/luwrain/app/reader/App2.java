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
import org.luwrain.controls.*;
import org.luwrain.popups.Popups;
import org.luwrain.reader.*;
import org.luwrain.controls.reader.*;
import org.luwrain.app.reader.books.*;
import org.luwrain.player.*;
import org.luwrain.template.*;

final class App2 extends AppBase<Strings>
{
    static final String LOG_COMPONENT = "reader";
    static private final String DEFAULT_ENCODING = "UTF-8";

enum ParaStyle {
	EMPTY_LINES,
	EACH_LINE,
	INDENT};

    private BookContainer bookContainer = null;
        private AudioPlaying audioPlaying = null;
    private StoredProperties storedProps = null;

    App2()
    {
	super(Strings.NAME, Strings.class);
    }

    @Override public boolean onAppInit()
    {
	this.audioPlaying = new AudioPlaying(getLuwrain());
	if (!audioPlaying.isLoaded())
		this.audioPlaying = null;
		return true;
		    }

    boolean openInitial(URL url, String contentType)
    {
	/*
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
	*/
	return true;
    }


    /*
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
    */

    /*
    boolean stopAudio()
    {
	if (audioPlaying == null)
	    return false;
	return audioPlaying.stop();
    }
    */

    BookContainer getBookContainer()
    {
	return this.bookContainer;
    }


    @Override public AreaLayout getDefaultAreaLayout()
    {
	return null;
    }
}

/*
   Copyright 2012-2021 Michael Pozhidaev <msp@luwrain.org>

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

package org.luwrain.app.opds;

import java.lang.reflect.*;
import java.net.*;
import java.util.*;
import java.io.*;

import com.google.gson.*;
import com.google.gson.reflect.*;

import org.luwrain.core.*;
import org.luwrain.core.events.*;
import org.luwrain.core.queries.*;
import org.luwrain.controls.*;
import org.luwrain.app.opds.Opds.Link;
import org.luwrain.app.base.*;
import org.luwrain.app.opds.Opds.Entry;

public final class App extends AppBase<Strings>
{
    static final Type LIBRARY_LIST_TYPE = new TypeToken<List<RemoteLibrary>>(){}.getType();

    private final Gson gson = new Gson();
    final List<RemoteLibrary> libraries = new ArrayList();
    final List<Entry> entries = new ArrayList<Entry>();
    final LinkedList<HistoryItem> history = new LinkedList<HistoryItem>();

    private Settings sett = null;
    private Conversations conv = null;
    private MainLayout mainLayout = null;

    public App()
    {
	super(Strings.NAME, Strings.class);
    }

    @Override protected AreaLayout onAppInit()
    {
	this.sett = Settings.create(getLuwrain());
	this.conv = new Conversations(this);
	loadLibraries();
	this.mainLayout = new MainLayout(this);
	setAppName(getStrings().appName());
	return mainLayout.getAreaLayout();
    }

    boolean open(URL url)
    {
	final TaskId taskId = newTaskId();
	return runTask(taskId, ()->{
		final Opds.Result res = Opds.fetch(url);
		finishedTask(taskId, ()->{
			if (res.error == Opds.Result.Errors.FETCHING_PROBLEM)
			{
			    message("Невозможно подключиться к серверу или данные по указанному адресу не являются правильным OPDS-каталогом", Luwrain.MessageType.ERROR);//FIXME:
			    return;
			}
			if(res.hasEntries())
			{
			    entries.clear();
			    entries.addAll(Arrays.asList(res.getEntries()));
			    history.add(new HistoryItem(url, res.getEntries()));
			    mainLayout.listArea.refresh();
			    mainLayout.listArea.reset(false);
			    mainLayout.setActiveArea(mainLayout.listArea);
			}
		    });
	    });
    }

    private void loadLibraries()
    {
	this.libraries.clear();
	final List<RemoteLibrary> res = gson.fromJson(sett.getLibraries(""), LIBRARY_LIST_TYPE);
	if (res == null)
	    return;
	final RemoteLibrary[] r = res.toArray(new RemoteLibrary[res.size()]);
	Arrays.sort(r);
	libraries.addAll(Arrays.asList(r));
    }

    void saveLibraries()
    {
	sett.setLibraries(gson.toJson(libraries));
    }

    URL opened()
    {
	return !history.isEmpty()?history.getLast().url:null;
    }

    Conversations getConv()
    {
	return conv;
    }

    @Override public boolean onEscape(InputEvent event)
    {
	NullCheck.notNull(event, "event");
	closeApp();
	return true;
    }
}

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

package org.luwrain.app.opds;

import java.util.*;
import java.net.*;
import java.util.concurrent.*;

import org.luwrain.core.*;
import org.luwrain.core.events.ThreadSyncEvent;
import org.luwrain.controls.*;
import org.luwrain.util.Opds;
import org.luwrain.util.RegistryPath;

class Base
{
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private Luwrain luwrain;
    private FutureTask task;
    private RemoteLibrary[] libraries;
    private final FixedListModel model = new FixedListModel();
    private final LinkedList<URL> history = new LinkedList<URL>();

    boolean init(Luwrain luwrain)
    {
	this.luwrain = luwrain;
	loadLibraries();
	model.setItems(libraries);
	return true;
    }

    ListArea.Model getModel()
    {
	return model;
    }

    boolean start(Area area, URL url)
    {
	NullCheck.notNull(area, "area");
	NullCheck.notNull(url, "url");
	if (task != null && !task.isDone())
	    return false;
	task = constructTask(area, url);
	executor.execute(task);
	history.add(url);
	model.clear();
	return true;
    }

    boolean returnBack(Area area)
    {
	NullCheck.notNull(area, "area");
	if (history.isEmpty() ||
	    (task != null && !task.isDone()))
	    return false;
	if (history.size() == 1)
	{
	    history.clear();
	    model.setItems(libraries);
	    return true ;
	}
	history.pollLast();
	task = constructTask(area, history.getLast());
	executor.execute(task);
	model.clear();
	return true;
    }


    boolean onReady()
    {
	Opds.Result res = null;
	try {
	    res = (Opds.Result)task.get();
	}
	catch(InterruptedException e)
	{
	    Thread.currentThread().interrupt();
	    e.printStackTrace(); 
	}
	catch(ExecutionException e)
	{
	    e.printStackTrace();
	}

	if (res == null)
	    return false;
	switch(res.error())
	{
	case FETCH:
	    luwrain.message("Каталог не может быть доставлен с сервера по причине ошибки соединения", Luwrain.MESSAGE_ERROR);
	    return false;

	case PARSE:
	    luwrain.message("Доставленные с сервера данные не являются корректным каталогом OPDS", Luwrain.MESSAGE_ERROR);
	    return false;
case NOERROR:
	    model.setItems(res.directory().entries());
luwrain.playSound(Sounds.MESSAGE_DONE);
return true;
default:
return false;
}
    }

    private FutureTask constructTask(Area destArea, URL url)
    {
	//	final Luwrain l = luwrain;
	return new FutureTask(()->{
		final Opds.Result res = Opds.fetch(url);
		luwrain.enqueueEvent(new ThreadSyncEvent(destArea));
		return res;
	});
    }

    private void loadLibraries()
    {
	libraries = new RemoteLibrary[0];
	final Registry registry = luwrain.getRegistry();
	final String dir = "/org/luwrain/app/opds/libraries";
	final String[] dirs = registry.getDirectories(dir);
	if (dirs == null || dirs.length <= 0)
	    return;
	final LinkedList<RemoteLibrary> res = new LinkedList<RemoteLibrary>();
	for(String s: dirs)
	{
	    final RemoteLibrary l = new RemoteLibrary();
	    if (l.init(registry, RegistryPath.join(dir, s)))
		res.add(l);
	}
	libraries = res.toArray(new RemoteLibrary[res.size()]);
	Arrays.sort(libraries);
    }

    boolean isFetchingInProgress()
    {
	return task != null && !task.isDone();
    }

    URL currentUrl()
    {
	return !history.isEmpty()?history.getLast():null;
    }
}

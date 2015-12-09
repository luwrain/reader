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

import java.net.*;
import java.util.concurrent.*;

import org.luwrain.core.*;
import org.luwrain.core.events.ThreadSyncEvent;
import org.luwrain.controls.*;
import org.luwrain.util.Opds;

class Base
{
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private Luwrain luwrain;
    private FutureTask task;
    private final FixedListModel model = new FixedListModel();

    boolean init(Luwrain luwrain)
    {
	this.luwrain = luwrain;
	return true;
    }

    ListArea.Model getModel()
    {
	return model;
    }

    boolean fetch(Area area, URL url)
    {
	NullCheck.notNull(area, "area");
	NullCheck.notNull(url, "url");
	if (task != null && !task.isDone())
	    return false;
	task = constructTask(area, url);
	executor.execute(task);
	return true;
    }

    void onReady()
    {
	try {
	    final Opds.Directory res = (Opds.Directory)task.get();
	    model.setItems(res.entries);
	}
	catch(InterruptedException e)
	{
	    //There should be something like currentThread().interrupt() but it's unclear is it applicable here
	    e.printStackTrace(); 
	}
	catch(ExecutionException e)
	{
	    e.printStackTrace();
	}
    }

    private FutureTask constructTask(final Area area, final URL url)
    {
	final Luwrain l = luwrain;
	return new FutureTask<Opds.Directory>(new Callable<Opds.Directory>(){
		@Override public Opds.Directory call()
		{
		    l.enqueueEvent(new ThreadSyncEvent(area));
		    return null;
		}
	    });
    }
}

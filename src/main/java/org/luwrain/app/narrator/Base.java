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

package org.luwrain.app.narrator;

import java.net.*;
import java.nio.file.*;
import java.util.concurrent.*;

import org.luwrain.core.*;
import org.luwrain.core.events.*;
//import org.luwrain.controls.*;

class Base
{
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private Luwrain luwrain;
    private Task task;
    private FutureTask futureTask;

    boolean init(Luwrain luwrain)
    {
	this.luwrain = luwrain;
	return true;
    }

    boolean start(Area destArea, String text)
    {
	if (futureTask != null && !futureTask.isDone())
	    return false;
	task = new Task(text, Paths.get("/tmp"), null){
		@Override protected void progressLine(String text)
		{
		    luwrain.enqueueEvent(new ProgressLineEvent(destArea, text));
		}
	    };
	futureTask = new FutureTask(task, null);
	executor.execute(futureTask);
	return true;
    }


    /*
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
    */
}

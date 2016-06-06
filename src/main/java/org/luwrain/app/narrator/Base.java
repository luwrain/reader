/*
   Copyright 2012-2016 Michael Pozhidaev <michael.pozhidaev@gmail.com>

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
import java.util.*;
import java.util.concurrent.*;

import org.luwrain.core.*;
import org.luwrain.core.events.*;
import org.luwrain.popups.*;
import org.luwrain.speech.*;

class Base
{
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private Luwrain luwrain;
    private Strings strings;
    private Task task;
    private FutureTask futureTask;

    boolean init(Luwrain luwrain, Strings strings)
    {
	this.luwrain = luwrain;
	this.strings = strings;
	return true;
    }

    boolean start(Area destArea, String text)
    {
	if (futureTask != null && !futureTask.isDone())
	    return false;

	if (text.trim().isEmpty())
	{
	    luwrain.message(strings.noTextToSynth(), Luwrain.MESSAGE_ERROR);
	    return true;
	}

	final Channel channel = luwrain.getAnySpeechChannelByCond(EnumSet.of(Channel.Features.CAN_SYNTH_TO_STREAM));
	if (channel == null)
	{
	    luwrain.message(strings.noChannelToSynth(), Luwrain.MESSAGE_ERROR);
	    return true;
	}
	final Path homeDir = luwrain.getPathProperty("luwrain.dir.userhome");
	final Path path = Popups.path(luwrain, 
					    strings.targetDirPopupName(), strings.targetDirPopupPrefix(), homeDir,
					    (pathArg)->{return true;});
	if (path == null)
	    return true;
	task = new Task(strings, text, path, 
			luwrain.launchContext().scriptPath("lwr-audio-compress").toString(), channel){
		@Override protected void progressLine(String text, boolean doneMessage)
		{
		    luwrain.enqueueEvent(new ProgressLineEvent(destArea, text));
		    if (doneMessage)
			luwrain.runInMainThread(()->luwrain.message(text, Luwrain.MESSAGE_DONE));
		}
	    };
	futureTask = new FutureTask(task, null);
	executor.execute(futureTask);
	return true;
    }
}

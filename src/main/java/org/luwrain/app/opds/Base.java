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

import java.util.*;
import java.net.*;
import java.util.concurrent.*;

import org.luwrain.core.*;
import org.luwrain.controls.*;
import org.luwrain.app.opds.Opds.Link;
import org.luwrain.app.opds.Opds.Entry;

final class Base
{

    private final Luwrain luwrain = null;
    private final Strings strings = null;
    private FutureTask task = null;
    private final RemoteLibrary[] libraries = null;

    private final LinkedList<HistoryItem> history = new LinkedList<HistoryItem>();

    Opds.Entry returnBack()
    {
	if (isBusy())
	    return null;
	if (history.size() <= 1)
	    return null;
	history.pollLast();
	//	model.setItems(history.getLast().entries);
	return history.getLast().selected;
    }

    void clearHistory()
    {
	history.clear();
    }


    boolean isBusy()
    {
	return task != null && !task.isDone();
    }








    private final class LibrariesModel implements ListArea.Model
    {
	@Override public int getItemCount()
	{
	    return libraries.length;
	}
	@Override public Object getItem(int index)
	{
	    return libraries[index];
	}
	@Override public void refresh()
	{
	}
    }
    ListArea.Model getLibrariesModel()
    {
	return new LibrariesModel();
    }
}

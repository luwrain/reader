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

package org.luwrain.app.reader;

import java.io.File;
import java.util.LinkedList;

import org.luwrain.core.*;
import org.luwrain.core.events.*;
import org.luwrain.controls.*;
import org.luwrain.util.*;
import org.luwrain.doctree.*;

class ReaderArea extends DocTreeArea
{
    private Luwrain luwrain;
    private Strings strings;
    private Actions actions;

    ReaderArea(Luwrain luwrain,
		      Strings strings,
		      Actions actions,
	       Document document)
    {
	super(new DefaultControlEnvironment(luwrain), new Introduction(new DefaultControlEnvironment(luwrain), strings), document);
	this.luwrain = luwrain;
	this.strings = strings;
	this.actions = actions;
	NullCheck.notNull(luwrain, "luwrain");
	NullCheck.notNull(strings, "strings");
	NullCheck.notNull(actions, "actions");
    }

    @Override public boolean onEnvironmentEvent(EnvironmentEvent event)
    {
	NullCheck.notNull(event, "event");
	switch(event.getCode())
	{
	case EnvironmentEvent.THREAD_SYNC:
	    return onThreadSync(event);
	case EnvironmentEvent.CLOSE:
	    actions.closeApp();
	    return true;
	default:
	    return super.onEnvironmentEvent(event);
	}
    }

    @Override public String getAreaName()
    {
	final Document doc = getDocument();
	return doc != null?doc.getTitle():strings.appName();
    }

    private boolean onThreadSync(EnvironmentEvent event)
    {
	if (event instanceof FetchEvent)
	{
	    final FetchEvent fetchEvent = (FetchEvent)event;
	    if (fetchEvent.getFetchCode() == FetchEvent.FAILED)
	    {
		luwrain.message(strings.errorFetching(), Luwrain.MESSAGE_ERROR);
		return true;
	    }
	    final Document doc = Factory.loadFromText(Factory.HTML, fetchEvent.getText());
	    if (doc != null)
	    {
		setDocument(doc);
		luwrain.playSound(Sounds.MESSAGE_DONE);
	    }  else
		luwrain.message("problem parsing", Luwrain.MESSAGE_ERROR);//FIXME:
	    return true;
	}
	return false;
    }

    @Override protected void noContentMessage()
    {
	luwrain.hint(strings.noContent(), Hints.NO_CONTENT);
    }

}

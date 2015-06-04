/*
   Copyright 2012-2015 Michael Pozhidaev <michael.pozhidaev@gmail.com>

   This file is part of the Luwrain.

   Luwrain is free software; you can redistribute it and/or
   modify it under the terms of the GNU General Public
   License as published by the Free Software Foundation; either
   version 3 of the License, or (at your option) any later version.

   Luwrain is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
   General Public License for more details.
*/

package org.luwrain.app.reader;

import org.luwrain.core.*;
import org.luwrain.core.events.*;

class FetchEvent extends ThreadSyncEvent
{
    public static final int SUCCESS = 0;
    public static final int FAILED = 1;

    private int code;
    private String text = "";

    public FetchEvent(Area area)
    {
	super(area);
	this.code = FAILED;
    }

    public FetchEvent(Area area, String text)
    {
	super(area);
	this.code = SUCCESS;
	this.text = text;
	if (text == null)
	    throw new NullPointerException("text may not be null");
    }

    public int getCode()
    {
	return code;
    }

    public String getText()
    {
	return text;
    }
}

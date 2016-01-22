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

package org.luwrain.app.wiki;

import org.luwrain.core.*;
import org.luwrain.core.events.*;

class FetchEvent extends ThreadSyncEvent
{
    public static final int SUCCESS = 0;
    public static final int FAILED = 1;

    private int code;
    private Page[] pages;

    public FetchEvent(Area area)
    {
	super(area);
	this.code = FAILED;
	this.pages = null;
    }

    public FetchEvent(Area area, Page[] pages)
    {
	super(area);
	this.code = SUCCESS;
	this.pages = pages;
	if (pages == null)
	    throw new NullPointerException("pages may not be null");
    }

    public int code()
    {
	return code;
    }

    public Page[] pages()
    {
	return pages;
    }
}

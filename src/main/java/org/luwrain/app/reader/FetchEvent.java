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

import java.net.*;

import org.luwrain.core.*;
import org.luwrain.core.events.*;

class FetchEvent extends ThreadSyncEvent
{
static final int SUCCESS = 0;
    static final int FAILED = 1;

    private int code;
    private String text;
    private URL url;

    FetchEvent(Area area, String text)
    {
	super(area);
	this.code = FAILED;
	this.text = text;
	NullCheck.notNull(text, "text");
    }

    FetchEvent(Area area, String text, URL url)
    {
	super(area);
	this.code = SUCCESS;
	this.text = text;
	this.url = url;
	NullCheck.notNull(text, "text");
	NullCheck.notNull(url, "url");
    }

    int getFetchCode()
    {
	return code;
    }

    public String getText()
    {
	return text;
    }

    URL getUrl()
    {
	return url;
    }
}

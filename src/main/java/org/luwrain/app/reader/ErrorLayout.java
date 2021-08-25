/*
   Copyright 2012-2021 Michael Pozhidaev <msp@luwrain.org>
   Copyright 2015-2016 Roman Volovodov <gr.rPman@gmail.com>

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

package org.luwrain.app.reader;

import java.io.*;

import org.luwrain.core.*;
import org.luwrain.core.events.*;
import org.luwrain.controls.*;
import org.luwrain.app.base.*;

final class ErrorLayout extends LayoutBase
{
    private final App app;
    private final Throwable ex;
    private final SimpleArea errorArea;

    ErrorLayout(App app, Throwable ex, Runnable closing)
    {
	NullCheck.notNull(app, "app");
	NullCheck.notNull(ex, "ex");
	this.app = app;
	this.ex = ex;
	this.errorArea = new SimpleArea(new DefaultControlContext(app.getLuwrain()), "FIXME") {
		@Override public boolean onInputEvent(InputEvent event)
		{
		    NullCheck.notNull(event, "event");
		    if (closing != null)
		    {
		    if (app.onInputEvent(this, event, closing))
			return true;
		    } else
					    if (app.onInputEvent(this, event))
			return true;
		    return super.onInputEvent(event);
		}
		@Override public boolean onSystemEvent(SystemEvent event)
		{
		    NullCheck.notNull(event, "event");
		    if (app.onSystemEvent(this, event))
			return true;
		    return super.onSystemEvent(event);
		}
		@Override public boolean onAreaQuery(AreaQuery query)
		{
		    NullCheck.notNull(query, "query");
		    if (app.onAreaQuery(this, query))
			return true;
		    return super.onAreaQuery(query);
		}
	    };
	fillContent();
    }

    private void fillContent()
    {
	final StringWriter sw = new StringWriter();
	final PrintWriter pw = new PrintWriter(sw);
	ex.printStackTrace(pw);
	pw.flush();
	sw.flush();
	final String[] trace = sw.toString().split(System.lineSeparator(), -1);
	errorArea.update((lines)->{
	lines.addLine("");
	for(String s: trace)
	    lines.addLine(s);
	lines.addLine("");
	lines.endLinesTrans();
	    });
    }

    AreaLayout getLayout()
    {
	return new AreaLayout(errorArea);
    }
}

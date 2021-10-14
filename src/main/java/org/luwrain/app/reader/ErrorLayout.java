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
import java.net.*;

import org.luwrain.core.*;
import org.luwrain.core.events.*;
import org.luwrain.controls.*;
import org.luwrain.util.Connections.*;
import org.luwrain.app.base.*;

final class ErrorLayout extends LayoutBase
{
    private final App app;
    private final Throwable ex;
    private final SimpleArea errorArea;

    ErrorLayout(App app, Throwable ex, ActionHandler closing)
    {
	super(app);
	NullCheck.notNull(ex, "ex");
	this.app = app;
	this.ex = ex;
	this.errorArea = new SimpleArea(getControlContext(), "Ошибка");
	fillContent();
	if (closing != null)
	    setCloseHandler(closing);
	setAreaLayout(errorArea, actions());
    }

    private void fillContent()
    {
 	if (ex instanceof InvalidHttpResponseCodeException)
	{
	    final InvalidHttpResponseCodeException e = (InvalidHttpResponseCodeException)ex;
	    errorArea.update((lines)->{
		    lines.addLine("");
		    lines.addLine("Ошибка " + String.valueOf(e.getHttpCode()));
		    lines.addLine("");
		});
	    return;
	}

	 	if (ex instanceof UnknownHostException)
	{
	    final UnknownHostException e = (UnknownHostException)ex;
	    errorArea.update((lines)->{
		    lines.addLine("");
		    lines.addLine("Неизвестный хост: " + e.getMessage());
		    lines.addLine("");
		});
	    return;
	}

		
	
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
	    });
    }
}

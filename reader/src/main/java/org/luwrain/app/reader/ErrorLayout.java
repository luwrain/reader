// SPDX-License-Identifier: BUSL-1.1
// Copyright 2012-2026 Michael Pozhidaev <msp@luwrain.org>

package org.luwrain.app.reader;

import java.io.*;
import java.net.*;

import org.luwrain.core.*;
import org.luwrain.core.events.*;
import org.luwrain.controls.*;
import org.luwrain.util.Connections.*;
import org.luwrain.app.base.*;

import static java.util.Objects.*;

final class ErrorLayout extends LayoutBase
{
    private final App app;
    private final Throwable ex;
    private final SimpleArea errorArea;

    ErrorLayout(App app, Throwable ex, ActionHandler closing)
    {
	super(app);
	requireNonNull(ex, "ex can't be null");
	this.app = app;
	this.ex = ex;
	this.errorArea = new SimpleArea(getControlContext(), app.getStrings().errorAreaName());
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
		    lines.add("");
		    lines.add("HTTP " + String.valueOf(e.getHttpCode()));
		    lines.add("");
		});
	    return;
	}

	if (ex instanceof UnknownHostException)
	{
	    final UnknownHostException e = (UnknownHostException)ex;
	    errorArea.update((lines)->{
		    lines.add("");
		    lines.add(app.getStrings().badUrl() + " " + e.getMessage());
		    lines.add("");
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
		lines.add("");
		for(String s: trace)
		    lines.add(s);
		lines.add("");
	    });
    }
}

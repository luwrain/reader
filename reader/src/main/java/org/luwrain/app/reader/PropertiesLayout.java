// SPDX-License-Identifier: BUSL-1.1
// Copyright 2012-2026 Michael Pozhidaev <msp@luwrain.org>

package org.luwrain.app.reader;

import java.util.*;
import java.io.*;

import org.luwrain.core.*;
import org.luwrain.controls.*;
import org.luwrain.io.bookdoc.*;
import org.luwrain.app.base.*;


final class PropertiesLayout extends LayoutBase
{
    private final App app;
    private final String[] lines;
    final NavigationArea propArea;

    PropertiesLayout(App app, ActionHandler closing)
    {
	super(app);
	this.app = app;
	final Doc doc = app.getBookContainer().getDocument();
	if (doc != null)
	{
	    final DocProps docProps = new DocProps(app.getLuwrain(), app.getStrings(), doc);
	    this.lines = docProps.getLines();
	} else
	    this.lines = new String[]{"", app.getStrings().noContent(), ""};
	this.propArea = new NavigationArea(getControlContext()){
		@Override public int getLineCount()
		{
		    return lines.length;
		}
		@Override public String getLine(int index)
		{
		    if (index < 0 || index >= lines.length)
			return "";
		    return lines[index];
		}
		@Override public String getAreaName()
		{
		    return app.getStrings().propertiesAreaName();
		}
	    };
	setCloseHandler(closing);
	setAreaLayout(propArea, null);
    }
}

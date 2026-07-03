// SPDX-License-Identifier: BUSL-1.1
// Copyright 2012-2026 Michael Pozhidaev <msp@luwrain.org>

package org.luwrain.app.reader;

import java.util.*;
import java.io.*;

import org.luwrain.core.*;
import org.luwrain.controls.*;
import org.luwrain.app.base.*;


final class PropertiesLayout extends LayoutBase
{
    private App app;
    final NavigationArea propArea;

    PropertiesLayout(App app, ActionHandler closing)
		{
		    super(app);
		    this.propArea = new NavigationArea(getControlContext()){
			    @Override public int getLineCount()
			    {
				return 2;
			    }
			    @Override public String getLine(int index)
			    {
				return "proba";
			    }
			    @Override public String getAreaName()
			    {
				return "Properties";
			    }
			};
		    setCloseHandler(closing);
    setAreaLayout(propArea, null);
		}
}

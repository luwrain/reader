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

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
import org.luwrain.core.events.*;
import org.luwrain.controls.*;
import org.luwrain.app.base.*;

import org.luwrain.io.api.books.v1.*;
import org.luwrain.io.api.books.v1.collection.*;

final class LocalRepoLayout extends LayoutBase implements ListArea.ClickHandler
{
    private App app;
    final ListArea listArea;

    LocalRepoLayout(App app)
    {
	NullCheck.notNull(app, "app");
	this.app = app;
	this.listArea = new ListArea(createListParams()) {
		@Override public boolean onInputEvent(InputEvent event)
		{
		    NullCheck.notNull(event, "event");
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
    }

    @Override public boolean onListClick(ListArea listArea, int index, Object obj)
    {
	NullCheck.notNull(obj, "obj");
	return true;
    }

    private ListArea.Params createListParams()
    {
	final ListArea.Params params = new ListArea.Params();
	params.context = new DefaultControlContext(app.getLuwrain());
	params.name = "Загруженные книги";
	params.model = new ListUtils.ArrayModel(()->app.getLocalRepo().getBooks());
	params.appearance = new ListUtils.DefaultAppearance(params.context);
	params.clickHandler = this;
	return params;
    }

    AreaLayout getLayout()
    {
	return new AreaLayout(listArea);
    }
}
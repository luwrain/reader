// SPDX-License-Identifier: BUSL-1.1
// Copyright 2012-2026 Michael Pozhidaev <msp@luwrain.org>

package org.luwrain.app.reader;

import java.util.*;
import java.io.*;

import org.luwrain.core.*;
import org.luwrain.core.events.*;
import org.luwrain.controls.*;
import org.luwrain.app.base.*;

import org.luwrain.io.api.books.v1.*;
import org.luwrain.io.api.books.v1.collection.*;

import static java.util.Objects.*;

final class LocalRepoLayout extends LayoutBase implements ListArea.ClickHandler
{
    final App app;
    final ListArea listArea;

    LocalRepoLayout(App app)
    {
	super(app);
	this.app = app;
	this.listArea = new ListArea(createListParams()) {
		final Actions actions = actions(
						action("delete", app.getStrings().localRepoActDelete(), new InputEvent(InputEvent.Special.DELETE),LocalRepoLayout.this::actDelete )
						);
		@Override public boolean onInputEvent(InputEvent event)
		{
		    if (app.onInputEvent(this, event))
			return true;
		    return super.onInputEvent(event);
		}
		@Override public boolean onSystemEvent(SystemEvent event)
		{
		    if (app.onSystemEvent(this, event, actions))
			return true;
		    return super.onSystemEvent(event);
		}
		@Override public boolean onAreaQuery(AreaQuery query)
		{
		    requireNonNull(query, "query can't be null");
		    if (app.onAreaQuery(this, query))
			return true;
		    return super.onAreaQuery(query);
		}
		@Override public Action[] getAreaActions()
		{
		    return actions.getAreaActions();
		}
	    };
	setAreaLayout(listArea, null);
    }

    @Override public boolean onListClick(ListArea listArea, int index, Object obj)
    {
	if (!(obj instanceof Book))
	    return false;
	final Book book = (Book)obj;
	if (!app.getLocalRepo().hasBook(book))
	{
	    app.message(app.getStrings().localRepoBookCorrupted(), Luwrain.MessageType.ERROR);
	    return true;
	}
	final File mainFile = app.getLocalRepo().findDaisyMainFile(book);
	if (mainFile == null)
	{
	    app.message(app.getStrings().localRepoBookCorrupted(), Luwrain.MessageType.ERROR);
	    return true;
	}
	app.open(mainFile.toURI());
	return true;
    }

    private boolean actDelete()
    {
	final Object obj = listArea.selected();
	if (obj == null || !(obj instanceof Book))
	    return false;
	final Book book = (Book)obj;
	if (!app.getConv().confirmLocalBookDeleting(book.getName()))
	    return true;
	app.getLocalRepo().remove(book);
	listArea.refresh();
	return true;
    }

    private ListArea.Params createListParams()
    {
	final ListArea.Params params = new ListArea.Params();
	params.context = new DefaultControlContext(app.getLuwrain());
	params.name = app.getStrings().localRepoAreaName();
	params.model = new ListUtils.ArrayModel(()->app.getLocalRepo().getBooks());
	params.appearance = new ListUtils.DefaultAppearance(params.context);
	params.clickHandler = this;
	return params;
    }
}

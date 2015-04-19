/*
   Copyright 2012-2015 Michael Pozhidaev <michael.pozhidaev@gmail.com>

   This file is part of the Luwrain.

   Luwrain is free software; you can redistribute it and/or
   modify it under the terms of the GNU General Public
   License as published by the Free Software Foundation; either
   version 3 of the License, or (at your option) any later version.

   Luwrain is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
   General Public License for more details.
*/

package org.luwrain.app.reader;

import java.io.File;

import org.luwrain.core.*;
import org.luwrain.core.events.*;
import org.luwrain.controls.*;

import org.luwrain.app.reader.doctree.*;

public class ReaderArea extends NavigateArea
{
    private Luwrain luwrain;
    private Strings strings;
    private Actions actions;
    private Document document;

    public ReaderArea(Luwrain luwrain,
		      Strings strings,
		      Actions actions)
    {
	super(new DefaultControlEnvironment(luwrain));
	this.luwrain = luwrain;
	this.strings = strings;
	this.actions = actions;
	if (luwrain == null)
	    throw new NullPointerException("luwrain may not be null");
	if (strings == null)
	    throw new NullPointerException("stringss may not be null");
	if (actions == null)
	    throw new NullPointerException("actions may not be null");

	Node root = new Node(Node.ROOT);
	Paragraph para = new Paragraph();
	para.runs = new Run[]{
		new Run("First testing run"),
		    new Run("Second testing run")
	    };
	para.setParentOfRuns();

	Node table1 = new Node(Node.TABLE);
	table1.subnodes = new Node[]{new Node(Node.TABLE_ROW), new Node(Node.TABLE_ROW)};
	table1.subnodes[0].subnodes = new Node[]{new Node(Node.TABLE_CELL), new Node(Node.TABLE_CELL)};
	table1.subnodes[1].subnodes = new Node[]{new Node(Node.TABLE_CELL), new Node(Node.TABLE_CELL)};
	table1.subnodes[0].subnodes[0].subnodes = new Node[]{new Paragraph(new Run("1"))};
	table1.subnodes[0].subnodes[1].subnodes = new Node[]{new Paragraph(new Run("2"))};
	table1.subnodes[1].subnodes[0].subnodes = new Node[]{new Paragraph(new Run("3"))};
	table1.subnodes[1].subnodes[1].subnodes = new Node[]{new Paragraph(new Run("4"))};

	root.subnodes = new Node[]{para, table1};

	document = new Document(root);
	document.buildView(50);
    }

    @Override public int getLineCount()
    {
	return document.getLineCount() + 1;
    }

    @Override public String getLine(int index)
    {
	return index < document.getLineCount()?document.getLine(index):"";
    }

    @Override public boolean onEnvironmentEvent(EnvironmentEvent event)
    {
	switch(event.getCode())
	{
	case EnvironmentEvent.CLOSE:
	    actions.closeApp();
	    return true;
	default:
	    return false;
	}
    }

    @Override public String getName()
    {
	return strings.appName();
    }
}

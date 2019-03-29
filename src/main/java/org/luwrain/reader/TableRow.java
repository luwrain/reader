/*
   Copyright 2012-2019 Michael Pozhidaev <michael.pozhidaev@gmail.com>
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

package org.luwrain.reader;

import java.util.*;

import org.luwrain.core.*;

public class TableRow extends Node
{
    TableRow()
    {
	super(Node.Type.TABLE_ROW);
    }

    @Override void preprocess()
    {
	NullCheck.notNullItems(subnodes, "subnodes");
	for(int i = 0;i < subnodes.length;++i)
	    if (!(subnodes[i] instanceof TableCell))
	    {
		Log.warning("doctree", "table row has a subnode of class " + subnodes[i].getClass().getName() + ", it will be put into newly created table cell");
		final Node n = NodeFactory.newNode(Type.TABLE_CELL);
		n.subnodes = new Node[]{subnodes[i]};
		n.subnodes[0].parentNode = n;
		n.parentNode = this;
		subnodes[i] = n;
	    }
	super.preprocess();
    }

    void addEmptyCells(int num)
    {
	NullCheck.notNullItems(subnodes, "subnodes");
	if (subnodes.length >= num)
	    return;
	final Node[] newNodes = Arrays.copyOf(subnodes, num);
	for(int i = subnodes.length;i < newNodes.length;++i)
	{
	    final TableCell cell = new TableCell();
	    cell.subnodes = new Node[]{new EmptyLine()};
	    cell.subnodes[0].parentNode = cell;
	    cell.parentNode = this;
	    newNodes[i] = cell;
	}
	subnodes = newNodes;
    }
}

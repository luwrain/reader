/*
   Copyright 2012-2016 Michael Pozhidaev <michael.pozhidaev@gmail.com>
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

package org.luwrain.doctree;

import org.luwrain.core.*;

import org.luwrain.core.NullCheck;

public class Table extends Node
{
    Table()
    {
	super(Node.Type.TABLE);
    }

    @Override void preprocess()
    {
	NullCheck.notNullItems(subnodes, "subnodes");
	for(int i = 0;i < subnodes.length;++i)
	    if (!(subnodes[i] instanceof TableRow))
	    {
		Log.warning("doctree", "table has a subnode of class " + subnodes[i].getClass().getName() + ", it will be put into newly created table row");
		final Node n = NodeFactory.newNode(Type.TABLE_ROW);
		n.subnodes = new Node[]{subnodes[i]};
		n.subnodes[0].parentNode = n;
		n.parentNode = this;
		subnodes[i] = n;
	    }
	int maxCellCount = 0;
	for(Node n: subnodes)
	    if (maxCellCount < n.getSubnodeCount())
		maxCellCount = n.getSubnodeCount();
	for(Node n: subnodes)
	    ((TableRow)n).addEmptyCells(maxCellCount);
	super.preprocess();
    }

    public TableCell getCell(int col, int row)
    {
	if (row >= subnodes.length || col >= subnodes[row].subnodes.length)
	    return null;
	final Node cellNode = subnodes[row].subnodes[col];
	if (cellNode == null || !(cellNode instanceof TableCell))
	    return null;
	return (TableCell)cellNode;
    }

    public int getRowCount()
    {
	return subnodes.length;
    }

    public int getColCount()
    {
	int maxValue = 0;
	for(Node n: subnodes)
	    if (maxValue < n.subnodes.length)
		maxValue = n.subnodes.length;
	return maxValue;
    }

    public int getTableLevel()
    {
	int count = 1;
	Node n = parentNode;
	while(n != null)
	{
	    if (n.type == Node.Type.TABLE)
		++count;
	    n = n.parentNode;
	}
	return count;
    }

    public boolean isSingleCellTable()
    {
	NullCheck.notNullItems(subnodes, "subnodes");
	return subnodes.length == 1 || subnodes[0].getSubnodes().length == 1;
    }
}

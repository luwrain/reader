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

import org.luwrain.core.*;

public class TableCell extends Node
{
    TableCell()
    {
	super(Node.Type.TABLE_CELL);
    }

    @Override public void preprocess()
    {
	NullCheck.notNullItems(subnodes, "subnodes");
	for(Node n: subnodes)
	{
	    if (n instanceof TableRow)
		Log.warning("doctree", "table cell contains table row what is very odd and looks like a bug");
	    if (n instanceof TableCell)
		Log.warning("doctree", "table cell contains table cell what is very odd and looks like a bug");
	    if (n instanceof ListItem)
		Log.warning("doctree", "table cell contains list item what is very odd and looks like a bug");
	}
	super.preprocess();
    }

    public Table getTable()
    {
	if (getParentNode() == null || getParentNode().getParentNode() == null)
	    return null;
	final Node tableNode = getParentNode().getParentNode();
	if (!(tableNode instanceof Table))
	{
	    Log.warning("doctree", "table node has a wrong class " + tableNode.getClass().getName());
	    return null;
	}
	return (Table)tableNode;
    }

    public int getColIndex()
    {
	return getIndexInParentSubnodes();
    }

    public int getRowIndex()
    {
	return getParentNode().getIndexInParentSubnodes();
    }
}

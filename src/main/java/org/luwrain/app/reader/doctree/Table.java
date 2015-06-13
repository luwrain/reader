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

package org.luwrain.app.reader.doctree;

public class Table extends Node
{
    public Table()
    {
	super(TABLE);
	type = TABLE;
    }

    public int getColIndexOfCell(Node cell)
    {
	if (cell == null || cell.parentNode.parentNode != this)
	    return -1;
	return cell.getIndexInParentSubnodes();
    }

    public int getRowIndexOfCell(Node cell)
    {
	if (cell == null || cell.parentNode.parentNode != this)
	    return -1;
	return cell.parentNode.getIndexInParentSubnodes();
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
}

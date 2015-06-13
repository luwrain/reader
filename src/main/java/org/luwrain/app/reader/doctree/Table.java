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
    }

    public Table(Node[] subnodes)
    {
	super(TABLE, subnodes);
    }

    @Override public void commit()
    {
	super.commit();
	for(Node n: subnodes)
	{
	    if (n.type != TABLE_ROW)
		System.out.println("warning:doctree:table has a subnode with type different than TABLE_ROW");
	    for(Node nn: n.subnodes)
		if (nn.type != TABLE_CELL)
		System.out.println("warning:doctree:table row has a subnode with type different than TABLE_CELL");
	}
    }

    public Node getCell(int col, int row)
    {
	if (row >= subnodes.length || col >= subnodes[row].subnodes.length)
	    return null;
	return subnodes[row].subnodes[col];
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

    public int getTableLevel()
    {
	int count = 1;
	Node n = parentNode;
	while(n != null)
	{
	    if (n.type == TABLE)
		++count;
	    n = n.parentNode;
	}
	return count;
    }

    public boolean isSingleLineRow(int index)
    {
	for(Node n: subnodes[index].subnodes)
	{
	    if (n.subnodes.length > 1)
		return false;
	    if (n.subnodes[0].type != PARAGRAPH || !(n.subnodes[0] instanceof Paragraph))
		return false;
	    final Paragraph p = (Paragraph)n.subnodes[0];
	    if (p.height > 1)
		return false;
	}
	    return true;
    }
}

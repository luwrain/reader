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

public class Node
{
    private static final int MIN_TABLE_CELL_WIDTH = 8;

    public final static int ROOT = 1;
    public final static int SECTION = 2;
    public final static int PARAGRAPH = 3;
    public final static int  TABLE = 4;
    public final static int  TABLE_ROW = 5;
    public final static int  TABLE_CELL = 6;
    public final static int  UNORDERED_LIST = 7;
    public final static int  ORDERED_LIST = 8;
    public final static int  LIST_ITEM = 9;

    public int type;
    public Node[] subnodes = new Node[0];

    public int x = -1;
    public int y = -1;
    public int width = 0;
    public int height = 0;

    public Node(int type)
    {
	this.type = type;
    }

    public void calcWidth(int recommended)
    {
	width = 0;
	switch (type)
	{
	case TABLE_CELL:
	    if (subnodes == null || subnodes.length < 1)
	    {
		width = recommended >= MIN_TABLE_CELL_WIDTH?recommended:MIN_TABLE_CELL_WIDTH;
		break;
	    }
	    for(Node n: subnodes)
	    {
		n.calcWidth(recommended >= MIN_TABLE_CELL_WIDTH?recommended:MIN_TABLE_CELL_WIDTH);
		if (width < n.width)
		    width = n.width;
	    }
	    break;
	case TABLE_ROW:
	    for(Node n: subnodes)
		n.calcWidth((recommended - subnodes.length + 1) >= subnodes.length?(recommended - subnodes.length + 1) / subnodes.length:1);
	    for(Node n: subnodes)
		width += n.width;
	    width += (subnodes.length - 1);//One additional empty column after each cell;
	    if (width < recommended)
		width = recommended;
	    break;
	case PARAGRAPH:
	    width = recommended;
	    break;
	case ROOT:
	case SECTION:
	case UNORDERED_LIST:
	case ORDERED_LIST:
	case LIST_ITEM:
	case TABLE:
	    if (subnodes == null || subnodes.length < 1)
	    {
		width = recommended;
		break;
	    }
	    for(Node n: subnodes)
	    {
		n.calcWidth(recommended);
		if (width < n.width)
		    width = n.width;
	    }
	    break;
	default:
	    throw new IllegalArgumentException("unknown node type " + type);
	}
    }

    public void calcHeight()
    {
	for(Node n: subnodes)
	    n.calcHeight();
	height = 0;
	switch (type)
	{
	case TABLE_ROW:
	    for(Node n: subnodes)
		if (height < n.height)
		    height = n.height;
	    break;
	case ROOT:
	case SECTION:
	case TABLE:
	case TABLE_CELL:
	case UNORDERED_LIST:
	case ORDERED_LIST:
	case LIST_ITEM:
	    for(Node n: subnodes)
		height += n.height;
	    break;
	default:
	    throw new IllegalArgumentException("unknown node type " + type);
	}
    }

    public void calcPosition()
    {
	int offset = 0;
	switch (type)
	{
	case TABLE_ROW:
	    offset = 0;
	    for(Node n: subnodes)
	    {
		n.x = x + offset;
		offset += (n.width + 1);
		n.y = y;
		n.calcPosition();
	    }
	    break;
	case PARAGRAPH:
	    break;
	case ROOT:
	    x = 0;
	    y = 0;
	case SECTION:
	case TABLE:
	case TABLE_CELL:
	case UNORDERED_LIST:
	case ORDERED_LIST:
	case LIST_ITEM:
	    for(Node n: subnodes)
	    {
		n.x = x;
		n.y = y + offset;
		offset += n.height;
		n.calcPosition();
	    }
	    break;
	default:
	    throw new IllegalArgumentException("unknown node type " + type);
	}
    }
}
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

//LWR_API 1.0

package org.luwrain.controls.reader.view;

import org.luwrain.core.*;

public class NodeBase
{
    /**Absolute horizontal position in the area*/
    private int x = -1;

    /**Absolute vertical position in the area*/
    private int y = -1;

    public int width = 0;
    private int height = 0;

    private RowPart[] rowParts = new RowPart[0];

    public int getNodeX()
    {
	return x;
    }

    public void setNodeX(int value)
    {
	x = value;
    }

    public int getNodeY()
    {
	return y;
    }

    public void setNodeY(int value)
    {
	y = value;
    }

    public int getNodeWidth()
    {
	return width;
    }

    public void setNodeWidth(int value)
    {
	width = value;
    }

    public int getNodeHeight()
    {
	return height;
    }

    public void setNodeHeight(int value)
    {
	height = value;
    }

    public void setRowParts(RowPart[] rowParts)
    {
	NullCheck.notNullItems(rowParts, "rowParts");
	this.rowParts = rowParts != null?rowParts:new RowPart[0];
    }

    public RowPart[] getRowParts()
    {
	return rowParts;
    }
}

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

package org.luwrain.controls.reader.view;

import org.luwrain.core.*;
import org.luwrain.reader.*;

final class NodeGeom
{
    void calcWidth(Node node, int recommended)
    {
	NullCheck.notNull(node, "node");
	final Node[] subnodes = node.getSubnodes();
	NullCheck.notNullItems(subnodes, "subnodes");
	if (node instanceof TableRow)
	{
	    final TableRow tableRow = (TableRow)node;
	    final int cellWidth = (recommended - subnodes.length + 1) >= subnodes.length?(recommended - subnodes.length + 1) / subnodes.length:1;
	    for(Node n: subnodes)
		calcWidth(n, cellWidth);
	    tableRow.width = 0;
	    for(Node n: subnodes)
		tableRow.width += n.width;
	    tableRow.width += (subnodes.length - 1);//One additional empty column after each cell
	    if (tableRow.width < recommended)
		tableRow.width = recommended;
	    return;
	}
	node.width = recommended;
	for(Node n: subnodes)
	{
	    calcWidth(n, recommended);
	    if (node.width < n.width)
	        node.width = n.width;
	}
    }

    void calcHeight(Node node)
    {
	/*
	NullCheck.notNull(node, "node");
	if (node instanceof Paragraph)
	{
	    final Paragraph para = (Paragraph)node;
	    if (para.getRowParts().length == 0)
	    {
		Log.warning("doctree", "there is a paragraph without runs");
		para.setNodeHeight(0);
		return;
	    }
	    int maxRelRowNum = 0;
	    for(RowPart p: (RowPart[])para.getRowParts())
		if (p.relRowNum > maxRelRowNum)
		    maxRelRowNum = p.relRowNum;
	    para.setNodeHeight(maxRelRowNum + 1);
	    return;
	}
	final Node[] subnodes = node.getSubnodes();
	NullCheck.notNullItems(subnodes, "subnodes");
	if (node instanceof TableRow)
	{
	    final TableRow tableRow = (TableRow)node;
	    for(Node n: subnodes)
		calcHeight(n);
	    tableRow.setNodeHeight(0);
	    for(Node n: subnodes)
		if (tableRow.getNodeHeight() < n.getNodeHeight())
		    tableRow.setNodeHeight(n.getNodeHeight());
	    return;
	}
	//Not a paragraph and not a table row
	for(Node n: subnodes)
	    calcHeight(n);
	int height = 0;
	for(Node n: subnodes)
	    height += n.getNodeHeight();
	if (!node.allSubnodesSingleLine())
    if (subnodes.length > 0)
	height += (subnodes.length - 1);
    node.setNodeHeight(height);
	*/
    }

    void calcPosition(Node node)
    {
	NullCheck.notNull(node, "node");
	final Node[] subnodes = node.getSubnodes();
	NullCheck.notNullItems(subnodes, "subnodes");
		if  (node.getType() == Node.Type.ROOT)
	{
	    node.setNodeX(0);
	    node.setNodeY(0);
	}
	//Assuming node.x and node.y already set appropriately
		    final int baseX = node.getNodeX();
		    		    final int baseY = node.getNodeY();
	if (node instanceof TableRow)
	{
	    final TableRow tableRow = (TableRow)node;
	    int offset = 0;
	    for(Node n: subnodes)
	    {
		n.setNodeX(baseX + offset);
		offset += (n.width + 1);
		n.setNodeY(baseY);
		calcPosition(n);
	    }
	    return;
	} //table row
	int offset = 0;
	/*
	if (node instanceof Paragraph && ((Paragraph)node).getRowParts().length > 0)
	    offset = 1;
	*/
	for(Node n: subnodes)
	{
	    n.setNodeX(baseX);
	    n.setNodeY(baseY + offset);
	    offset += n.getNodeHeight();
	    if (!node.allSubnodesSingleLine())
		offset++;
	    calcPosition(n);
	}
    }
}

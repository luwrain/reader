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

public class Document 
{
    private Node root;
    public RowPart[] rowParts;
    private Row[] rows;

    public Document(Node root)
    {
	this.root = root;
	if (root == null)
	    throw new NullPointerException("root may not be null");
    }

    public void buildView()
    {
	/*
	root.setParentOfSubnodes();
	RowPartsBuilder rowPartsBuilder = new RowPartsBuilder(50);//FIXME:50;
	rowParts = rowPartsBuilder.parts();
	if (rowParts == null)
	    rowParts = new RowParts[0];
	if (rowParts.length <= 0)
	    return;

	for(RowPart part: rowParts)
	    part.node.containsRow(part.rowNum);
	root.setHeightInRows();
	rows = RowsBuilder.buildRows(rowParts);
	*/
    }

}

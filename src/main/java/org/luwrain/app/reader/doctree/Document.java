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
    private Line[] lines;

    public Document(Node root)
    {
	this.root = root;
	if (root == null)
	    throw new NullPointerException("root may not be null");
    }

    public void buildView(int width)
    {
	root.calcWidth(width);
	RowPartsBuilder rowPartsBuilder = new RowPartsBuilder();
	rowParts = rowPartsBuilder.parts();
	if (rowParts == null)
	    rowParts = new RowPart[0];
	if (rowParts.length <= 0)
	    return;
	for(RowPart part: rowParts)
	    part.run.parentParagraph.containsRow(part.rowNum);
	root.calcHeight();
	//	root.calcPosition();
	rows = RowsBuilder.buildRows(rowParts);
	int maxLineNum = 0;
	for(Row r: rows)
	{
	    //Generally admissible situation as not all rows should have associated parts;
	    if (r.partsFrom < 0 || r.partsTo < 0 || r.partsFrom >= r.partsTo)
		continue;
	    final Run run = rowParts[r.partsFrom].run;
	    if (run == null)
		throw new NullPointerException("run may not be null");
	    final Paragraph paragraph = run.parentParagraph;
	    if (paragraph == null)
		throw new NullPointerException("paragraph may not be null");
	    r.x = paragraph.x;
	    r.y = paragraph.y + (rowParts[r.partsFrom].rowNum - paragraph.minRowIndex);
	    if (r.y > maxLineNum)
		maxLineNum = r.y;
	}
	lines = new Line[maxLineNum];
	for(int k = 0;k < rows.length;++k)
	{
	    final Line line = lines[rows[k].y];
	    final int[] oldRows = line.rows;
	    line.rows = new int[oldRows.length + 1];
	    for(int i = 0;i < oldRows.length;++i)
		line.rows[i] = oldRows[i];
	    line.rows[oldRows.length] = k;

	}
    }

}

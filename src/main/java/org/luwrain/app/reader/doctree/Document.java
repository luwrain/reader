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
    private Line[] lines = new Line[0];

    public Document(Node root)
    {
	this.root = root;
	if (root == null)
	    throw new NullPointerException("root may not be null");
    }

    public void buildView(int width)
    {
	//	System.out.println("reader:starting Document.buildView()");
	root.calcWidth(width);
	RowPartsBuilder rowPartsBuilder = new RowPartsBuilder();
	rowPartsBuilder.onNode(root);
	rowParts = rowPartsBuilder.parts();
	//	System.out.println("reader:" + rowParts.length + " parts");
	if (rowParts == null)
	    rowParts = new RowPart[0];
	if (rowParts.length <= 0)
	    return;
	//	for(RowPart part: rowParts)
	//	    part.run.parentParagraph.containsRow(part.rowNum);
	//	System.out.println("reader:parent paragraphs installed");
	root.calcHeight();
	//	System.out.println("reader:root height = " + root.height);
		root.calcPosition();
		//		System.out.println("reader:positions calculated");
	rows = RowsBuilder.buildRows(rowParts);
	System.out.println("reader:" + rows.length + " rows");
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
	    //	    r.y = paragraph.y + (rowParts[r.partsFrom].rowNum - paragraph.minRowIndex);
	    if (r.y > maxLineNum)
		maxLineNum = r.y;
	}
	System.out.println("reader:maxLineNum=" + maxLineNum);
	lines = new Line[maxLineNum + 1];
	for(int i = 0;i < lines.length;++i)
	    lines[i] = new Line();
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

    public int getLineCount()
    {
	return lines != null?lines.length:0;
    }

    public String getLine(int index)
    {
	final Line line = lines[index];
	StringBuilder b = new StringBuilder();
	for(int r: line.rows)
	{
	    final Row row = rows[r];
	    while(b.length() < row.x)
		b.append(" ");
	    b.append(row.text(rowParts));
	}
	return b.toString();
    }

    public int getRowCount()
    {
	return rows != null?rows.length:0;
    }

    public String getRowText(int index)
    {
	if (rows == null || index < 0 || index >= rows.length)
	    return "";
	final Row row = rows[index];
	if (row.partsFrom < 0 || row.partsTo < 0)
	    return "";
	return row.text(rowParts);
    }

    public boolean isValidRowIndex(int index)
    {
	if (rows == null)
	    return false;
	return index >= 0 && index < rows.length;
    }

    public int getRowIndexInParagraph(int index)
    {
	if (rows == null)
	    throw new NullPointerException("rows may not be null");
	final Row row = rows[index];
	if (row.partsFrom < 0 || row.partsTo < 0)
	    return 0;
	final Paragraph para = rowParts[row.partsFrom].run.parentParagraph;
	return index - para.topRowIndex;
    }

    public Paragraph getParagraph(int index)
    {
	if (rows == null)
	    throw new NullPointerException("rows may not be null");
	final Row row = rows[index];
	if (row.partsFrom < 0 || row.partsTo < 0)
	    return null;
return rowParts[row.partsFrom].run.parentParagraph;
    }
}

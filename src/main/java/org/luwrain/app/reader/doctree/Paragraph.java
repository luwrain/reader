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

public class Paragraph extends Node
{
    public Run[] runs = new Run[0];
    public RowPart[] rowParts = new RowPart[0];

    /** Position of the first row in a document*/
    public int topRowIndex = -1;

    public Paragraph()
    {
	super(PARAGRAPH);
    }

    public Paragraph(Run run)
    {
	super(PARAGRAPH);
	if (run == null)
	    throw new NullPointerException("run may not be null");
	runs = new Run[]{run};
	runs[0].parentParagraph = this;
    }

    @Override public void commit()
    {
	subnodes = null;
	if (runs == null)
	    return;
	for(Run r: runs)
	    r.parentParagraph = this;
    }

    @Override public void setEmptyMark()
    {
	empty = true;
	if (runs == null || runs.length < 1)
	    return;
	for(Run r: runs)
	    if (r.text != null && !r.text.isEmpty())
		empty = false;
    }

    @Override public void removeEmpty()
    {
	if (runs == null)
	    return;
	int k = 0;
	for(int i = 0;i < runs.length;++i)
	    if (runs[i].text.isEmpty() )
		++k; else
		runs[i - k] = runs[i];
	if (k > 0)
	{
	    final int count = runs.length - k;
	    Run[] newRuns = new Run[count];
	    for(int i = 0;i < count;++i)
		newRuns[i] = runs[i];
	    runs = newRuns;
	}
    }

    @Override public void saveStatistics(Statistics stat)
    {
	++stat.numNodes;
	++stat.numParagraphs;
	stat.numRuns += (runs != null?runs.length:0);
    }

    @Override public void calcHeight()
    {
	if (rowParts == null && rowParts.length < 1)
	{
	    height = 0;
	    return;
	}
	int maxRelRowNum = 0;
	for(RowPart p: rowParts)
	    if (p.relRowNum > maxRelRowNum)
		maxRelRowNum = p.relRowNum;
	height = maxRelRowNum + 1;
    }

    public boolean shouldHaveExtraLine()
    {
	if (getParentType() == ROOT)
	    return true;
	return false;
	/*
	final int parentSubnodeCount = getParentSubnodeCount();
	if (parentSubnodeCount == 1 &&
	    (isInTableCell() || isInListItem()))
	    return;
	++height;
	*/
    }

    public boolean isInListItem()
    {
	return parentNode != null && parentNode.type == LIST_ITEM;
    }

    public boolean isInTableCell()
    {
	return parentNode != null && parentNode.type == TABLE_CELL;
    }

    public int getParaIndex()
    {
	return getIndexInParentSubnodes();
    }
}

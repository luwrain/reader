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
    public int minRowIndex = -1;
    public int maxRowIndex = -1;

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

    public void containsRow(int index)
    {
	if (minRowIndex < 0 || maxRowIndex < 0)
	{
	    minRowIndex = index;
	    maxRowIndex = index;
	    return;
	}
	if (minRowIndex <= index && maxRowIndex >= index)
	    return;
	if (index < minRowIndex)
	    minRowIndex = index;
	if (index > maxRowIndex)
	    maxRowIndex = index;
    }

    @Override public void calcHeight()
    {
	if (minRowIndex < 0 || maxRowIndex < 0)
	{
	    height = 0;
	    return;
	}
	height = maxRowIndex - minRowIndex + 2;
    }

    public void setParentOfRuns()
    {
	if (runs == null)
	    return;
	for(Run r: runs)
	    r.parentParagraph = this;
    }

    @Override public void setParentOfSubnodes()
    {
	setParentOfRuns();
    }
}

/*
   Copyright 2012-2018 Michael Pozhidaev <michael.pozhidaev@gmail.com>
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

package org.luwrain.doctree.view;

import java.util.*;

import org.luwrain.core.*;
import org.luwrain.doctree.*;

public class Row
{
    /** Absolute horizontal position in the area*/
int x = 0;
    /** Absolute vertical position in the area*/
    int y = 0;

    private final RowPart[] parts;
    private final int partsFrom;
    private final int partsTo;

    Row()
    {
	this.parts = null;
	this.partsFrom = -1;
	this.partsTo = -1;
    }

    Row(RowPart[] parts, int partsFrom, int partsTo)
    {
	NullCheck.notNull(parts, "parts");
	if (partsFrom < 0)
	    throw new IllegalArgumentException("partsFrom (" + partsFrom + ") may not be negative");
		if (partsTo < 0)
	    throw new IllegalArgumentException("partsTo (" + partsTo + ") may not be negative");
		if (partsFrom >= partsTo)
		    throw new IllegalArgumentException("partsFrom (" + partsFrom + ") must be less than partsTo (" + partsTo + ")");
				this.parts = parts;
	this.partsFrom = partsFrom;
	this.partsTo = partsTo;
    }

        public String getText()
    {
	//	if (isEmpty())
	//	    return "";
	final StringBuilder b = new StringBuilder();
	for(int i = partsFrom;i < partsTo;++i)
	    b.append(parts[i].getText());
	return b.toString();
    }

    //returns null if there is no suitable
    Run getRunUnderPos(int pos)
    {
	if (pos < 0)
	    throw new IllegalArgumentException("pos may not be negative");
	//	if (isEmpty())
	//	    throw new RuntimeException("Row is empty");
	final int index = getPartIndexUnderPos(pos);
	if (index < 0)
    return null;
	return parts[index].run;
    }

    public Run[] getRuns()
    {
	//	if (isEmpty())
	//	    return new Run[0];
	final Vector<Run> res = new Vector<Run>();
	for(int i = partsFrom;i < partsTo;++i)
	{
	    final Run run = parts[i].run;
	    int k = 0;
	    for(k = 0;k < res.size();++k)
		if (res.get(k) == run)
		    break;
	    if (k >= res.size())
		res.add(run);
	}
	return res.toArray(new Run[res.size()]);
    }

    //returns -1 if index is invalid
    public int runBeginsAt(Run run)
    {
	NullCheck.notNull(run, "run");
	//	if (isEmpty())
	//	    throw new RuntimeException("Row is empty");
	int offset = 0;
	for(int i = partsFrom;i < partsTo;++i)
	{
	    final String text = parts[i].getText();
	    if (text == null || text.isEmpty())
		continue;
	    if (parts[i].run == run)
		return offset;
	    offset += text.length();
	}
	return offset;
    }

    //Row number in the paragraph
    public int getRelNum()
    {
	//	if (isEmpty())
	//	    throw new RuntimeException("Row is empty");
	return getFirstPart().relRowNum;
    }

    public Run getFirstRun()
    {
	//	if (isEmpty())
	//	    throw new RuntimeException("Row is empty");
	return getFirstPart().run;
    }

    public int getRowX()
    {
	return x;
    }

    public int getRowY()
    {
	return y;
    }

    private RowPart getFirstPart()
    {
	//	if (isEmpty())
	//	    throw new RuntimeException("Row is empty");
	return parts[partsFrom];
    }

    //returns -1 if there is no matching pos
    private int getPartIndexUnderPos(int pos)
    {
	if (pos < 0)
	    throw new IllegalArgumentException("pos may not be negative");
	//	if (isEmpty())
	//	    return -1;
	int offset = 0;
	for(int i = partsFrom;i < partsTo;++i)
	{
	    final String text = parts[i].getText();
	    if (text == null || text.isEmpty())
		continue;
	    if (pos >= offset && pos < offset + text.length())
		return i;
	    offset += text.length();
	}
	return -1;
    }
}

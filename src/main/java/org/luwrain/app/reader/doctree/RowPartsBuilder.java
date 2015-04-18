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

import java.util.*;

class RowPartsBuilder
{
    private int index = 0;
    private int offset = 0;
    private Vector<RowPart> parts = new Vector<RowPart>();

    //Removes spaces only on row breaks and only if after the break there are non-spacing chars;
    public void onRun(Run run, int maxRowLen)
    {
	final String text = run.text;
	if (text == null)
	    throw new NullPointerException("text may not be null");
	if (text.isEmpty())
	    return;
	int posFrom = 0;
	while (posFrom < text.length())
	{
	    final int available = maxRowLen - offset;
	    if (available <= 0)
	    {
		++index;
		offset = 0;
		continue;
	    }
	    final int remains = text.length() - posFrom;
	    //	    System.out.println("remains=" + remains);
	    //	    System.out.println("available=" + available);
	    //Both remains and available are greater than zero;
	    if (remains <= available)
	    {
		//We have a chunk for the last row for this run;
		save(makeRunPart(run, posFrom, text.length()));
		offset = remains;
		posFrom = text.length();
		continue;
	    }
	    int posTo = posFrom;
	    int nextWordEnd = posTo;
	    while (nextWordEnd - posFrom <= available)
	    {
		posTo = nextWordEnd;//It is definitely before the row end;
		while (nextWordEnd < text.length() && Character.isSpace(text.charAt(nextWordEnd)))
		    ++nextWordEnd;
		while (nextWordEnd < text.length() && !Character.isSpace(text.charAt(nextWordEnd)))
		    ++nextWordEnd;
	    }
	    if (posTo == posFrom)//No word ends before the row end;
		posTo = posFrom + available;
	    save(makeRunPart(run, posFrom, posTo));
	    ++index;
	    offset = 0;
	    posFrom = posTo;
	    //Trying to find the beginning of the next word;
	    final int rollBack = posFrom;
	    while (posFrom < text.length() && Character.isSpace(text.charAt(posFrom)))
		++posFrom;
	    if (posFrom >= text.length())
		posFrom = rollBack;
	}
    }

    private RowPart makeRunPart(Run run,
				 int posFrom,
				 int posTo)
    {
	RowPart part = new RowPart();
	part.run = run;
	part.rowNum = index;
	part.posFrom = posFrom;
	part.posTo = posTo;
	return part;
    }

    private void save(RowPart part)
    {
	if (part == null)
	    throw new NullPointerException("part may not be null");
	parts.add(part);
    }

    public RowPart[] parts()
    {
	if (parts == null || parts.isEmpty())
	    return new RowPart[0];
	return parts.toArray(new RowPart[parts.size()]);
    }
}

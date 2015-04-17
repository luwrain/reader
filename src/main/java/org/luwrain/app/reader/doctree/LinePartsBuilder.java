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

class LinePartsBuilder
{
    private int maxLineLen = 64;
    private int index = 0;
    private int offset = 0;
    private Vector<LinePart> parts = new Vector<LinePart>();

    public LinePartsBuilder(int maxLineLen)
    {
	this.maxLineLen = maxLineLen;
	if (maxLineLen <= 0)
	    throw new IllegalArgumentException("maxLineLen should be greater than zero (maxLineLen = " + maxLineLen + ")");
    }

    private void onNode(Node node)
    {
	if (node == null)
	    throw new NullPointerException("node may not be null");
	switch(node.type())
	{
	case Node.SECTION:
	    onSection(node);
	    break;
	case Node.PARAGRAPH:
	    onParagraph(node);
	    break;
	case Node.RUN:
	    onRun(node);
	    break;
	}
	final int count = node.subnodeCount();
	for (int i = 0;i < count;++i)
	    onNode(node.subnode(i));
    }

    private void onSection(Node node)
    {
	LinePart part = new LinePart();
	part.isSection = true;
	part.isHRef = !node.href().isEmpty(); 
    }

    public void onParagraph(Node node)
    {
	offset = 0;
	index += 2;
    }

    //Removes spaces only on line breaks and only if after the break there are non-spacing chars;
    public void onRun(Node node)
    {
	//	System.out.println("new");
	final String text = node.text();
	if (text == null)
	    throw new NullPointerException("text may not be null");
	if (text.isEmpty())
	    return;
	int posFrom = 0;
	while (posFrom < text.length())
	{
	    final int available = maxLineLen - offset;
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
		//We have a chunk for the last line for this run;
		save(makeRunPart(node, posFrom, text.length()));
		offset = remains;
		posFrom = text.length();
		continue;
	    }
	    int posTo = posFrom;
	    int nextWordEnd = posTo;
	    while (nextWordEnd - posFrom <= available)
	    {
		posTo = nextWordEnd;//It is definitely before the line end;
		while (nextWordEnd < text.length() && Character.isSpace(text.charAt(nextWordEnd)))
		    ++nextWordEnd;
		while (nextWordEnd < text.length() && !Character.isSpace(text.charAt(nextWordEnd)))
		    ++nextWordEnd;
	    }
	    if (posTo == posFrom)//No word ends before the line end;
		posTo = posFrom + available;
	    save(makeRunPart(node, posFrom, posTo));
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

    private LinePart makeRunPart(Node node,
				 int posFrom,
				 int posTo)
    {
	LinePart part = new LinePart();
	part.node = node;
	part.lineNum = index;
	part.posFrom = posFrom;
	part.posTo = posTo;
	part.textAttr = node.textAttr();
	part.isHRef = !node.href().isEmpty();
	return part;
    }

    private void save(LinePart part)
    {
	if (part == null)
	    throw new NullPointerException("part may not be null");
	parts.add(part);
    }

    public LinePart[] parts()
    {
	if (parts == null || parts.isEmpty())
	    return new LinePart[0];
	return parts.toArray(new LinePart[parts.size()]);
    }
}

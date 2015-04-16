/*
   Copyright 2012-2015 Michael Pozhidaev <msp@altlinux.org>

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

package org.luwrain.app.preview.doctree;

class LinePartsBuilder
{
    private int maxLineLen = 64;
    private int index = 1;
    private int offset = 0;

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

    public void onRun(Node node)
    {
	final String text = node.text();
	int startFrom = offset;	
	int pos = 0;
	while (pos < text.length())
	{
	    int i = pos;
	    while (i < text.length() && Character.isSpace(text.charAt(i)))
		++i;
	    if (i >= text.length())
		break;
	    while (i < text.length() && !Character.isSpace(text.charAt(i)))
		++i;
	    if (i - startFrom + offset >  maxLineLen)
	    {
		LinePart part = new LinePart();
		part.node = node;
		part.posFrom = startFrom;
		part.posTo = pos;
		part.textAttr = node.textAttr();
		save(part);
		offset = 0;
		++index;
		startFrom = pos;
		while (startFrom < text.length() && Character.isSpace(text.charAt(startFrom)))
		    ++startFrom;
		continue;
	    }
	    pos = i;
	}
    }

    private void save(LinePart part)
    {
    }
}

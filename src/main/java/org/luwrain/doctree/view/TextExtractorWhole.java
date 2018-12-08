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

package org.luwrain.doctree.view;

import java.util.*;

import org.luwrain.core.*;
import org.luwrain.doctree.*;

public final class TextExtractorWhole extends TextExtractor
{
    static private final String LOG_COMPONENT = "document";

    private final int width;
    private final List<RowPart> parts = new LinkedList();

    public TextExtractorWhole(int width)
    {
	if (width < 0)
	    throw new IllegalArgumentException("width (" + width + ") may not be negative");
	this.width = width;
    }

    public void onNode(Node node)
    {
	NullCheck.notNull(node, "node");
	if (node instanceof EmptyLine)
	{
	    addEmptyLine();
	    return;
	}
   	if (node instanceof Paragraph)
	{
	    onParagraph((Paragraph)node);
	    addEmptyLine();
	    return;
	}
	for(Node n: node.getSubnodes())
	    onNode(n);
    }

    private void onParagraph(Paragraph para)
    {
	NullCheck.notNull(para, "para");
	final RowPartsSplitter splitter = new RowPartsSplitter();
	for(Run r: para.runs())
	{
	    final String text = r.text();
	    NullCheck.notNull(text, "text");
	    splitter.onRun(r, text, 0, text.length(), width);
	}
	if (splitter.res.isEmpty())
	    return;
	onParagraphLines(splitter.res.toArray(new RowPart[splitter.res.size()]));
    }
}

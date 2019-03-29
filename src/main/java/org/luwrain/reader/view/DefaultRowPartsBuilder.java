/*
   Copyright 2012-2019 Michael Pozhidaev <michael.pozhidaev@gmail.com>
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

package org.luwrain.reader.view;

import java.util.*;

import org.luwrain.core.*;
import org.luwrain.reader.*;

final class DefaultRowPartsBuilder
{
    static private final String LOG_COMPONENT = "doctree";

    private final List<RowPart> parts = new LinkedList();
    private final List<Paragraph> paragraphs = new LinkedList<Paragraph>();

void onNode(Node node)
    {
	NullCheck.notNull(node, "node"); 
	onNode(node, 0);
    }

void onNode(Node node, int width)
    {
	NullCheck.notNull(node, "node");
	if (node instanceof EmptyLine)
	{
	    final Paragraph para = (Paragraph)node;
	    final RowPart part = new RowPart(para.getRuns()[0]);
	    para.setRowParts(new RowPart[]{part});
	    parts.add(part);
	    return;
	}
   	if (node instanceof Paragraph)
	{
	    onParagraph((Paragraph)node, width);
	    return;
	}
	for(Node n: node.getSubnodes())
		onNode(n);
    }

    private void onParagraph(Paragraph para, int width)
    {
	NullCheck.notNull(para, "para");
	final RowPartsSplitter splitter = new RowPartsSplitter();
	for(Run r: para.getRuns())
	{
	    final String text = r.text();
	    NullCheck.notNull(text, "text");
	    splitter.onRun(r, text, 0, text.length(), width > 0?width:para.width);
	}
	if (!splitter.res.isEmpty())
	{
	    para.setRowParts(splitter.res.toArray(new RowPart[splitter.res.size()]));
	    paragraphs.add(para);
	    for(RowPart p: splitter.res)
		parts.add(p);
	}
    }
    static private RowPart makeTitlePart(Run run)
    {
	NullCheck.notNull(run, "run");
return new RowPart(run);
    }

RowPart[] getRowParts()
    {
	return parts.toArray(new RowPart[parts.size()]);
    }

Paragraph[] getParagraphs()
    {
	return paragraphs.toArray(new Paragraph[paragraphs.size()]);
    }
}

/*
   Copyright 2012-2021 Michael Pozhidaev <msp@luwrain.org>
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

package org.luwrain.app.reader;

import org.luwrain.reader.*;
import org.luwrain.reader.view.*;

class NarratorTextVisitor implements Visitor
{
    private final int paraPause = 500;

    private final StringBuilder builder = new StringBuilder();

    @Override public void visitNode(Node node)
    {
    }

    @Override public void visit(ListItem node)
    {
	builder.append("Элемент списка");
    }

    @Override public void visit(Paragraph para)
    {
	final String[] lines = org.luwrain.reader.view.View.getParagraphLines(para, 80);
	if (lines.length < 1)
	    return;
	for(String s: lines)
	    builder.append(s + "\n");
	builder.append("#" + paraPause + "\n\n");
    }

    @Override public void visit(Section node)
    {
	builder.append("Заголовок ");
    }

    @Override public void visit(TableCell node)
    {
    }

    @Override public void visit(Table node)
    {
    }

    @Override public void visit(TableRow node)
    {
    }

    @Override public String toString()
    {
	return new String(builder);
    }
}

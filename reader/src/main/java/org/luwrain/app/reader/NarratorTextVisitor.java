// SPDX-License-Identifier: BUSL-1.1
// Copyright 2012-2026 Michael Pozhidaev <msp@luwrain.org>

package org.luwrain.app.reader;

import org.luwrain.io.bookdoc.*;
import org.luwrain.io.bookdoc.view.*;

class NarratorTextVisitor extends Visitor
{
    private final int paraPause = 500;

    private final StringBuilder builder = new StringBuilder();

    @Override public void visit(Heading h)
    {
	builder.append("Заголовок ");
    }

    @Override public void visit(Paragraph para)
    {
	final String[] lines = View.getParagraphLines(para, 80);
	if (lines.length < 1)
	    return;
	for(String s: lines)
	    builder.append(s + "\n");
	builder.append("#" + paraPause + "\n\n");
    }

    @Override public String toString()
    {
	return new String(builder);
    }
}

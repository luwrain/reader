
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

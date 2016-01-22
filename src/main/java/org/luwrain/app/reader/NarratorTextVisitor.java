
package org.luwrain.app.reader;

import org.luwrain.doctree.*;

class NarratorTextVisitor implements Visitor
{
    private final int paraPause = 500;

    private final StringBuilder builder = new StringBuilder();

    @Override public void visitNode(NodeImpl node)
    {
    }

    @Override public void visit(ListItem node)
    {
	builder.append("Элемент списка");
    }

    @Override public void visit(ParagraphImpl para)
    {
	final String[] lines = RowPartsBuilder.paraToLines(para, 80);
	if (lines.length < 1)
	    return;
	for(String s: lines)
	    builder.append(s + "\n");
	builder.append("#" + paraPause + "\n\n");
    }

    @Override public void visit(Section node)
    {
	builder.append("Заголовок");
    }

    @Override public void visit(TableCell node)
    {
	builder.append("Ячейка таблицы");
    }

    @Override public void visit(Table node)
    {
	builder.append("Таблица\n");
    }

    @Override public void visit(TableRow node)
    {
	builder.append("Строка таблицы\n");
    }

    @Override public String toString()
    {
	return new String(builder);
    }
}

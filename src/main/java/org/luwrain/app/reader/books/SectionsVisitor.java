
package org.luwrain.app.reader.books;

import java.util.*;

import org.luwrain.core.*;
import org.luwrain.doctree.*;

class SectionsVisitor implements Visitor
{
    private final LinkedList<Book.Section> sections = new LinkedList<Book.Section>();

    @Override public void visit(Section node)
    {
	NullCheck.notNull(node, "node");
	final LinkedList<String> hrefs = new LinkedList<String>();
	collectHrefs(node, hrefs);
	if (!hrefs.isEmpty())
	    sections.add(new Book.Section(node.getSectionLevel(), node.getCompleteText().trim(), hrefs.getFirst()));
    }

    Book.Section[] getBookSections()
    {
	return sections.toArray(new Book.Section[sections.size()]);
    }

    @Override public void visitNode(Node node) {}
    @Override public void visit(ListItem node) {}
    @Override public void visit(Paragraph node) {}

    @Override public void visit(TableCell node) {}
    @Override public void visit(Table node) {}
    @Override public void visit(TableRow node) {}

    static private void collectHrefs(Node node, LinkedList<String> hrefs)
    {
	NullCheck.notNull(node, "node");
	NullCheck.notNull(hrefs, "hrefs");
	if (node instanceof Paragraph)
	{
	    final Paragraph para = (Paragraph)node;
	    if (para.runs != null)
		for(Run r: para.runs)
		    if (r.href() != null && !r.href().isEmpty())
			hrefs.add(r.href());
	} else
	    for(Node n: node.getSubnodes())
		    collectHrefs(n, hrefs);
    }
}

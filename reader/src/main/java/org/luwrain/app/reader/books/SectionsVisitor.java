// SPDX-License-Identifier: BUSL-1.1
// Copyright 2012-2026 Michael Pozhidaev <msp@luwrain.org>

package org.luwrain.app.reader.books;

import java.util.*;

import org.luwrain.core.*;
import org.luwrain.io.bookdoc.*;

import static java.util.Objects.*;

class SectionsVisitor extends Visitor
{
    private final LinkedList<Book.Section> sections = new LinkedList<Book.Section>();

    @Override public void visit(Heading h)
    {
	requireNonNull(h, "h can't be null");
	final List<String> hrefs = new ArrayList<>();
	final Visitor hrefsVisitor = new Visitor(){
		@Override public void visit(Run run)
		{
		    if (run.getHref() != null && !run.getHref().isEmpty())
			hrefs.add(run.getHref());
		}
	    };
	Visitor.walk(h, hrefsVisitor);
	if (!hrefs.isEmpty())
	    sections.add(new Book.Section(h.getLevel(), h.getText(), hrefs.get(0)));
    }

    Book.Section[] getBookSections()
    {
	return sections.toArray(new Book.Section[sections.size()]);
    }
}

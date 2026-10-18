// SPDX-License-Identifier: BUSL-1.1
// Copyright 2012-2026 Michael Pozhidaev <msp@luwrain.org>

package org.luwrain.controls.reader;

import org.luwrain.core.*;
import org.luwrain.controls.*;
import org.luwrain.io.bookdoc.*;
import org.luwrain.io.bookdoc.view.*;

import static java.util.Objects.*;

/**
 * The default implementation of {@link ReaderArea.Announcement}.
 *
 * <p>This implementation announces the current row. When the row begins a new
 * paragraph inside a table cell, it prepends a brief cell description when
 * table cell indices are available.</p>
 */
public class DefaultAnnouncement implements ReaderArea.Announcement
{
    protected final ControlContext context;
    protected final Strings strings;

    /**
     * Creates an announcement provider.
     *
     * @param context the control context; must not be {@code null}
     * @param strings the localizable strings; must not be {@code null}
     * @throws NullPointerException if {@code context} or {@code strings} is {@code null}
     */
    public DefaultAnnouncement(ControlContext context, Strings strings)
    {
	requireNonNull(context, "context can't be null");
	requireNonNull(strings, "strings can't be null");
	this.context = context;
	this.strings = strings;
    }

    @Override public void announce(Iterator it, boolean brief)
    {
	requireNonNull(it, "it can't be null");
	if (it.noContent())
	    return;
	final Container node = it.getContainer();
	if (node == null)
	{
	    context.setEventResponse(DefaultEventResponse.text(it.getText()));
	    return;
	}
	if (brief)
	{
	    context.setEventResponse(DefaultEventResponse.text(it.getText()));
	    return;
	}
	final String text = getAnnouncementText(it);
	if (!text.isEmpty())
	    context.setEventResponse(DefaultEventResponse.text(text)); else
	    context.setEventResponse(DefaultEventResponse.text(it.getText()));
    }

    /**
     * Builds the full announcement text for the supplied iterator position.
     *
     * @param it the iterator position; must not be {@code null}
     * @return the announcement text; never {@code null}
     */
    protected String getAnnouncementText(Iterator it)
    {
	requireNonNull(it, "it can't be null");
	final Container node = it.getContainer();
	if (node == null)
	    return "";
	final StringBuilder b = new StringBuilder();
	if (it.isParagraphBeginning())
	{
	    final Paragraph para = it.getParagraph();
	    if (para != null)
	    {
		final Container parent2 = para.getContainer();
		if (parent2 instanceof TableCell)
		{
		    final String intro = getTableCellIntro((TableCell)parent2);
		    if (!intro.isEmpty())
		    {
			if (b.length() > 0)
			    b.append(" ");
			b.append(intro);
		    }
		}
	    }
	}
	if (b.length() > 0)
	    b.append(" ");
	b.append(it.getText());
	return new String(b);
    }

    private String getTableCellIntro(TableCell cell)
    {
	requireNonNull(cell, "cell can't be null");
	try {
	    final int row = cell.getRowIndex();
	    final int col = cell.getColIndex();
	    return strings.tableCellIntro(row, col);
	}
	catch(IllegalStateException e)
	{
	    // Table cells do not provide indices yet in the book model.
	    // Ignore this optional description until getRowIndex() and
	    // getColIndex() are fully implemented.
	    return "";
	}
    }
}

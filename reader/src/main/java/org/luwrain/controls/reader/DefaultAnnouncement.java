// SPDX-License-Identifier: BUSL-1.1
// Copyright 2012-2026 Michael Pozhidaev <msp@luwrain.org>

package org.luwrain.controls.reader;

import org.luwrain.core.*;
import org.luwrain.controls.*;
import org.luwrain.io.bookdoc.*;
import org.luwrain.io.bookdoc.view.*;

import static java.util.Objects.*;

public class DefaultAnnouncement implements ReaderArea.Announcement
{
    protected final ControlContext context;
    protected final Strings strings;

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
	final Node node = it.getNode();
	if (node == null)
	    return;
	if (brief)
	    context.setEventResponse(DefaultEventResponse.text(it.getText())); else
	    context.setEventResponse(DefaultEventResponse.text(getAnnouncementText(it)));
    }

    protected String getAnnouncementText(Iterator it)
    {
	requireNonNull(it, "it can't be null");
	final Node node = it.getNode();
	if (node == null)
	    return "";
	final StringBuilder b = new StringBuilder();
	if (it.isParagraphBeginning())
	{
	    final Paragraph para = it.getParagraph();
	    if (para != null)
	    {
		final Node parent2 = para.getParentNode();
		if (parent2 instanceof TableCell)
		{
		    final TableCell cell = (TableCell)parent2;
		    b.append(strings.tableCellIntro(cell.getRowIndex(), cell.getColIndex()));
		}
	    }
	}
	b.append(it.getText());
	return new String(b);
    }
}

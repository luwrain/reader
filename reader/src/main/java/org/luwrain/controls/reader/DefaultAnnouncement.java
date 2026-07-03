package org.luwrain.controls.reader;

import org.luwrain.core.*;
import org.luwrain.io.bookdoc.*;
import org.luwrain.io.bookdoc.view.*;

public class DefaultAnnouncement implements ReaderArea.Announcement
{
    protected final ControlContext context;
    protected final Strings strings;

    public DefaultAnnouncement(ControlContext context, Strings strings)
    {
	NullCheck.notNull(context, "context");
	NullCheck.notNull(strings, "strings");
	this.context = context;
	this.strings = strings;
    }

    @Override public void announce(Iterator it, boolean brief)
    {
	NullCheck.notNull(it, "it");
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
	NullCheck.notNull(it, "it");
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

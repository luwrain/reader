
//LWR_API 1.0

package org.luwrain.controls.reader;

import org.luwrain.core.*;
import org.luwrain.controls.*;
import org.luwrain.reader.*;
import org.luwrain.reader.view.Iterator;

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

    @Override public void announce(Iterator it, boolean briefIntroduction)
    {
	NullCheck.notNull(it, "it");
	if (it.noContent())
	{
	    context.setEventResponse(DefaultEventResponse.hint(Hint.EMPTY_LINE));
	    return;
	}
	if (it.getNode() == null)
	{
	context.say(it.getText());
	return;
	}
	if (it.isTitleRow())
	{
	    onTitle(it);
	    return;
	}
Node node = getDominantNode(it);
	if (node != null)
	{
	    if (node instanceof TableCell)
		onTableCell((TableCell)node);
	    return;
	}
	announceText(it);
    }

    protected void onTitle(Iterator it)
    {
	context.say("title");
	/*
	final Node node = it.getNode();
		    if (node instanceof TableCell)
			onTableCell((TableCell)node); else
		    {
			context.say("title");
		    }
	*/
    }

    

protected void onTableCell(TableCell cell)
    {
	NullCheck.notNull(cell, "cell");
	final TableRow row = (TableRow)cell.getParentNode();
	final int rowIndex = cell.getRowIndex();
	final int colIndex = cell.getColIndex();
	if (rowIndex == 0 && colIndex == 0)
	{
	    context.say(row.getCompleteText() + " Начало таблицы", Sounds.TABLE_CELL);
	    return;
}
	if (colIndex == 0)
	{
	    context.say(row.getCompleteText() + " строка " + (rowIndex + 1) , Sounds.TABLE_CELL);
	    return;
	}
	context.say("столбец " + (colIndex + 1), Sounds.TABLE_CELL);
    }

    private void announceText(Iterator it)
    {
	NullCheck.notNull(it, "it");
	//Checking if there is nothing to say
	if (it.getText().trim().isEmpty())
	{
	    context.setEventResponse(DefaultEventResponse.hint(Hint.EMPTY_LINE));
	    return;
	}
	//Checking should we use any specific sound
final Sounds sound;
	if (it.getIndexInParagraph() == 0 && it.getNode() != null)
	{
		switch(it.getNode().getType())
		{
		case SECTION:
		    sound = Sounds.DOC_SECTION;
		    break;
		case LIST_ITEM:
		    sound = Sounds.LIST_ITEM;
		    break;
		default:
		    sound = null;
		}
	} else
	    sound = null;
	//Speaking with sound if we have chosen any
	if (sound != null)
	{
	    context.say(it.getText(), sound);
	    return;
	}
	//Speaking with paragraph sound if it is a first row

	if (it.getIndexInParagraph() == 0)
	    context.say(it.getText(), Sounds.PARAGRAPH); else
		context.say(it.getText());
    }

    protected Node getDominantNode(Iterator it)
    {
	NullCheck.notNull(it, "it");
	final Node res = findDominantNode(it);
	if (res == null)
	    return null;
	if (res instanceof TableRow)
	{
	    if (!res.noSubnodes())
		return res.getSubnode(0);
	    return res;
	}
	if (res instanceof Table)
	{
	    if (!res.noSubnodes() &&!res.getSubnode(0).noSubnodes())
		return res.getSubnode(0).getSubnode(0);
	return res;
	}
	/*
    if (res instanceof OrderedList || res instanceof UnorderedList)
    {
	if (!res.noContent())
	    return res.getSubnode(0);
	return res;
    }
	*/
    return res;
    }

    protected Node findDominantNode(Iterator it)
    {
	NullCheck.notNull(it, "it");
	if (it.getIndexInParagraph() != 0)
	    return null;
	Node node = it.getNode();
	if (node.getIndexInParentSubnodes() != 0)
	    return null;
	node = node.getParentNode();
	while (node != null)
	{
	    if (node.getIndexInParentSubnodes() != 0)
		return node;
	    node = node.getParentNode();
	}
	
	return null;
    }
}

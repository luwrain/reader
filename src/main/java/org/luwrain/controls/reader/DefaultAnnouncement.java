
//LWR_API 1.0

package org.luwrain.controls.reader;

import org.luwrain.core.*;
import org.luwrain.controls.*;
import org.luwrain.reader.*;
import org.luwrain.reader.view.Iterator;

public class DefaultAnnouncement implements ReaderArea.Announcement
{
    protected final ControlContext environment;
    protected final Strings strings;

    public DefaultAnnouncement(ControlContext environment, Strings strings)
    {
	NullCheck.notNull(environment, "environment");
	NullCheck.notNull(strings, "strings");
	this.environment = environment;
	this.strings = strings;
    }

    public void announce(Iterator it, boolean briefIntroduction)
    {
	NullCheck.notNull(it, "it");
	if (it.noContent())
	{
	    environment.setEventResponse(DefaultEventResponse.hint(Hint.EMPTY_LINE));
	    return;
	}
	if (it.getNode() == null)
	{
	environment.say(it.getText());
	return;
	}
	if (it.isTitleRow())
	{
	    onTitle(it);
	    return;
	}
	announceText(it);
    }

    protected void onTitle(Iterator it)
    {
	final Node node = it.getNode();
		    if (node instanceof TableCell)
			onTableCell(it); else
		    {
			environment.say("title");
		    }
    }

protected void onTableCell(Iterator it)
    {
	final TableCell cell = (TableCell)it.getNode();
	final TableRow row = (TableRow)cell.getParentNode();
	final int rowIndex = cell.getRowIndex();
	final int colIndex = cell.getColIndex();
	if (rowIndex == 0 && colIndex == 0)
	{
	    environment.say("Начало таблицы " + row.getCompleteText(), Sounds.TABLE_CELL);
	    return;
}
	if (colIndex == 0)
	{
	    environment.say("строка " + (rowIndex + 1) + row.getCompleteText(), Sounds.TABLE_CELL);
	    return;
	}
	environment.say("столбец " + (colIndex + 1), Sounds.TABLE_CELL);
    }

    private void announceText(Iterator it)
    {
	NullCheck.notNull(it, "it");
	//Checking if there is nothing to say
	if (it.getText().trim().isEmpty())
	{
	    environment.setEventResponse(DefaultEventResponse.hint(Hint.EMPTY_LINE));
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
	    environment.say(it.getText(), sound);
	    return;
	}
	//Speaking with paragraph sound if it is a first row

	if (it.getIndexInParagraph() == 0)
	    environment.say(it.getText(), Sounds.PARAGRAPH); else
		environment.say(it.getText());
    }
}

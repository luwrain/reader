/*
   Copyright 2012-2018 Michael Pozhidaev <michael.pozhidaev@gmail.com>
   Copyright 2015-2016 Roman Volovodov <gr.rPman@gmail.com>

   This file is part of LUWRAIN.

   LUWRAIN is free software; you can redistribute it and/or
   modify it under the terms of the GNU General Public
   License as published by the Free Software Foundation; either
   version 3 of the License, or (at your option) any later version.

   LUWRAIN is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
   General Public License for more details.
*/

//LWR_API 1.0

package org.luwrain.controls.doc;

import org.luwrain.core.*;
import org.luwrain.controls.*;
import org.luwrain.reader.*;
import org.luwrain.reader.view.Iterator;
//import org.luwrain.doctree.view.Layout;

public class Announcement
{
    protected final ControlEnvironment environment;
    protected final Strings strings;

    public Announcement(ControlEnvironment environment, Strings strings)
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

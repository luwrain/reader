/*
   Copyright 2012-2016 Michael Pozhidaev <michael.pozhidaev@gmail.com>

   This file is part of the LUWRAIN.

   LUWRAIN is free software; you can redistribute it and/or
   modify it under the terms of the GNU General Public
   License as published by the Free Software Foundation; either
   version 3 of the License, or (at your option) any later version.

   LUWRAIN is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
   General Public License for more details.
*/

package org.luwrain.app.reader;

import org.luwrain.core.*;
import org.luwrain.controls.*;
import org.luwrain.doctree.*;

class Announcement
{
    private ControlEnvironment environment;
    private Strings strings;

    Announcement(ControlEnvironment environment, Strings strings)
    {
	NullCheck.notNull(environment, "environment");
	NullCheck.notNull(strings, "strings");
	this.environment = environment;
	this.strings = strings;
    }

    void announce(Iterator it, boolean briefIntroduction)
    {
	NullCheck.notNull(it, "it");
	if (it.noContent() || it.isEmptyRow())
	{
	    environment.hint(Hints.EMPTY_LINE);
	    return;
	}
	if (it.isTitleRow())
	{
	    onTitle(it);
	    return;
	}
	final Node node = it.getParaContainer();
	if (node == null)
	{
	environment.say(it.getText());
	return;
	}
	if (node instanceof ListItem)
	{
	    if (!Layout.hasTitleRun(node))
		environment.say(it.getText(), Sounds.LIST_ITEM); else
		environment.say(it.getText());
	    return;
	}

	if (node instanceof Section)
	{
	    if (!Layout.hasTitleRun(node))
		environment.say(it.getText(), Sounds.DOC_SECTION); else
		environment.say(it.getText());
	    return;
	}


		environment.say(it.getText());
    }

    private void onTitle(Iterator it)
    {
	final Node node = it.getTitleParentNode();
	if (node.getType() == Node.Type.ORDERED_LIST)
	    onOrderedList(it); else
	    if (node.getType() == Node.Type.UNORDERED_LIST)
		onUnorderedList(it); else
		if (node instanceof ListItem)
		    onListItem(it); else
		    if (node instanceof TableCell)
			onTableCell(it); else
		    {
			environment.say("title");
		    }
    }

    private void onOrderedList(Iterator it)
    {
	environment.say("Нумерованный список");
    }

    private void onUnorderedList(Iterator it)
    {
	environment.say("Ненумерованный список");
    }

    private void onListItem(Iterator it)
    {
	//	final Node node = it.getTitleParentNode();
	environment.say("Элемент списка ", Sounds.LIST_ITEM);
    }

    private void onTableCell(Iterator it)
    {it.getTitleParentNode();
	final TableCell cell = (TableCell)it.getTitleParentNode();
	final int rowIndex = cell.getRowIndex();
	final int colIndex = cell.getColIndex();
	if (rowIndex == 0 && colIndex == 0)
	{
	    environment.say("Начало таблицы");
	    return;
}
	environment.say("Строка " + (rowIndex + 1) + ", столбец " + (colIndex + 1));
    }
}

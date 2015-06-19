/*
   Copyright 2012-2015 Michael Pozhidaev <michael.pozhidaev@gmail.com>

   This file is part of the Luwrain.

   Luwrain is free software; you can redistribute it and/or
   modify it under the terms of the GNU General Public
   License as published by the Free Software Foundation; either
   version 3 of the License, or (at your option) any later version.

   Luwrain is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
   General Public License for more details.
*/

package org.luwrain.app.reader;

import org.luwrain.core.*;
import org.luwrain.controls.*;

import org.luwrain.app.reader.doctree.*;

class Introduction
{
    private ControlEnvironment environment;
    private Strings strings;

    public Introduction(ControlEnvironment environment, Strings strings)
    {
	this.environment = environment;
	this.strings = strings;
	if (environment == null)
	    throw new NullPointerException("environment may not be null");
	if (strings == null)
	    throw new NullPointerException("strings may not be null");
    }

    public void introduce(Iterator iterator, boolean briefIntroduction)
    {
	if (briefIntroduction ||
	    iterator.isCurrentRowEmpty() ||
	    iterator.getCurrentRowRelIndex() != 0 &&
	    iterator.getCurrentParagraphIndex() != 0)
	    simple(iterator); else
	    advanced(iterator);
    }

    private void advanced(Iterator iterator)
    {
	if (iterator.isCurrentParaContainerTableCell())
	    inTableCell(iterator); else
	    if (iterator.isCurrentParaContainerListItem())
		inListItem(iterator); else
		environment.say(iterator.getCurrentText());
    }

    private void inListItem(Iterator iterator)
    {
	final int itemIndex = iterator.getListItemIndexOfCurrentParaContainer();
	final String text = iterator.getCurrentText();
	if (iterator.isListOfCurrentParaContainerOrdered())
	    environment.say(strings.orderedListItemIntroduction(itemIndex, text)); else
	    environment.say(strings.unorderedListItemIntroduction(itemIndex, text));
    }

    private void inTableCell(Iterator iterator)
    {
	final Table table = iterator.getTableOfCurrentParaContainer();
	final int level = table.getTableLevel();
	final Node cell = iterator.getCurrentParaContainer();
	final int colIndex = table.getColIndexOfCell(cell);
	final int rowIndex = table.getRowIndexOfCell(cell);
	final int colCount = table.getColCount();
	final int rowCount = table.getRowCount();
	String text = "";
	//If the row has only one line in height we speak all cells of this line;
	if (colIndex == 0 && table.isSingleLineRow(rowIndex))
	{
	    StringBuilder s = new StringBuilder();
	    for(int i = 0;i < colCount;++i)
	    {
		final Node n = table.getCell(i, rowIndex);
		if (n != null)
		    System.out.println("n=" + n.toString());
	    if (n != null)
		s.append(n.toString());
	    }
	    text = s.toString();
	} else
	    text = iterator.getCurrentText();
	if (colIndex == 0 && rowIndex == 0)
	{
	    if (level > 1)
		environment.say(strings.tableIntroductionWithLevel(level, rowCount, colCount, text)); else 
	    environment.say(strings.tableIntroduction(rowCount, colCount, text));
	} else
	    environment.say(strings.tableCellIntroduction(rowIndex + 1, colIndex + 1, text));
    }

    private void simple(Iterator iterator)
    {
	if (!iterator.isCurrentRowEmpty())
	    environment.say(iterator.getCurrentText()); else
	    environment.hint(Hints.EMPTY_LINE);
    }
}

/*
   Copyright 2012-2015 Michael Pozhidaev <michael.pozhidaev@gmail.com>

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

class Introduction implements RowIntroduction
{
    static private final String LINK_PREFIX = " ссылка ";//FIXME:

    private ControlEnvironment environment;
    private Strings strings;

    Introduction(ControlEnvironment environment, Strings strings)
    {
	this.environment = environment;
	this.strings = strings;
	NullCheck.notNull(environment, "environment");
	NullCheck.notNull(strings, "strings");
    }

    @Override public void introduce(Iterator iterator, boolean briefIntroduction)
    {
	if (briefIntroduction)
	{
	    brief(iterator);
	    return;
	}
	if (iterator.isCurrentRowEmpty() ||
	    !iterator.isCurrentRowFirst())
	    simple(iterator); else
	    advanced(iterator);
    }

    private void advanced(Iterator iterator)
    {
	if (iterator.getCurrentParaIndex() > 0)
    {
	inParagraph(iterator);
	return;
    }
	if (iterator.isCurrentParaContainerTableCell())
	    inTableCell(iterator); else
	    if (iterator.isCurrentParaContainerSection())
		inSection(iterator); else
		if (iterator.isCurrentParaContainerListItem())
		    inListItem(iterator); else
		    inParagraph(iterator);
}

private void inParagraph(Iterator iterator)
{
    {
		final ParagraphImpl para = iterator.getCurrentParagraph();
		if (para.hasSingleLineOnly())
		environment.say(iterator.getCurrentText()); else
		    environment.say(strings.paragraphIntroduction(iterator.getCurrentText()));
	    }

}

    private void inListItem(Iterator iterator)
    {
	if (!iterator.isCurrentParaContainerListItem())
	    throw new IllegalArgumentException("Iterator isn\'t in list item");
	final ListItem item = iterator.getListItem();
	final int itemIndex = item.getListItemIndex();
	final String text = iterator.getCurrentText();
	if (item.isListOrdered())
	    environment.say(strings.orderedListItemIntroduction(itemIndex, text)); else
	    environment.say(strings.unorderedListItemIntroduction(itemIndex, text));
    }

    private void inSection(Iterator iterator)
    {
	final Section sect = iterator.getSection();
	final String text = iterator.getCurrentText();
	environment.say("Заголовок уровня " + sect.getSectionLevel() + " " + text);
    }

    private void inTableCell(Iterator iterator)
    {
	final TableCell cell = iterator.getTableCell();
	final Table table = cell.getTable();
	final int level = table.getTableLevel();
	final int colIndex = cell.getColIndex();
	final int rowIndex = cell.getRowIndex();
	final int colCount = table.getColCount();
	final int rowCount = table.getRowCount();

	if (rowCount < 2)
	{
	    simple(iterator);
	    return;
	}
	String text = "";
	//If the row has only one line in height we speak all cells of this line;
	/*
	if (colIndex == 0 && table.isSingleLineRow(rowIndex))
	{
	    StringBuilder s = new StringBuilder();
	    for(int i = 0;i < colCount;++i)
	    {
		final TableCell n = table.getCell(i, rowIndex);
	    if (n != null)
		s.append(n.toString() + " ");
	    }
	    text = s.toString();
	} else
	*/
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
	if (!iterator.isCurrentRowEmpty() || iterator.getCurrentText().trim().isEmpty())
	    environment.say(iterator.getCurrentTextWithHref(LINK_PREFIX)); else
	    environment.hint(Hints.EMPTY_LINE);
    }

    private void brief(Iterator iterator)
    {
	if (!iterator.isCurrentRowEmpty())
	    environment.say(iterator.getCurrentText()); else
	    environment.hint(Hints.EMPTY_LINE);
    }

}

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

class Introduction implements RowIntroduction
{
    //    static private final String LINK_PREFIX = " ссылка ";//FIXME:

    private ControlEnvironment environment;
    private Strings strings;

    Introduction(ControlEnvironment environment, Strings strings)
    {
	this.environment = environment;
	this.strings = strings;
	NullCheck.notNull(environment, "environment");
	NullCheck.notNull(strings, "strings");
    }

    @Override public void introduce(Iterator it, boolean briefIntroduction)
    {
	if (briefIntroduction)
	{
	    brief(it);
	    return;
	}
	if (it.isEmptyRow() ||
	    !it.isFirstRow())
	    simple(it); else
	    advanced(it);
    }

    private void advanced(Iterator it)
    {
	if (it.getParaIndex() > 0)
    {
	inParagraph(it);
	return;
    }
	if (it.isContainerTableCell())
	    inTableCell(it); else
	    if (it.isContainerSection())
		inSection(it); else
		if (it.isContainerListItem())
		{
		    final ListItem listItem = it.getListItem();
		    if (listItem .getParentListItemCount() > 1)
			inListItem(it); else
			inParagraph(it);
		} else
		    inParagraph(it);
}

private void inParagraph(Iterator it)
{
    {
		final ParagraphImpl para = it.getParagraph();
		if (para.hasSingleLineOnly())
		environment.say(it.getText()); else
		    environment.say(strings.paragraphIntroduction(it.getText()));
	    }
}

    private void inListItem(Iterator it)
    {
	final ListItem item = it.getListItem();
	final int itemIndex = item.getListItemIndex();
	final String text = it.getText();
	environment.playSound(Sounds.NEW_LIST_ITEM);
	if (item.isListOrdered())
	    environment.say(strings.orderedListItemIntroduction(itemIndex, text)); else
	    environment.say(strings.unorderedListItemIntroduction(itemIndex, text));
    }

    private void inSection(Iterator it)
    {
	final Section sect = it.getSection();
	final String text = it.getText();
	environment.playSound(Sounds.DOC_SECTION);
	environment.say(strings.sectionIntroduction(sect.getSectionLevel(), text));
    }

    private void inTableCell(Iterator it)
    {
	final TableCell cell = it.getTableCell();
	final Table table = cell.getTable();
	final int level = table.getTableLevel();
	final int colIndex = cell.getColIndex();
	final int rowIndex = cell.getRowIndex();
	final int colCount = table.getColCount();
	final int rowCount = table.getRowCount();
	if (rowCount < 2)
	{
	    simple(it);
	    return;
	}
	String text = "";
	    text = it.getText();
	if (colIndex == 0 && rowIndex == 0)
	{
	    if (level > 1)
		environment.say(strings.tableIntroductionWithLevel(level, rowCount, colCount, text)); else 
	    environment.say(strings.tableIntroduction(rowCount, colCount, text));
	} else
	    environment.say(strings.tableCellIntroduction(rowIndex + 1, colIndex + 1, text));
    }

    private void simple(Iterator it)
    {
	if (!it.isEmptyRow() || it.getText().trim().isEmpty())
	    environment.say(it.getTextWithHref(strings.linkPrefix() + " ")); else
	    environment.hint(Hints.EMPTY_LINE);
    }

    private void brief(Iterator it)
    {
	if (!it.isEmptyRow())
	{
	    if (it.isContainerListItem() && it.isFirstRow())
		environment.playSound(Sounds.NEW_LIST_ITEM);
	    environment.say(it.getText());
	} else
	    environment.hint(Hints.EMPTY_LINE);
    }
}

/*
   Copyright 2012-2019 Michael Pozhidaev <michael.pozhidaev@gmail.com>
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

package org.luwrain.controls.reader;

import org.luwrain.core.*;
import org.luwrain.controls.*;
import org.luwrain.reader.*;
import org.luwrain.reader.view.Iterator;

public class DefaultAnnouncement implements ReaderArea.Announcement
{
    public interface TextPreprocessor
    {
	String preprocess(String text);
    }
    
    protected final ControlContext context;
    protected final TextPreprocessor textPreprocessor;
    protected final Strings strings;

    public DefaultAnnouncement(ControlContext context, TextPreprocessor textPreprocessor, Strings strings)
    {
	NullCheck.notNull(context, "context");
	NullCheck.notNull(textPreprocessor, "textPreprocessor");
	NullCheck.notNull(strings, "strings");
	this.context = context;
	this.textPreprocessor = textPreprocessor;
	this.strings = strings;
    }

    public DefaultAnnouncement(ControlContext context, Strings strings)
    {
	this(context, (text)->{return context.getSpokenText(text, Luwrain.SpokenTextType.NATURAL);}, strings);
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
	Node node = getDominantNode(it);
	if (node != null)
	{
	    if (node instanceof TableCell)
		onTableCell((TableCell)node); else
		if (node instanceof ListItem)
		    onListItem(it, (ListItem)node); else
		    announceText(it);
	    return;
	}
	announceText(it);
    }

    protected void onTableCell(TableCell cell)
    {
	NullCheck.notNull(cell, "cell");
	final TableRow row = (TableRow)cell.getParentNode();
	final int rowIndex = cell.getRowIndex();
	final int colIndex = cell.getColIndex();
	if (rowIndex == 0 && colIndex == 0)
	{
	    context.say(textPreprocessor.preprocess(row.getCompleteText()) + " Начало таблицы", Sounds.TABLE_CELL);
	    return;
	}
	if (colIndex == 0)
	{
	    context.say(textPreprocessor.preprocess(row.getCompleteText()) + " строка " + (rowIndex + 1) , Sounds.TABLE_CELL);
	    return;
	}
	context.say("столбец " + (colIndex + 1), Sounds.TABLE_CELL);
    }

    protected void onListItem(Iterator it, ListItem listItem)
    {
	NullCheck.notNull(it, "it");
	NullCheck.notNull(listItem, "listItem");
	context.setEventResponse(DefaultEventResponse.text(Sounds.LIST_ITEM, it.getText()));
    }

    protected void announceText(Iterator it)
    {
	NullCheck.notNull(it, "it");
	final String text = it.getText().trim();
	if (text.isEmpty())
	{
	    context.setEventResponse(DefaultEventResponse.hint(Hint.EMPTY_LINE));
	    return;
	}
	if (it.getIndexInParagraph() == 0)
	    context.setEventResponse(DefaultEventResponse.text(Sounds.PARAGRAPH, text)); else
	    context.setEventResponse(DefaultEventResponse.text(textPreprocessor.preprocess(text)));
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
	if (res.getType() == Node.Type.ORDERED_LIST || res.getType() == Node.Type.UNORDERED_LIST)
	{
	    if (res.getSubnodeCount() > 0)
		return res.getSubnode(0);
	    return res;
	}
	return res;
    }

    protected Node findDominantNode(Iterator it)
    {
	NullCheck.notNull(it, "it");
	if (it.getIndexInParagraph() != 0)
	    return null;
	Node node = it.getNode();
	if (node.getIndexInParentSubnodes() != 0)
	    return node;
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

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
	    context.say(textPreprocessor.preprocess(it.getText()), sound);
	    return;
	}
	//Speaking with paragraph sound if it is a first row
	if (it.getIndexInParagraph() == 0)
	    context.say(textPreprocessor.preprocess(it.getText()), Sounds.PARAGRAPH); else
	    context.say(textPreprocessor.preprocess(it.getText()));
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

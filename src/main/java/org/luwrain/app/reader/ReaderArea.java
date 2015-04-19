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

import java.io.File;

import org.luwrain.core.*;
import org.luwrain.core.events.*;
import org.luwrain.controls.*;

import org.luwrain.app.reader.doctree.*;

public class ReaderArea implements Area
{
    private Luwrain luwrain;
    private ControlEnvironment environment;
    private Strings strings;
    private Actions actions;

    //This hot point isn't a real hot point in the area, it is the coordinates in rows;
    private int hotPointX = 0;
    private int hotPointY = 0;

    private Document document;

    public ReaderArea(Luwrain luwrain,
		      Strings strings,
		      Actions actions)
    {
	this.luwrain = luwrain;
	this.strings = strings;
	this.actions = actions;
	if (luwrain == null)
	    throw new NullPointerException("luwrain may not be null");
	if (strings == null)
	    throw new NullPointerException("stringss may not be null");
	if (actions == null)
	    throw new NullPointerException("actions may not be null");
	environment = new DefaultControlEnvironment(luwrain);

	Node root = new Node(Node.ROOT);
	Paragraph para = new Paragraph();
	para.runs = new Run[]{
		new Run("First testing run"),
		    new Run("Second testing run")
	    };
	para.setParentOfRuns();

	Node table1 = new Node(Node.TABLE);
	table1.subnodes = new Node[]{new Node(Node.TABLE_ROW), new Node(Node.TABLE_ROW)};
	table1.subnodes[0].subnodes = new Node[]{new Node(Node.TABLE_CELL), new Node(Node.TABLE_CELL)};
	table1.subnodes[1].subnodes = new Node[]{new Node(Node.TABLE_CELL), new Node(Node.TABLE_CELL)};
	table1.subnodes[0].subnodes[0].subnodes = new Node[]{new Paragraph(new Run("1"))};
	table1.subnodes[0].subnodes[1].subnodes = new Node[]{new Paragraph(new Run("2"))};
	table1.subnodes[1].subnodes[0].subnodes = new Node[]{new Paragraph(new Run("3"))};
	table1.subnodes[1].subnodes[1].subnodes = new Node[]{new Paragraph(new Run("4"))};

	root.subnodes = new Node[]{para, table1};
	root.setParentOfSubnodes();

	document = new Document(root);
	document.buildView(50);
    }

    @Override public int getLineCount()
    {
	return document.getLineCount() + 1;
    }

    @Override public String getLine(int index)
    {
	return index < document.getLineCount()?document.getLine(index):"";
    }

    @Override public boolean onKeyboardEvent(KeyboardEvent event) 
    {
	if (event == null)
	    throw new NullPointerException("event may not be null");
	if (event.isCommand() && !event.isModified())
	    switch(event.getCommand())
	    {
	    case KeyboardEvent.ARROW_DOWN:
		return onArrowDown(event, false);
	    case KeyboardEvent.ARROW_UP:
		return onArrowUp(event, false);
	    case KeyboardEvent.ALTERNATIVE_ARROW_DOWN:
		return onArrowDown(event, true);
	    case KeyboardEvent.ALTERNATIVE_ARROW_UP:
		return onArrowUp(event, true);

	    case KeyboardEvent.ARROW_LEFT:
		return onArrowLeft(event);
	    case KeyboardEvent.ARROW_RIGHT:
		return onArrowRight(event);
	    default:
		return false;
	    }
	return false;
    }

    @Override public boolean onEnvironmentEvent(EnvironmentEvent event)
    {
	switch(event.getCode())
	{
	case EnvironmentEvent.CLOSE:
	    actions.closeApp();
	    return true;
	default:
	    return false;
	}
    }

    @Override public int getHotPointX()
    {
	return 0;
    }

    @Override public int getHotPointY()
    {
	return -0;
    }

    @Override public String getName()
    {
	return strings.appName();
    }

    private boolean onArrowDown(KeyboardEvent event, boolean briefIntroduction)
    {
	if (noContentCheck())
	    return true;
	final int count = document.getRowCount();
	if (hotPointY >= count)
	{
	    environment.hint(Hints.NO_LINES_BELOW);
	    return false;
	}
	++hotPointY;
	onNewHotPointY( briefIntroduction );
	return true;
    }

    private boolean onArrowUp(KeyboardEvent event, boolean briefIntroduction)
    {
	if (noContentCheck())
	    return true;
	final int count = document.getRowCount();
	if (hotPointY <= 0)
	{
	    environment.hint(Hints.NO_LINES_ABOVE);
	    return true;
	}
	--hotPointY;
	onNewHotPointY( briefIntroduction);
	return true;
    }

    private boolean onArrowLeft(KeyboardEvent event)
    {
	if (noContentCheck())
	    return true;
	final int count = document.getRowCount();
	if (hotPointY >= count)
	{
	    if (count == 0)
	    {
		hotPointX = 0;
		hotPointY = 0;
		environment.hint(Hints.BEGIN_OF_TEXT);
		environment.onAreaNewHotPoint(this);
		return true;
	    }
	    hotPointY = count - 1;
	    final String text = document.getRowText(hotPointY);
	    hotPointX = text.length();
	    if (hotPointX == 0)
		environment.hint(Hints.EMPTY_LINE); else
		environment.hint(Hints.END_OF_LINE);
	    environment.onAreaNewHotPoint(this);
	    return true;
	}
	final String text = document.getRowText(hotPointY);
	if (hotPointX > text.length())
	    hotPointX = text.length();
	if (hotPointX > 0)
	{
	    --hotPointX;
	    environment.sayLetter(text.charAt(hotPointX));
	    environment.onAreaNewHotPoint(this);
	    return true;
	}
	if (hotPointY <= 0)
	{
	    environment.hint(Hints.BEGIN_OF_TEXT);
	    return true;
	}
	--hotPointY;
	final String prevRowText = document.getRowText(hotPointY);
	hotPointX = prevRowText.length();
	environment.hint(Hints.END_OF_LINE);
	return true;
    }

    private boolean onArrowRight(KeyboardEvent event)
    {
	if (noContentCheck())
	    return true;
	final int count = document.getRowCount();
	if (hotPointY >= count)
	{
	    environment.hint(Hints.END_OF_TEXT);
	    return true;
	}
	final String text = document.getRowText(hotPointY);
	if (hotPointX > text.length())
	    hotPointX = text.length();
	if (hotPointX < text.length())
	{
	    ++hotPointX;
	    if (hotPointX < text.length())
		environment.sayLetter(text.charAt(hotPointX)); else
		environment.hint(Hints.END_OF_LINE);
	    environment.onAreaNewHotPoint(this);
	    return true;
	}
	if (hotPointY + 1 == count)
	{
	    environment.hint(Hints.END_OF_TEXT);
	    hotPointX = 0;
	    hotPointY = count;
	    environment.onAreaNewHotPoint(this);
	    return true;
	}
	++hotPointY;
	final String nextRowText = document.getRowText(hotPointY);
	hotPointX = 0;
	//	System.out.println("here2");
	if (nextRowText.isEmpty())
	    environment.hint(Hints.END_OF_LINE); else
	    environment.sayLetter(nextRowText.charAt(0));
	//	System.out.println("after here2");
	environment.onAreaNewHotPoint(this);
	return true;
    }

    private void onNewHotPointY(boolean briefIntroduction)
    {
	final int count = document.getRowCount();
	if (hotPointY > count)
	{
	    environment.hint(Hints.EMPTY_LINE);
	    hotPointX = 0;
	    environment.onAreaNewHotPoint(this);
	    return;
	}
	final String text = document.getRowText(hotPointY);
	if (hotPointX > text.length())
	    hotPointX = text.length();
	introduceRow(hotPointY, briefIntroduction);
	environment.onAreaNewHotPoint(this);
    }

    private void introduceRow(int index, boolean briefIntroduction)
    {
	final String text = document.getRowText(index);
	if (briefIntroduction || !document.isValidRowIndex(index))
	{
	    simpleIntroduction(text);
	    return;
	}
	final int indexInParagraph = document.getRowIndexInParagraph(index);
	if (indexInParagraph > 0)
	{
	    simpleIntroduction(text);
	    return;
	}
	final Paragraph para = document.getParagraph(index);
	if (para == null || para.parentNode == null)
	{
	    simpleIntroduction(text);
	    return;
	}
	final int paragraphIndex = para.getIndexInParentSubnodes(); 
	if (paragraphIndex > 0)
	{
	    simpleIntroduction(text);
	    return;
	}
	final int  paraParentType = para.parentNode.type;
	switch (paraParentType)
	{
	case Node.TABLE_CELL:
	    introduceTableCell(para, text);
	    return;
	}
	simpleIntroduction(text);
    }

    private void introduceTableCell(Paragraph para, String text)
    {
	if (para.parentNode == null ||  //Cell;
	    para.parentNode.parentNode == null || //Row;
	    para.parentNode.parentNode.parentNode == null) //Table itself;
	{
	    simpleIntroduction(text);
	    return;
	}
	final int colIndex = para.parentNode.getIndexInParentSubnodes();
	final int rowIndex = para.parentNode.parentNode.getIndexInParentSubnodes();
	final int colCount = para.parentNode.parentNode.subnodes.length;
	final int rowCount = para.parentNode.parentNode.parentNode.subnodes.length;
	if (colIndex == 0 && rowIndex == 0)
	{
	    environment.say(strings.tableIntroduction(rowCount, colCount, text));
	    return;
	}
	environment.say(strings.tableCellIntroduction(rowIndex + 1, colIndex + 1, text));
	//	simpleIntroduction(text);
    }

    private void simpleIntroduction(String text)
    {
	if (!text.isEmpty())
	    environment.say(text); else
	    environment.hint(Hints.EMPTY_LINE);
    }

    private boolean noContentCheck()
    {
	if (document == null)
	{
	    environment.hint("no content");
	    return true;
	}
	return false;
    }
}

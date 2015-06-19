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
import java.util.LinkedList;

import org.luwrain.core.*;
import org.luwrain.core.events.*;
import org.luwrain.controls.*;
import org.luwrain.util.*;

import org.luwrain.app.reader.filters.Filters;
import org.luwrain.app.reader.doctree.*;

public class ReaderArea implements Area, CopyCutRequest
{
    private Luwrain luwrain;
    private ControlEnvironment environment;
    private CopyCutInfo copyCutInfo = new CopyCutInfo(this);
    private Strings strings;
    private Actions actions;

    private Filters filters;
    private Introduction introduction;

    private Document document;
    private Iterator iterator;
    private int hotPointX = 0;

    public ReaderArea(Luwrain luwrain,
		      Strings strings,
		      Actions actions,
		      Filters filters,
Document document)
    {
	this.luwrain = luwrain;
	this.strings = strings;
	this.actions = actions;
	this.filters = filters;
	if (luwrain == null)
	    throw new NullPointerException("luwrain may not be null");
	if (strings == null)
	    throw new NullPointerException("stringss may not be null");
	if (actions == null)
	    throw new NullPointerException("actions may not be null");
	if (filters == null)
	    throw new NullPointerException("filters may not be null");
	environment = new DefaultControlEnvironment(luwrain);
	introduction = new Introduction(environment, strings);
	this.document = document;
	if (document != null)
	    iterator = document.getIterator();
    }

    public void setDocument(Document document)
    {
	if (document == null)
	    throw new NullPointerException("document may not be null");
	this.document = document;
	new DumpInFileSystem(document.getRoot()).dump("/tmp/doc");
	iterator = document.getIterator();
	hotPointX = 0;
	luwrain.onAreaNewContent(this);
    }

    @Override public int getLineCount()
    {
	return document != null?document.getLineCount() + 1:1;
    }

    @Override public String getLine(int index)
    {
	if (document == null)
	    return "";
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
	    case KeyboardEvent.ALTERNATIVE_ARROW_LEFT:
		return onAltLeft(event);
	    case KeyboardEvent.ALTERNATIVE_ARROW_RIGHT:
		return onAltRight(event);
	    case KeyboardEvent.HOME:
		return onHome(event);
	    case KeyboardEvent .END:
		return onEnd(event);
	    case KeyboardEvent.ALTERNATIVE_HOME:
		return onAltHome(event);
	    case KeyboardEvent .ALTERNATIVE_END:
		return onAltEnd(event);
	    case KeyboardEvent.PAGE_UP:
		return onPageUp(event, false);
	    case KeyboardEvent.PAGE_DOWN:
		return onPageDown(event, false);
	    case KeyboardEvent.ALTERNATIVE_PAGE_UP:
		return onPageUp(event, true);
	    case KeyboardEvent.ALTERNATIVE_PAGE_DOWN:
		return onPageDown(event, true);
	    default:
		return false;
	    }
	return false;
    }

    @Override public boolean onEnvironmentEvent(EnvironmentEvent event)
    {
	    if (event == null)
	    throw new NullPointerException("event may not be null");
	switch(event.getCode())
	{
	case EnvironmentEvent.THREAD_SYNC:
	    return onThreadSync(event);
	case EnvironmentEvent.COPY_CUT_POINT:
	    return copyCutInfo.copyCutPoint(getHotPointX(), getHotPointY());
	case EnvironmentEvent.COPY:
	    return copyCutInfo.copy(getHotPointX(), getHotPointY());
	case EnvironmentEvent.CLOSE:
	    actions.closeApp();
	    return true;
	default:
	    return false;
	}
    }

    @Override public boolean onAreaQuery(AreaQuery query)
    {
	return false;
    }

    @Override public int getHotPointX()
    {
	if (document == null || iterator == null)
	    return 0;
	return iterator.getCurrentRow().x + hotPointX;
    }

    @Override public int getHotPointY()
    {
	if (document == null || iterator == null)
	    return 0;
	return iterator.getCurrentRow().y;
    }

    @Override public String getAreaName()
    {
	if (document != null)
	{
	    final String title = document.getTitle();
	    if (title == null || title.trim().isEmpty())
		return strings.appName();
	    return title.trim();
	}
	return strings.appName();
    }

    private boolean onThreadSync(EnvironmentEvent event)
    {
	if (event instanceof FetchEvent)
	{
	    final FetchEvent fetchEvent = (FetchEvent)event;
	    if (fetchEvent.getFetchCode() == FetchEvent.FAILED)
	    {
		luwrain.message(strings.errorFetching(), Luwrain.MESSAGE_ERROR);
		return true;
	    }
	    final Document doc = filters.readText(Filters.HTML, fetchEvent.getText());
	    if (doc != null)
	    {
		setDocument(doc);
		luwrain.playSound(Sounds.GENERAL_OK);
	    }  else
		luwrain.message("problem parsing", Luwrain.MESSAGE_ERROR);//FIXME:
	    return true;
	}
	return false;
    }

    private boolean onArrowDown(KeyboardEvent event, boolean briefIntroduction)
    {
	if (noContentCheck())
	    return true;

	if (iterator.isCurrentParaContainerTableCell())
	{
	    final Node cell = iterator.getCurrentParaContainer();
	    final Table table = iterator.getTableOfCurrentParaContainer();
	    final int col = table.getColIndexOfCell(cell);
	    final int row = table.getRowIndexOfCell(cell);
	    if (table.isSingleLineRow(row) && row + 1 < table.getRowCount())
	    {
		final Node nextRowCell = table.getCell(0, row + 1);
		if (iterator.moveNextUntilContainer(nextRowCell))
		{
	onNewHotPointY( briefIntroduction );
	return true;
		}
	    }
	}

	if (!iterator.moveNext())
	{
	    environment.hint(Hints.NO_LINES_BELOW);
	    return false;
	}
	onNewHotPointY( briefIntroduction );
	return true;
    }

    private boolean onArrowUp(KeyboardEvent event, boolean briefIntroduction)
    {
	if (noContentCheck())
	    return true;
	if (!iterator.movePrev())
	{
	    environment.hint(Hints.NO_LINES_ABOVE);
	    return true;
	}
	onNewHotPointY( briefIntroduction);
	return true;
    }

    private boolean onAltEnd(KeyboardEvent event)
    {
	if (noContentCheck())
	    return true;
	iterator.moveEnd();
	hotPointX = 0;
	onNewHotPointY( false);
	return true;
    }

    private boolean onAltHome(KeyboardEvent event)
    {
	if (noContentCheck())
	    return true;
	iterator.moveHome();
	hotPointX = 0;
	onNewHotPointY( false);
	return true;
    }

    //TODO:
    private boolean onPageDown(KeyboardEvent event, boolean briefIntroduction)
    {
	/*
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
	*/
	return true;
    }

    //TODO:
    private boolean onPageUp(KeyboardEvent event, boolean briefIntroduction)
    {
	/*
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
	*/
	return true;
    }

    private boolean onArrowLeft(KeyboardEvent event)
    {
	if (noContentCheck())
	    return true;
	if (!iterator.isCurrentRowEmpty())
	{
	final String text = iterator.getCurrentText();
	if (hotPointX > text.length())
	    hotPointX = text.length();
	if (hotPointX > 0)
	{
	    --hotPointX;
	    environment.sayLetter(text.charAt(hotPointX));
	    environment.onAreaNewHotPoint(this);
	    return true;
	}
    }
	if (!iterator.canMovePrev())
	{
	    environment.hint(Hints.BEGIN_OF_TEXT);
	    return true;
	}
	iterator.movePrev();
	final String prevRowText = iterator.getCurrentText();
	hotPointX = prevRowText.length();
	environment.hint(Hints.END_OF_LINE);
	return true;
    }

    private boolean onArrowRight(KeyboardEvent event)
    {
	if (noContentCheck())
	    return true;
	if (!iterator.isCurrentRowEmpty())
	{
	final String text = iterator.getCurrentText();
	if (hotPointX < text.length())
	{
	    ++hotPointX;
	    if (hotPointX < text.length())
		environment.sayLetter(text.charAt(hotPointX)); else
		environment.hint(Hints.END_OF_LINE);
	    environment.onAreaNewContent(this);
	    return true;
	}
}
	if (!iterator.canMoveNext())
	{
	    environment.hint(Hints.END_OF_TEXT);
	    return true;
	}
	iterator.moveNext();
	final String nextRowText = iterator.getCurrentText();
	hotPointX = 0;
	if (nextRowText.isEmpty())
	    environment.hint(Hints.END_OF_LINE); else
	    environment.sayLetter(nextRowText.charAt(0));
	environment.onAreaNewHotPoint(this);
	return true;
    }

    private boolean onAltLeft(KeyboardEvent event)
    {
	if (noContentCheck())
	    return true;
	if (iterator.isCurrentRowEmpty())
	{
	    environment.hint(Hints.EMPTY_LINE);
	    return true;
	}
	final String text = iterator.getCurrentText();
	final WordIterator it = new WordIterator(text, hotPointX);
	if (!it.stepBackward())
	{
	    environment.hint(Hints.BEGIN_OF_LINE);
	    return true;
	}
	hotPointX = it.pos();
	environment.say(it.announce());
	environment.onAreaNewHotPoint(this);
	return true;
    }

    private boolean onAltRight(KeyboardEvent event)
    {
	if (noContentCheck())
	    return true;
	if (iterator.isCurrentRowEmpty())
	{
	    environment.hint(Hints.EMPTY_LINE);
	    return true;
	}
	final String text = iterator.getCurrentText();
	final WordIterator it = new WordIterator(text, hotPointX);
	if (!it.stepForward())
	{
	    environment.hint(Hints.END_OF_LINE);
	    return true;
	}
	hotPointX = it.pos();
	if (it.announce().length() > 0)
	    environment.say(it.announce()); else
	    environment.hint(Hints.END_OF_LINE);
	environment.onAreaNewHotPoint(this);
	return true;
    }

    private boolean onHome(KeyboardEvent event)
    {
	if (noContentCheck())
	    return true;
	if (iterator.isCurrentRowEmpty())
	{
	    environment.hint(Hints.EMPTY_LINE);
	    return true;
	}
	final String text = iterator.getCurrentText();
	hotPointX = 0;
	if (!text.isEmpty())
	    environment.sayLetter(text.charAt(0)); else
	    environment.hint(Hints.EMPTY_LINE);
	environment.onAreaNewHotPoint(this);
	return true;
    }

    private boolean onEnd(KeyboardEvent event)
    {
	if (noContentCheck())
	    return true;
	if (iterator.isCurrentRowEmpty())
	{
	    environment.hint(Hints.EMPTY_LINE);
	    return true;
	}

	final String text = iterator.getCurrentText();
	hotPointX = text.length();
	environment.hint(Hints.END_OF_LINE);
	environment.onAreaNewHotPoint(this);
	return true;
    }

    private void onNewHotPointY(boolean briefIntroduction)
    {
	hotPointX = 0;
	if (iterator.isCurrentRowEmpty())
	    environment.hint(Hints.EMPTY_LINE); else
	    introduction.introduce(iterator, briefIntroduction);
	environment.onAreaNewHotPoint(this);
    }

    @Override public boolean onCopyAll()
    {
	if (document == null || iterator == null)
	    return false;
	LinkedList<String> res = new LinkedList<String>();
	final int count = document.getLineCount();
	for(int i = 0;i < count;++i)
	{
	    final String line = document.getLine(i);
	    if (line != null)
		res.add(line); else
		res.add("");
	}
	environment.say(environment.staticStr(Langs.COPIED_LINES) + (res.size()));
	environment.setClipboard(res.toArray(new String[res.size()]));
	return true;
    }

    @Override public boolean onCopy(int fromX, int fromY, int toX, int toY)
    {
	if (document == null || iterator == null)
	    return false;
	if (fromY >= document.getLineCount() || toY > document.getLineCount())
	    return false;
	if (fromY == toY)
	{
	    final String line = document.getLine(fromY);
	    if (line.isEmpty())
		return false;
	    final int fromPos = fromX < line.length()?fromX:line.length();
	    final int toPos = toX < line.length()?toX:line.length();
	    if (fromPos >= toPos)
		throw new IllegalArgumentException("fromPos should be less than toPos");
	    environment.say(line.substring(fromPos, toPos));
	    environment.setClipboard(new String[]{line.substring(fromPos, toPos)});
	    return true;
	}
	LinkedList<String> res = new LinkedList<String>();
	for(int i = fromY;i < toY;++i)
	{
	    final String line = document.getLine(i);
	    if (line != null)
		res.add(line); else
		res.add("");
	}
	if (res.size() == 2)
	    environment.say(environment.staticStr(Langs.COPIED_LINES) + (res.size()));
	environment.setClipboard(res.toArray(new String[res.size()]));
	return true;
    }

    @Override public boolean onCut(int fromX, int fromY, int toX, int toY)
    {
	return false;
    }

    private boolean noContentCheck()
    {
	if (document == null)
	{
	    environment.hint(strings.noContent());
	    return true;
	}
	return false;
    }
}

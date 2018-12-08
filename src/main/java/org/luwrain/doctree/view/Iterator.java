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

package org.luwrain.doctree.view;

import org.luwrain.core.*;
import org.luwrain.doctree.*;

public final class Iterator
{
    public interface Matching
    {
	//paragraph is null on title rows
	boolean isRowMatching(Node node, Paragraph paragraph, Row row);
    }

    protected final View view ;
    protected final Paragraph[] paragraphs;
    protected final Row[] rows;
    protected int current = 0;

    public Iterator(View view)
    {
	NullCheck.notNull(view, "view");
this.view = view;
this.paragraphs = view.getParagraphs();
NullCheck.notNullItems(paragraphs, "paragraphs");
	this.rows = view.getRows();
	NullCheck.notNullItems(rows, "rows");
	current = 0;
    }

    public Iterator(View view, int initialPos)
    {
	NullCheck.notNull(view, "view");
		this.view = view;
	this.paragraphs = view.getParagraphs();
	NullCheck.notNull(paragraphs, "paragraphs");
	this.rows = view.getRows();
	NullCheck.notNull(rows, "rows");
	if (initialPos < 0 || initialPos >= rows.length)
	    throw new IllegalArgumentException("INvalid row initialPos (" + initialPos + "), row count is " + rows.length);
	current = initialPos;
    }

    public boolean noContent()
    {
	return rows.length == 0;
    }

        public int getIndex()
    {
	if (noContent())
	    return -1;
	return current;
    }

    public int getCount()
    {
	return rows.length;
    }

    public View getView()
    {
	return view;
    }


    @Override public boolean equals(Object o)
    {
	if (o == null || !(o instanceof Iterator))
	    return false;
	final Iterator it = (Iterator)o;
	return current == it.current;
    }

    @Override public org.luwrain.doctree.view.Iterator clone()
    {
	return new Iterator(view, current);
    }

    public boolean canMoveNext()
    {
	if (noContent())
	    return false;
	return current + 1 < rows.length;
    }

    public boolean canMovePrev()
    {
	if (noContent())
	    return false;
	return current > 0;
    }

    public boolean moveNext()
    {
	if (!canMoveNext())
	    return false;
	++current;
	return true;
    }

    public boolean movePrev()
    {
	if (!canMovePrev())
	    return false;
	--current;
	return true;
    }

    public void moveEnd()
    {
	current = rows.length > 0?rows.length - 1:0;
    }

    public void moveBeginning()
    {
	current = 0;
    }

    public boolean searchForward(Matching matching)
    {
	NullCheck.notNull(matching, "matching");
	if (noContent())
	    return false;
	return searchForward(matching, 0);
    }

    public boolean searchForward(Matching matching, int searchFrom)
    {
	NullCheck.notNull(matching, "matching");
	if (searchFrom < 0)
	    throw new IllegalArgumentException("searchFrom (" + searchFrom + ") may not be negative");
	if (noContent())
	    return false;
	return search(matching, searchFrom, 1);
	    }

        public boolean searchBackward(Matching matching)
    {
	NullCheck.notNull(matching, "matching");
	if (noContent())
	    return false;
	return searchBackward(matching, rows.length - 1);
    }

    public boolean searchBackward(Matching matching, int searchFrom)
    {
	NullCheck.notNull(matching, "matching");
	if (searchFrom < 0)
	    throw new IllegalArgumentException("searchFrom (" + searchFrom + ") may not be negative");
	if (noContent())
	    return false;
	return search(matching, searchFrom, -1);
	    }


    //do not changes the position of failure
    protected boolean search(Matching matching, int searchFrom, int step)
    {
	NullCheck.notNull(matching, "matching");
	if (searchFrom < 0)
	    throw new IllegalArgumentException("searchFrom (" + searchFrom + ") may not be negative");
	if (noContent())
	    return false;
	for(int i = searchFrom;i >= 0 && i < rows.length;i += step)
	{
	    final Row row = rows[i];
	    final Run firstRun = row.getFirstRun();
	    final Paragraph para;
	    final Node node;
	    if (firstRun instanceof TitleRun)
	    {
		para = null;
		node = firstRun.getParentNode();
	    } else
	    {
		if (!(firstRun.getParentNode() instanceof Paragraph))
		    throw new RuntimeException("Row " + i + " isn\'t a title row, but its parent isn\'t a paragraph");
		para = (Paragraph)firstRun.getParentNode();
		node = para.getParentNode();
	    }
	    if (matching.isRowMatching(node, para, row))
	    {
		current = i;
		return true;
	    }
	}
	return false;
	    }

    public String getText()
    {
	if (noContent())
	    return "";
	final Row row = rows[current];
	//	return !row.isEmpty()?row.getText():"";
	return row.getText();
    }

    //returns -1 if no content

    //returns -1 if no content
    public int getIndexInParagraph()
    {
	if (noContent())
	    return -1;
	//	return current - getParagraph().topRowIndex;
	return getRow().getRelNum();
    }

    public boolean isParagraphBeginning()
    {
	return getIndexInParagraph() == 0;
    }

    public boolean hasRunOnRow(Run run)
    {
	NullCheck.notNull(run, "run");
	final Run[] runs = getRow().getRuns();
	for(Run r: runs)
	    if (run == r)
		return true;
	return false;
    }

    public Run[] getRuns()
    {
	if (noContent())
	    return new Run[0];
	return getRow().getRuns();
    }

    public int runBeginsAt(Run run)
    {
	NullCheck.notNull(run, "run");
	return getRow().runBeginsAt(run);
    }

    public int getX()
    {
	return getRow().x;
    }

    public int getY()
    {
	return getRow().y;
    }

    public boolean isTitleRow()
    {
	final Row row = getRow();
	if (row == null)
	    return false;
	return row.getFirstRun() instanceof TitleRun;
    }

    public Node getNode()
    {
	if (isTitleRow())
	    return getTitleParentNode();
	return getParaContainer();
    }

    public Paragraph getParagraph()
    {
	if (noContent()/* || isEmptyRow()*/)
	    return null;
	final Node parent = getFirstRunOfRow().getParentNode();
	return (parent instanceof Paragraph)?(Paragraph)parent:null;
    }

    //Returns null if is at title row
    protected Node getParaContainer()
    {
	if (noContent() || isTitleRow())
	    return null;
	final Paragraph para = getParagraph();
	return para != null?para.getParentNode():null;
    }

    public boolean coversPos(int x, int y)
    {
	if (noContent())
	    return false;
	final Row r = getRow();
	if (r.getRowY() != y)
	    return false;
	if (x < r.getRowX())
	    return false;
	if (x > r.getRowX() + getText().length())
	    return false;
	return true;
    }

    //pos is relative to the roe beginning
    public Run getRunUnderPos(int pos)
    {
	if (pos < 0)
	    throw new IllegalArgumentException("pos may not be negative");
		if (noContent())
	    throw new RuntimeException("The iterator is without content");
	return rows[current].getRunUnderPos(pos);
    }

public Row getRow()
    {
	if (noContent())
	    throw new RuntimeException("Iterator is without content");
	if (current < 0 || current >= rows.length)
	    return null;
	return rows[current];
    }

    protected Run getFirstRunOfRow()
    {
	if (noContent())
	    return null;
	return rows[current].getFirstRun();
    }

    protected Node getTitleParentNode()
    {
	if (!isTitleRow())
	    return null;
	return getRow().getFirstRun().getParentNode();
    }
}

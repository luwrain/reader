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
import org.luwrain.reader.*;
import org.luwrain.reader.view.*;

public class DefaultTransition implements ReaderArea.Transition
{
    @Override public boolean transition(Type type, Iterator it)
    {
	NullCheck.notNull(type, "type");
	NullCheck.notNull(it, "it");
	switch(type)
	{
	case NEXT:
	    return onNext(it);
	case PREV:
	    return onPrev(it);
	case NEXT_SECTION:
	case NEXT_SECTION_SAME_LEVEL:
	    return onNextSection(it, type == Type.NEXT_SECTION_SAME_LEVEL);
	case PREV_SECTION:
	case PREV_SECTION_SAME_LEVEL:
	    return onPrevSection(it, type == Type.NEXT_SECTION_SAME_LEVEL);
	case NEXT_PARAGRAPH:
	    return onNextParagraph(it);

	    	case PREV_PARAGRAPH:
	    return onPrevParagraph(it);

	    
	default:
	    return false;
	}
    }

    boolean onNext(Iterator it)
    {
	NullCheck.notNull(it, "it");
	final TableCell tableCell = isTableCellIntroRow(it);// always for the table of the maximum depth
	if (tableCell != null && onTableDown(tableCell, it))
	    return true;
	return it.searchForward((node,para,row)->{
		return para != null;
	    }, it.getIndex() + 1);
    }

    boolean onPrev(Iterator it)
    {
	if (it.getIndex() == 0)
	    return false;
	NullCheck.notNull(it, "it");
	final TableCell tableCell = isTableCellIntroRow(it);
	if (tableCell != null && onTableUp(tableCell, it))
	    return true;
	return it.searchBackward((node,para,row)->{
		return para != null;
	    }, it.getIndex() - 1);
    }

    protected boolean onNextSection(Iterator it, boolean sameLevel)
    {
	final Node currentNode = it.getNode();
	if (currentNode == null)//Actually very strange, should never happen
	    return false;
	final int currentSectLevel; 
	if (currentNode instanceof Section)
	{
	    final Section sect = (Section)currentNode;
	    currentSectLevel = sect.getSectionLevel();
	} else
	    currentSectLevel = -1;
	if (!sameLevel || currentSectLevel < 0)
	{
	    if (!it.searchForward((node,para,row)->{
			if (node == currentNode)
			    return false;
			return node.getType() == Node.Type.SECTION;
		    }, it.getIndex()))
		return false;
	} else
	{
	    if (!it.searchForward((node,para,row)->{
			if (node == currentNode)
			    return false;
			if (node.getType() != Node.Type.SECTION)
			    return false;
			final Section sect = (Section)node;
			return sect.getSectionLevel() <= currentSectLevel;
		    }, it.getIndex()))
		return false;
	}
	return true;
    }

    protected boolean onPrevSection(Iterator it, boolean sameLevel)
    {
	NullCheck.notNull(it, "it");
	final Node currentNode = it.getNode();
	if (currentNode == null)//Actually very strange, should never happen
	    return false;
	final int currentSectLevel; 
	if (currentNode instanceof Section)
	{
	    final Section sect = (Section)currentNode;
	    currentSectLevel = sect.getSectionLevel();
	} else
	    currentSectLevel = -1;
	if (!sameLevel || currentSectLevel < 0)
	{
	    if (!it.searchBackward((node,para,row)->{
			if (node == currentNode || row.getRelNum() > 0)
			    return false;
			return node.getType() == Node.Type.SECTION;
		    }, it.getIndex()))
		return false;
	} else
	{
	    if (!it.searchBackward((node,para,row)->{
			if (node == currentNode || row.getRelNum() > 0)
			    return false;
			if (node.getType() != Node.Type.SECTION)
			    return false;
			final Section sect = (Section)node;
			return sect.getSectionLevel() <= currentSectLevel;
		    }, it.getIndex()))
		return false;
	}
	return true;
    }

    protected boolean onNextParagraph(Iterator it)
    {
	NullCheck.notNull(it, "it");
	return it.searchForward((node,para,row)->{
		return row.getRelNum() == 0;
	    }, it.getIndex() + 1);
    }

        protected boolean onPrevParagraph(Iterator it)
    {
	NullCheck.notNull(it, "it");
	if (it.getIndex() == 0)
	    return false;
	return it.searchBackward((node,para,row)->{
		return row.getRelNum() == 0;
	    }, it.getIndex() - 1);
    }


    boolean onTableDown(TableCell tableCell, Iterator it)
    {
	NullCheck.notNull(tableCell, "tableCell");
	NullCheck.notNull(it, "it");
	final Table table = tableCell.getTable();
	final int rowIndex = tableCell.getRowIndex();
	final int colIndex = tableCell.getColIndex();
	if (rowIndex + 1 >= table.getRowCount())
	    //	    throw new RuntimeException("proba");
	    return it.searchForward((node,para,row)->{
		    return !node.isInTable(table) && !node.noText();
		}, it.getIndex() + 1);
	final TableCell newCell = table.getCell(colIndex, rowIndex + 1);
	if (newCell == null)
	    return false;
	return findTableCellForward(newCell, it);
    }

    boolean onTableUp(TableCell tableCell, Iterator it)
    {
	NullCheck.notNull(tableCell, "tableCell");
	NullCheck.notNull(it, "it");
	final Table table = tableCell.getTable();
	final int rowIndex = tableCell.getRowIndex();
	final int colIndex = tableCell.getColIndex();
	if (rowIndex == 0)
	    return false;
	final TableCell newCell = table.getCell(colIndex, rowIndex - 1);
	if (newCell == null)
	    return false;
	return findTableCellBackward(newCell, it);
    }

    protected boolean findTableCellForward(TableCell tableCell, Iterator it)
    {
	NullCheck.notNull(tableCell, "tableCell");
	NullCheck.notNull(it, "it");
	return it.searchForward((node,para,row)->{
		if (para == null)//title row
		    return false;
		return isIntroRowFor(row, para, tableCell);
	    });
    }

    protected boolean findTableCellBackward(TableCell tableCell, Iterator it)
    {
	NullCheck.notNull(tableCell, "tableCell");
	NullCheck.notNull(it, "it");
	return it.searchBackward((node,para,row)->{
		if (para == null)//title row
		    return false;
		return isIntroRowFor(row, para, tableCell);
	    });
    }

    //Returns the closest one, but there can be more
    protected TableCell isTableCellIntroRow(Iterator it)
    {
	NullCheck.notNull(it, "it");
	if (it.getIndexInParagraph() != 0)
	    return null;
	Node node = it.getParagraph();
	while(node != null)
	{
	    if (node instanceof TableCell)
		return (TableCell)node;
	    if (node.getIndexInParentSubnodes() != 0)
		return null;
	    node = node.getParentNode();
	}
	return null;
    }

    protected boolean isIntroRowFor(Row row, Paragraph paragraph, Node nodeToCheck)
    {
	NullCheck.notNull(row, "row");
	NullCheck.notNull(paragraph, "paragraph");
	NullCheck.notNull(nodeToCheck, "nodeToCheck");
	if (row.getRelNum() != 0)
	    return false;
	Node node = paragraph;
	while(node != null)
	{
	    if (node == nodeToCheck)
		return true;
	    if (node.getIndexInParentSubnodes() != 0)
		return false;
	    node = node.getParentNode();
	}
	return false;
    }
}

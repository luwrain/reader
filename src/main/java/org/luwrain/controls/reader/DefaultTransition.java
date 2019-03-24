

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
	default:
	return false;
	}
    }
    
    boolean onNext(Iterator it)
    {
     NullCheck.notNull(it, "it");
     final TableCell tableCell = isTableCellIntroRow(it);
     if (tableCell != null && onTableDown(tableCell, it))
	 return true;
     return it.moveNext();
    }

    boolean onTableDown(TableCell tableCell, Iterator it)
    {
	     NullCheck.notNull(tableCell, "tableCell");
     NullCheck.notNull(it, "it");
     final Table table = tableCell.getTable();
     final int rowIndex = tableCell.getRowIndex();
     final int colIndex = tableCell.getColIndex();
     if (rowIndex + 1 >= table.getRowCount())
	 return false;
     final TableCell newCell = table.getCell(colIndex, rowIndex + 1);
     if (newCell == null)
	 return false;
     return findTableCellForward(newCell, it);
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


    //Returns the closest one, but there can be more
    protected TableCell isTableCellIntroRow(Iterator it)
    {
	NullCheck.notNull(it, "it");
	if (it.isTitleRow())
	    return null;
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

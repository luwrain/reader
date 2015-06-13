
package org.luwrain.app.reader.doctree;

public class Iterator
{
    private Document document;
    private Node root;
    private Paragraph[] paragraphs;
    private RowPart[] rowParts;
    private Row[] rows;

    private int current = 0;

    public Iterator(Document document)
    {
	if (document == null)
	    throw new NullPointerException("document may not be null");
	this.document = document;
	this.root = document.getRoot();
	this.paragraphs = document.getParagraphs();
	this.rowParts = document.getRowParts();
	this.rows = document.getRows();
    }

    public Row getCurrentRow()
    {
	return rows[current];
    }

    public int getCurrentRowAbsIndex()
    {
	return current;
    }

    public int getCurrentRowRelIndex()
    {
	return current - getCurrentParagraph().topRowIndex;
    }

    public boolean isCurrentRowEmpty()
    {
	return !rows[current].hasAssociatedText();
    }

    public Paragraph getCurrentParagraph()
    {
	return rowParts[rows[current].partsFrom].run.parentParagraph;
    }

    public int getCurrentParagraphIndex()
    {
	return getCurrentParagraph().getIndexInParentSubnodes();
    }

    public String getCurrentText()
    {
	//FIXME:
	return "";
    }

    public Node getCurrentParaContainer()
    {
	return getCurrentParagraph().parentNode;
    }

    public boolean isCurrentParaContainerTableCell()
    {
	final Node container = getCurrentParaContainer();
	return container.type == Node.TABLE_CELL &&
	container.parentNode != null && container.parentNode.type == Node.TABLE_ROW &&
	container.parentNode.parentNode != null && container.parentNode.parentNode.type == Node.TABLE &&
	container.parentNode.parentNode instanceof Table;
    }

    public Table getTableOfCurrentParaContainer()
    {
	final Node container = getCurrentParaContainer();
	if (container == null || container.type != Node.TABLE_CELL)
	    return null;
	if (container.parentNode == null || container.parentNode.parentNode == null)
	    return null;
	final Node tableNode = container.parentNode.parentNode;
	if (tableNode instanceof Table)
	    return (Table)tableNode;
	return null;
    }

    public boolean isCurrentParaContainerListItem()
    {
	final Node container = getCurrentParaContainer();
	return container.type == Node.LIST_ITEM &&
	container.parentNode != null &&
	(container.parentNode.type == Node.ORDERED_LIST || container.parentNode.type == Node.UNORDERED_LIST);
    }

    public int getListItemIndexOfCurrentParaContainer()
    {
	return getCurrentParaContainer().getIndexInParentSubnodes();
    }

    public boolean isListOfCurrentParaContainerOrdered()
    {
	return getCurrentParaContainer().parentNode.type == Node.ORDERED_LIST;
    }

    public boolean moveNext()
    {
	//FIXME:
	return false;
    }

    public boolean movePrev()
    {
	//FIXME:
	return false;
    }

    public void moveEnd()
    {
    }

    public void moveHome()
    {
    }

    public boolean isEmpty()
    {
	//FIXME:
	return true;
    }

    public boolean canMoveNext()
    {
	//FIXME :
	return false;
    }

    public boolean canMovePrev()
    {
	//FIXME:
	return false;
    }







}

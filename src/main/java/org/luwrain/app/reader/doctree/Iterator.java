
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
	current = 0;
    }

    public Iterator(Document document, int index)
    {
	if (document == null)
	    throw new NullPointerException("document may not be null");
	this.document = document;
	this.root = document.getRoot();
	this.paragraphs = document.getParagraphs();
	this.rowParts = document.getRowParts();
	this.rows = document.getRows();
	current = index;
    }

    @Override public Object clone()
    {
	return new Iterator(document, current);
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
	final Row row = rows[current];
	if (row.partsFrom < 0 || row.partsTo < 0)
	    return "";
	return row.text(rowParts);
    }

    public Node getCurrentParaContainer()
    {
	return getCurrentParagraph().parentNode;
    }

    public boolean hasContainerInParents(Node container)
    {
	Node n = getCurrentParagraph();
	while (n != null && n != container)
	    n = n.parentNode;
	return n == container;
    }

    public boolean isCurrentParaContainerTableCell()
    {
	if (isCurrentRowEmpty())
	    return false;
	final Node container = getCurrentParaContainer();
	return container.type == Node.TABLE_CELL &&
	container.parentNode != null && container.parentNode.type == Node.TABLE_ROW &&
	container.parentNode.parentNode != null && container.parentNode.parentNode.type == Node.TABLE &&
	container.parentNode.parentNode instanceof Table;
    }

    public Table getTableOfCurrentParaContainer()
    {
	if (isCurrentRowEmpty())
	    return null;

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
	if (isCurrentRowEmpty())
	    return false;
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
	if (rowParts.length == 0 || current + 1 >= rowParts.length)
	    return false;
	++current;
	return true;
    }

    public boolean moveNextUntilContainer(Node container)
    {
	final Iterator it = (Iterator)clone();
	while (!it.hasContainerInParents(container))
	    if (!it.moveNext())
		break;
	if (!it.hasContainerInParents(container))
	    return false;
	current = it.getCurrentRowAbsIndex();
	return true;
    }

    public boolean movePrev()
    {
	if (current == 0)
	    return false;
	--current;
	return true;
    }

    public void moveEnd()
    {
	current = rowParts.length > 0?rowParts.length - 1:0;
    }

    public void moveHome()
    {
	current = 0;
    }

    /*
    public boolean isEmpty()
    {
	//FIXME:
	return true;
    }
    */

    public boolean canMoveNext()
    {
	return current + 1 < rowParts.length;
    }

    public boolean canMovePrev()
    {
	return current > 0;
    }
}

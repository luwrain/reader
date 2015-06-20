

package org.luwrain.app.reader.doctree;

class Layout
{
    private Document document;

    private Node root;
    private Paragraph[] paragraphs; //Only paragraphs which appear in document, no paragraphs without row parts
    public RowPart[] rowParts;
    private Row[] rows;

    private Line[] lines = new Line[0];

    public Layout(Document document)
    {
	this.document = document;
	if (document == null)
	    throw new NullPointerException("document may not be null");
    }

    public void init()
    {
	root = document.getRoot();
	paragraphs = document.getParagraphs();
	rowParts = document.getRowParts();
	rows = document.getRows();
    }

    public void calc()
    {
	final int lineCount = calcRowsPosition();
	lines = new Line[lineCount];
	for(int i = 0;i < lines.length;++i)
	    lines[i] = new Line();
	for(int k = 0;k < rows.length;++k)
	{
	    if (rows[k].partsFrom < 0 || rows[k].partsTo < 0)
		continue;
	    final Line line = lines[rows[k].y];
	    //	    System.out.println("y=" + rows[k].y);
	    final int[] oldRows = line.rows;
	    line.rows = new int[oldRows.length + 1];
	    for(int i = 0;i < oldRows.length;++i)
		line.rows[i] = oldRows[i];
	    line.rows[oldRows.length] = k;
	}
    }

    private int calcRowsPosition()
    {
	int maxLineNum = 0;
	int lastX = 0;
	int lastY = 0;
	for(Row r: rows)
	{
	    //Generally admissible situation as not all rows should have associated parts;
	    if (r.partsFrom < 0 || r.partsTo < 0 || r.partsFrom >= r.partsTo)
	    {
		r.x = lastX;
		r.y = lastY + 1;
		continue;
	    }
	    final Run run = rowParts[r.partsFrom].run;
	    final Paragraph paragraph = run.parentParagraph;
	    r.x = paragraph.x;
	    r.y = paragraph.y + rowParts[r.partsFrom].relRowNum;
	    lastX = r.x;
	    lastY = r.y;
	    if (r.y > maxLineNum)
		maxLineNum = r.y;
	}
	return maxLineNum + 1;
    }

    public int getLineCount()
    {
	return lines != null?lines.length:0;
    }

    public String getLine(int index)
    {
	final Line line = lines[index];
	StringBuilder b = new StringBuilder();
	for(int r: line.rows)
	{
	    final Row row = rows[r];
	    while(b.length() < row.x)
		b.append(" ");
	    b.append(row.text(rowParts));
	}
	return b.toString();
    }
}

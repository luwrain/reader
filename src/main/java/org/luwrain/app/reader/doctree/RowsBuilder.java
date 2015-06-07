
package org.luwrain.app.reader.doctree;

class RowsBuilder
{
    static public Row[] buildRows(RowPart[] parts)
    {
	Row[] rows = new Row[parts[parts.length - 1].absRowNum + 1];
	for(int i = 0;i < rows.length;++i)
	    rows[i] = new Row();
	int current = -1;
	for(int i = 0;i < parts.length;++i)
	{
	    if (parts[i] == null)
		throw new NullPointerException("parts[" + i + "] may not be null");
	    final int rowNum = parts[i].absRowNum;
	    //We are registering only a first part
	    if (rows[rowNum].partsFrom < 0)
		rows[rowNum].partsFrom = i;
	    if (rows[rowNum].partsTo < i + 1)
		rows[rowNum].partsTo = i + 1;
	    }
	return rows;
    }
}

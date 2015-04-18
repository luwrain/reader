
package org.luwrain.app.reader.doctree;

class RowsBuilder
{
    static public Row[] buildRows(RowPart[] parts)
    {
	Row[] rows = new Row[parts[parts.length - 1].rowNum + 1];
	int current = -1;
	for(int i = 0;i < parts.length;++i)
	{
	    final int rowNum = parts[i].rowNum;
	    if (rows[rowNum].partsFrom < 0)
		rows[rowNum].partsFrom = i;
	    if (rows[rowNum].partsTo < i + 1)
		rows[rowNum].partsTo = i + 1;
	    }
	return rows;
    }
}

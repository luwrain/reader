
package org.luwrain.app.reader.doctree;

public class Row
{
    /** Absolute horizontal position in the area*/
    public int x = 0;

    /** Absolute vertical position in the area*/
    public int y = 0;

    public int partsFrom = -1;
    public int partsTo = -1;

    public String text(RowPart[] parts)
    {
	StringBuilder b = new StringBuilder();
	for(int i = partsFrom;i < partsTo;++i)
	    b.append(parts[i].text());
	return b.toString();
    }
    {
    }

    public boolean hasAssociatedText()
    {
	return partsFrom >= 0 && partsTo >= 0;
    }
}


package org.luwrain.app.reader.filters;

import org.luwrain.app.reader.doctree.Document;

public class Filters
{
    public static final int DOC = 0;

    public static Document read(String fileName, int format)
    {
	if (fileName == null)
	    throw new NullPointerException("fileName may not be null");
	if (fileName.isEmpty())
	    throw new IllegalArgumentException("fileName may not be empty");
	Filter filter = null;
	switch (format)
	{
	case DOC:
	    filter = new Doc(fileName);
	    break;
	default:
	    throw new IllegalArgumentException("unknown format " + format);
	}
	return filter.constructDocument();
    }
}

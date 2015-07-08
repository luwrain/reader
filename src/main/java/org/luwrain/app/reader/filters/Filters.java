
package org.luwrain.app.reader.filters;

import org.luwrain.core.FileTypes;
import org.luwrain.app.reader.doctree.Document;

public class Filters
{
    public static final int UNRECOGNIZED = 0;
    public static final int DOC = 1;
    public static final int HTML = 2;
    public static final int DOCX = 3;

    public Document readFromFile(String fileName)
    {
	final int type = recognizeType(fileName);
	if (type == UNRECOGNIZED)
	    return null;
	return readFromFile(fileName, type);
    }

    private Document readFromFile(String fileName, int format)
    {
	Filter filter = null;
	switch (format)
	{
	case DOC:
	    filter = new Doc(fileName);
	    break;
	case DOCX:
	    filter = new Docx(fileName);
	    break;
	case HTML:
	    filter = new Html(true, fileName);
	    break;
	default:
	    throw new IllegalArgumentException("unknown format " + format);
	}
	final Document res = filter.constructDocument();
	res.buildView(100);//FIXME:
	return res;
    }

    public Document readText(int format, String text)
    {
	Filter filter = null;
	switch (format)
	{
	case HTML:
	    filter = new Html(false, text);
	    break;
	default:
	    throw new IllegalArgumentException("unknown format " + format);
	}
	final Document res = filter.constructDocument();
	res.buildView(100);//FIXME:
	return res;
    }

    static private int recognizeType(String path)
    {
	if (path == null)
	    throw new NullPointerException("path may not be null");
	if (path.isEmpty())
	    throw new IllegalArgumentException("path may not be empty");
	final String ext = FileTypes.getExtension(path);
	if (ext == null || path.isEmpty())
	    return UNRECOGNIZED;
	if (ext.toLowerCase().equals("doc"))
	    return DOC;
	if (ext.toLowerCase().equals("html") || ext.toLowerCase().equals("htm"))
	    return HTML;
	return UNRECOGNIZED;
    }
}

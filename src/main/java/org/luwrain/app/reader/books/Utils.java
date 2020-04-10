
package org.luwrain.app.reader.books;

import java.util.*;
import java.io.*;
import javax.activation.*;

import org.luwrain.core.*;

public final class Utils
{
    static public String getDoctypeName(InputStream s) throws IOException
    {
	final org.jsoup.nodes.Document doc = org.jsoup.Jsoup.parse(s, "us-ascii", "", org.jsoup.parser.Parser.xmlParser());
	List<org.jsoup.nodes.Node>nods = doc.childNodes();
	for (org.jsoup.nodes.Node node : nods)
	    if (node instanceof org.jsoup.nodes.DocumentType)
	    {
		org.jsoup.nodes.DocumentType documentType = (org.jsoup.nodes.DocumentType)node;                  
		final String res = documentType.attr("name");
		if (res != null)
		    return res;
	    }
	for (org.jsoup.nodes.Node node : nods)
	    if (node instanceof org.jsoup.nodes.Element)
	    {
		org.jsoup.nodes.Element el = (org.jsoup.nodes.Element)node;                  
		final String res = el.tagName();
		if (res != null)
		    return res;
	    }
	return "";
    }

    static public String extractBaseContentType(String value)
    {
	NullCheck.notEmpty(value, "value");
	try {
	    final MimeType mime = new MimeType(value);
	    final String res = mime.getBaseType();
	    return res != null?res:"";
	}
	catch(MimeTypeParseException e)
	{
	    return "";
	}
    }

    static public String extractCharset(String value)
    {
	NullCheck.notEmpty(value, "value");
	try {
	    final MimeType mime = new MimeType(value);
	    final String res = mime.getParameter("charset");
	    return res != null?res:"";
	}
	catch(MimeTypeParseException e)
	{
	    return "";
	}
    }

        static public String extractHtmlCharset(File file) throws IOException
    {
	NullCheck.notNull(file, "file");
	final String res = Encoding.getHtmlEncoding(file.toPath());
	if (res == null)
	    return "";
	return res;
    }

    /*
    static private ParaStyle translateParaStyle(String str)
    {
	NullCheck.notNull(str, "str");
	switch(str)
	{
	case "EMPTY_LINES":
	    return ParaStyle.EMPTY_LINES;
	case "INDENT":
	    return ParaStyle.INDENT;
	case "EACH_LINE":
	    return ParaStyle.EACH_LINE;
	default:
	    return null;
	}
    }

    */

}

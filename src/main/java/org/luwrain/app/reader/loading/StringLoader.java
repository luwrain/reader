
package org.luwrain.app.reader.loading;

import java.net.*;
import java.io.*;
import java.nio.file.*;
import java.nio.charset.*;
import java.util.*;
import java.util.zip.*;
import javax.activation.*;

import org.apache.poi.util.IOUtils;

import org.luwrain.core.*;
import org.luwrain.doctree.*;
import org.luwrain.app.reader.formats.*;
import org.luwrain.app.reader.books.*;
import org.luwrain.app.reader.*;

class StringLoader
{
    static public final String CONTENT_TYPE_HTML = UrlLoader.CONTENT_TYPE_HTML;

    static private final String DEFAULT_CHARSET = "UTF-8";

    private final String text;
    private final String contentType;
    private final URL url;
    private String charset = "";

    public StringLoader(String text, String contentType, URL url)
    {
	NullCheck.notNull(text, "text");
	NullCheck.notNull(contentType, "contentType");
	NullCheck.notNull(url, "url");
	this.text = text;
	this.contentType = contentType;
	this.url = url;
    }

    public Result load()
    {
	    UrlLoader.Format format = UrlLoader.chooseFilterByContentType(UrlLoader.extractBaseContentType(contentType));
	    if (format == null)
	    {
		Log.error("doctree", "unable to choose suitable filter depending on content type:" + contentType);
		final Result res = new Result(Result.Type.UNRECOGNIZED_FORMAT);
res.setProperty("contenttype", contentType);
res.setProperty("url", url.toString());
return res;
	    }
	    selectCharset(format);
	    Log.debug("doctree", "selected charset is " + charset);
	    final Result res = parse(format);
	    if (res.doc == null)
	    {
		final Result r = new Result(Result.Type.UNRECOGNIZED_FORMAT);
r.setProperty("contenttype", contentType);
r.setProperty("url", url.toString());
return r;
	    }
res.doc.setProperty("url", url.toString());
res.doc.setProperty("format", format.toString());
res.doc.setProperty("contenttype", contentType);
res.doc.setProperty("charset", charset);
res.setProperty("url", url.toString());
res.setProperty("format", format.toString());
res.setProperty("contenttype", contentType);
res.setProperty("charset", charset);
	    return res;
    }

    private void selectCharset(UrlLoader.Format format)
    {
	NullCheck.notNull(format, "format");
	NullCheck.notEmpty(contentType, "contentType");
	charset = "";//UrlLoader.extractCharset(contentType);
	if (charset == null || charset.isEmpty())
charset = DEFAULT_CHARSET;
    }

    private Result parse(UrlLoader.Format format)
    {
	NullCheck.notNull(format, "format");
	Log.debug("doctree", "parsing the document as " + format.toString());
	try {
	    final Result res = new Result(Result.Type.OK);
	    switch(format)
	    {
	    case HTML:
		res.doc = new Html(text, url).constructDocument();
		return res;
	    default:
		return new Result(Result.Type.UNRECOGNIZED_FORMAT);
	    }
	}
	finally {
	}
    }

    static public class Result
    {
	public enum Type {
	    OK,
	    UNKNOWN_HOST,  //See "host" property
	    HTTP_ERROR, //See "httpcode" property
	    FETCHING_ERROR, //See "descr" property
	    UNDETERMINED_CONTENT_TYPE,
	    UNRECOGNIZED_FORMAT, //See "contenttype" property
	};

	public Type type = Type.OK;
		public Book book = null;
	public Document doc = null;
	//	int startingRowIndex;
	private final Properties props = new Properties();

	public Result()
	{
	    type = Type.OK;
	}

	Result(Type type)
	{
	    NullCheck.notNull(type, "type");
	    this.type = type;
	}

	public String getProperty(String propName)
	{
	    NullCheck.notNull(propName, "propName");
	    final String res = props.getProperty(propName);
	    return res != null?res:"";
	}

	void setProperty(String propName, String value)
	{
	    NullCheck.notEmpty(propName, "propName");
	    NullCheck.notNull(value, "value");
	    props.setProperty(propName, value);
	}
    }
}

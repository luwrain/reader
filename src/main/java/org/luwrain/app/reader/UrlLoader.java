
package org.luwrain.app.reader;

import java.net.*;
import java.io.*;
import java.nio.file.*;
import java.nio.charset.*;
import java.util.*;
import java.util.zip.*;

import org.luwrain.core.*;
import org.luwrain.util.*;
import org.luwrain.doctree.*;
import org.luwrain.app.reader.formats.*;
import org.luwrain.app.reader.books.*;

public final class UrlLoader
{
    static private final String LOG_COMPONENT = "reader";

    static public final String CONTENT_TYPE_DATA = "application/octet-stream";
    static public final String CONTENT_TYPE_PDF = "application/pdf";
    static public final String CONTENT_TYPE_POSTSCRIPT = "application/postscript";
    static public final String CONTENT_TYPE_XHTML = "application/xhtml";
    static public final String CONTENT_TYPE_ZIP = "application/zip";
    static public final String CONTENT_TYPE_DOC = "application/msword";
    static public final String CONTENT_TYPE_DOCX = "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
    static public final String CONTENT_TYPE_HTML = "text/html";
    static public final String CONTENT_TYPE_TXT = "text/plain";
    static public final String CONTENT_TYPE_XML = "application/xml";
    static public final String CONTENT_TYPE_FB2 = "application/fb2";
    static public final String CONTENT_TYPE_FB2_ZIP = "application/fb2+zip";

    static private final String DOCTYPE_FB2 = "fictionbook";

    static private final String DEFAULT_CHARSET = "UTF-8";

    enum Format {
	TXT, HTML, XML, DOC, DOCX,
	FB2, FB2_ZIP, EPUB, SMIL,
    };

    private final Luwrain luwrain;
    private final URL requestedUrl;
    private String requestedContentType = "";
    private String requestedTagRef = "";
    private String requestedCharset = "";
    private TextFiles.ParaStyle requestedTxtParaStyle = TextFiles.ParaStyle.EMPTY_LINES;

    private URL responseUrl = null;
    private String responseContentType = "";
    private String responseContentEncoding = "";

    private String selectedContentType = "";
    private String selectedCharset = "";

        private Path tmpFile;

    public UrlLoader(Luwrain luwrain, URL url) throws MalformedURLException
    {
	NullCheck.notNull(luwrain, "luwrain");
	NullCheck.notNull(url, "url");
	this.luwrain = luwrain;
	this.requestedTagRef = url.getRef();
	this.requestedUrl = new URL(url.getProtocol(), IDN.toASCII(url.getHost()),
			       url.getPort(), url.getFile());
    }

    public void setContentType(String contentType)
    {
	NullCheck.notEmpty(contentType, "contentType");
	this.requestedContentType = contentType;
    }

    public void setCharset(String charset)
    {
	NullCheck.notEmpty(charset, "charset");
	this.requestedCharset = charset;
    }

    void setTxtParaStyle(TextFiles.ParaStyle paraStyle)
    {
	NullCheck.notNull(paraStyle, "paraStyle");
	this.requestedTxtParaStyle = paraStyle;
    }

    public Result load() throws IOException
    {
	try {
	    try {
		Log.debug(LOG_COMPONENT, "fetching " + requestedUrl.toString());
				fetch();
	    }
	    catch(Connections.InvalidHttpResponseCodeException e)
	    {
		Log.error(LOG_COMPONENT, e.getClass().getName() + ":" + e.getMessage());
				final Result res = new Result(Result.Type.HTTP_ERROR);
		res.setProperty("url", requestedUrl.toString());
		res.setProperty("httpcode", "" + e.getHttpCode());
		return res;
	    }
	    catch (UnknownHostException  e)
	    {
				Log.error(LOG_COMPONENT, e.getClass().getName() + ":" + e.getMessage());
		final Result res = new Result(Result.Type.UNKNOWN_HOST);
		res.setProperty("url", requestedUrl.toString());
		res.setProperty("host", e.getMessage());
		return res;
	    }
	    catch (IOException e)
	    {
				Log.error(LOG_COMPONENT, e.getClass().getName() + ":" + e.getMessage());
		final Result res = new Result(Result.Type.FETCHING_ERROR);
		res.setProperty("url", requestedUrl.toString());
		res.setProperty("descr", e.getClass().getName() + ":" + e.getMessage());
		return res;
	    }
	    this.selectedContentType = requestedContentType.isEmpty()?responseContentType:requestedContentType;

	    	    if (selectedContentType.isEmpty())
			this.selectedContentType = luwrain.suggestContentType(requestedUrl, ContentTypes.ExpectedType.TEXT);
	    if (selectedContentType.isEmpty())
		return new Result(Result.Type.UNDETERMINED_CONTENT_TYPE);
	    final Format format = chooseFilterByContentType(Utils.extractBaseContentType(selectedContentType));
	    if (format == null)
	    {
		Log.error(LOG_COMPONENT, "unable to choose the suitable filter for " + requestedUrl.toString());
		final Result res = new Result(Result.Type.UNRECOGNIZED_FORMAT);
res.setProperty("contenttype", selectedContentType);
res.setProperty("url", responseUrl.toString());
return res;
	    }
	    selectCharset(format);
	    final Result res = parse(format);
	    if (res.doc == null)
	    {
		final Result r = new Result(Result.Type.UNRECOGNIZED_FORMAT);
r.setProperty("contenttype", selectedContentType);
r.setProperty("url", responseUrl.toString());
return r;
	    }
res.doc.setProperty("url", responseUrl.toString());
res.doc.setProperty("format", format.toString());
res.doc.setProperty("contenttype", selectedContentType);
res.doc.setProperty("charset", selectedCharset);
if (requestedTagRef != null)
    res.doc.setProperty("startingref", requestedTagRef);
/*
		if (responseUrl.getFile().toLowerCase().endsWith("/ncc.html"))
		{
		    Log.debug("doctree", "daisy book detected");
		    res.book = BookFactory.initDaisy2(res.doc, this);
			res.doc = null;
		}
*/
res.setProperty("url", responseUrl.toString());
res.setProperty("format", format.toString());
res.setProperty("contenttype", selectedContentType);
res.setProperty("charset", selectedCharset);
	    return res;
	}
	finally {
	    if (tmpFile != null)
	    {
		Log.debug("doctree", "deleting temporary file " + tmpFile.toString());
		Files.delete(tmpFile);
		tmpFile = null;
	    }
	}
    }

    private void fetch() throws IOException
    {
	final URLConnection con = Connections.connect(requestedUrl, 0);
	final InputStream responseStream = con.getInputStream();
	try {
	    this.responseUrl = con.getURL();
	    if (responseUrl == null)
		this.responseUrl = requestedUrl;
	    this.responseContentType = con.getContentType();
	    if (responseContentType == null)
		responseContentType = "";
	    this.responseContentEncoding = con.getContentEncoding();
	    if (responseContentEncoding == null)
		responseContentEncoding = "";
	    if (responseContentEncoding.toLowerCase().trim().equals("gzip"))
		downloadToTmpFile(new GZIPInputStream(responseStream)); else
		downloadToTmpFile(responseStream);
	}
	finally {
	    responseStream.close();
	}
    }

    private void downloadToTmpFile(InputStream s) throws IOException
    {
	NullCheck.notNull(s, "s");
	tmpFile = Files.createTempFile("lwr-doctree", "");
	Log.debug("doctree", "creating temporary file " + tmpFile.toString());
	Files.copy(s, tmpFile, StandardCopyOption.REPLACE_EXISTING);
    }

    private void selectCharset(Format format) throws IOException
    {
	NullCheck.notNull(format, "format");
	NullCheck.notEmpty(selectedContentType, "selectedContentType");
	if (!requestedCharset.isEmpty())
	{
	    this.selectedCharset = requestedCharset;
	    return;
	}
	this.selectedCharset = Utils.extractCharset(selectedContentType);
	if (!selectedCharset.isEmpty())
	    return;
	switch(format)
	{
	case XML:
	    this.selectedCharset = Encoding.getXmlEncoding(tmpFile);
	    break;
	case HTML:
	    this.selectedCharset = extractCharset(tmpFile);
	    break;
	}
	if (selectedCharset == null || selectedCharset.isEmpty())
	    selectedCharset = DEFAULT_CHARSET;
    }

    private Result parse(Format format) throws IOException
    {
	NullCheck.notNull(format, "format");
	final Result res = new Result(Result.Type.OK);
	final InputStream 	    stream = Files.newInputStream(tmpFile);
	try {
	    switch(format)
	    {
	    case HTML:
		res.doc = new Html(stream, selectedCharset, responseUrl).constructDocument();
		return res;
	    case XML:
		res.doc = readXml();
		return res;
	    case DOCX:
		res.doc = DocX.read(tmpFile);
		return res;
	    case DOC:
		res.doc = Doc.read(tmpFile);
		return res;
	    case FB2:
res.doc = new Fb2(tmpFile, selectedCharset).createDoc();
return res;
	    case FB2_ZIP:
		res.doc = new org.luwrain.app.reader.formats.Zip(tmpFile, (is)->{
			try {
return new Fb2(is, selectedCharset).createDoc();
			}
			catch (IOException e)
			{
			    Log.error("doctree", "unable to read FB2 subdoc in ZIP:" + e.getClass().getName() + ":" + e.getMessage());
			    return null;
			}
}).createDoc();
		return res;
	    case TXT:
		res.doc = new TextFiles(tmpFile.toFile(), selectedCharset, requestedTxtParaStyle).makeDoc();
		    return res;
	    default:
		return new Result(Result.Type.UNRECOGNIZED_FORMAT);
	    }
	}
	finally {
		stream.close();
	}
    }

    private Document readXml() throws IOException
    {
	final String doctype = Utils.getDoctypeName(Files.newInputStream(tmpFile));
	if (doctype == null || doctype.trim().isEmpty())
	{
	    Log.debug(LOG_COMPONENT, "unable to determine doctype");
	    return null;
	}
	Log.debug(LOG_COMPONENT, "determined doctype is \'" + doctype + "\'");
	switch(doctype.trim().toLowerCase())
	{
	case DOCTYPE_FB2:
	    return new Fb2(tmpFile, selectedCharset).createDoc();
	}
	return null;
    }

    static public Format chooseFilterByContentType(String contentType)
    {
	NullCheck.notEmpty(contentType, "contentType");
	switch(contentType.toLowerCase().trim())
	{
	case ContentTypes.TEXT_HTML_DEFAULT:
	    return Format.HTML;
	case CONTENT_TYPE_XML:
	    return Format.XML;
	case CONTENT_TYPE_FB2:
	    return Format.FB2;
	case CONTENT_TYPE_FB2_ZIP:
	case CONTENT_TYPE_ZIP:
	    return Format.FB2_ZIP;
	case CONTENT_TYPE_TXT:
	    return Format.TXT;
	default:
	    return null;
	}
    }

    static public String extractCharset(Path path) throws IOException
    {
	NullCheck.notNull(path, "path");
	final String res = Encoding.getHtmlEncoding(path);
	if (res == null)
	    return "";
	return res;
    }

    static public final class Result
    {
	public enum Type {
	    OK,
	    UNKNOWN_HOST,  //See "host" property
	    HTTP_ERROR, //See "httpcode" property
	    FETCHING_ERROR, //See "descr" property
	    UNDETERMINED_CONTENT_TYPE,
	    UNRECOGNIZED_FORMAT, //See "contenttype" property
	};

	public final Type type;
		public Book book = null;
	public Document doc = null;
	public final Properties props = new Properties();
	Result()
	{
	    this.type = Type.OK;
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

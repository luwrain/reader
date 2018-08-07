
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

public class UrlLoader
{
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

    static public final String PARA_STYLE_EMPTY_LINES = "empty-lines";
    static public final String PARA_STYLE_INDENT = "indent";
    static public final String PARA_STYLE_EACH_LINE = "each-line";

    static public final String USER_AGENT = "Mozilla/5.0";
    static private final String DEFAULT_CHARSET = "UTF-8";
    static private final Txt.ParaStyle DEFAULT_PARA_STYLE = Txt.ParaStyle.EMPTY_LINES;

    enum Format {
	TXT, HTML, XML, DOC, DOCX,
	FB2, FB2_ZIP, EPUB, SMIL,
    };

    private URL requestedUrl;
    private String requestedContentType;
    private String requestedTagRef;
    private URL responseUrl;
    private String responseContentType;
    private String responseContentEncoding;
    private int httpCode;
    private Path tmpFile;
    private String selectedContentType;
    private String selectedCharset;

    public UrlLoader(URL url) throws MalformedURLException
    {
	NullCheck.notNull(url, "url");
	requestedTagRef = url.getRef();
	requestedUrl = new URL(url.getProtocol(), IDN.toASCII(url.getHost()),
			       url.getPort(), url.getFile());
	requestedContentType = "";
    }

    public UrlLoader newUrlLoader(URL url) throws MalformedURLException
    {
	NullCheck.notNull(url, "url");
	return new UrlLoader(url);
    }

    public UrlLoader(URL url, String contentType) throws MalformedURLException
    {
	NullCheck.notNull(url, "url");
	NullCheck.notNull(contentType, "contentType");
	requestedUrl = new URL(url.getProtocol(), IDN.toASCII(url.getHost()),
			       url.getPort(), url.getFile());
	requestedContentType = contentType;
    }

    public Result load() throws IOException
    {
	try {
	    try {
		if (!fetch())
		{
		final Result res = new Result(Result.Type.HTTP_ERROR);
		res.setProperty("url", requestedUrl.toString());
		res.setProperty("httpcode", "" + httpCode);
		return res;
		}
	    }
	    catch (UnknownHostException  e)
	    {
		final Result res = new Result(Result.Type.UNKNOWN_HOST);
		res.setProperty("url", requestedUrl.toString());
		res.setProperty("host", e.getMessage());
		return res;
	    }
	    catch (IOException e)
	    {
		e.printStackTrace();
		final Result res = new Result(Result.Type.FETCHING_ERROR);
		res.setProperty("url", requestedUrl.toString());
		res.setProperty("descr", e.getClass().getName() + ":" + e.getMessage());
		return res;
	    }
	    selectedContentType = requestedContentType.isEmpty()?responseContentType:requestedContentType;
	    if (selectedContentType.isEmpty())
		return new Result(Result.Type.UNDETERMINED_CONTENT_TYPE);
	    if (!requestedContentType.isEmpty())
	    {
		Log.debug("doctree", "requested content type is " + requestedContentType);
		Log.debug("doctree", "response content type is " + responseContentType);
		Log.debug("doctree", "selected content type is " + selectedContentType);
	    } else
		Log.debug("doctree", "response content type is " + responseContentType);
	    Format format = chooseFilterByContentType(extractBaseContentType(selectedContentType));
	    if (format == null)
		format = chooseFilterByFileName(responseUrl);
	    if (format == null)
	    {
		Log.error("doctree", "unable to choose suitable filter depending on selected content type:" + requestedUrl.toString());
		final Result res = new Result(Result.Type.UNRECOGNIZED_FORMAT);
res.setProperty("contenttype", selectedContentType);
res.setProperty("url", responseUrl.toString());
return res;
	    }
	    selectCharset(format);
	    Log.debug("doctree", "selected charset is " + selectedCharset);
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

    // Returns false only on HTTP errors, see httpCode for details.
    // ALl other errors are reported through IOException
    private boolean fetch() throws IOException
    {
	InputStream responseStream = null;
	try {
	    URLConnection con;
	    Log.debug("doctree", "opening connection for " + requestedUrl.toString());
	    con = requestedUrl.openConnection();
	    while(true)
	    {
		con.setRequestProperty("User-Agent", USER_AGENT);
		con.connect();
		if (!(con instanceof HttpURLConnection))
		    break;//Considering everything is OK, but lines below are pointless
		final HttpURLConnection httpCon = (HttpURLConnection)con;
		httpCode = httpCon.getResponseCode();
		Log.debug("doctree", "response code is " + httpCode);
		if (httpCode >= 400 || httpCode < 200)
		    return false;
		if (httpCode >= 200 && httpCode <= 299)
		    break;
		final String location = httpCon.getHeaderField("location");
		if (location == null || location.isEmpty())
		{
		    Log.warning("doctree", "HTTP response code is " + httpCode + " but \'location\' field is empty");
		    return false;
		}
		Log.debug("doctree", "redirected to " + location);
		final URL locationUrl = new URL(location);
		con = locationUrl.openConnection();
	    }
	    responseStream = con.getInputStream();
	    responseUrl = con.getURL();
	    if (responseUrl == null)
		responseUrl = requestedUrl;
	    responseContentType = con.getContentType();
	    if (responseContentType == null)
		responseContentType = "";
	    responseContentEncoding = con.getContentEncoding();
	    if (responseContentEncoding == null)
		responseContentEncoding = "";
	    //						 InputStream is = null;
	    if (responseContentEncoding.toLowerCase().trim().equals("gzip"))
	    {
		Log.debug("doctree", "enabling gzip decompressing");
		downloadToTmpFile(new GZIPInputStream(responseStream));
	    } else
		downloadToTmpFile(responseStream);
	    return true;
	}
	finally {
	    if (responseStream != null)
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
	selectedCharset = extractCharset(selectedContentType);
	if (!selectedCharset.isEmpty())
	    return;
	switch(format)
	{
	case XML:
	    selectedCharset = XmlEncoding.getEncoding(tmpFile);
	    break;
	case HTML:
	    selectedCharset = extractCharset(tmpFile);
	    break;
	}
	if (selectedCharset == null || selectedCharset.isEmpty())
	    selectedCharset = DEFAULT_CHARSET;
    }

    private Result parse(Format format) throws IOException
    {
	NullCheck.notNull(format, "format");
	Log.debug("doctree", "parsing the document as " + format.toString());
	InputStream stream = null;
	try {
	    stream = Files.newInputStream(tmpFile);
	    final Result res = new Result(Result.Type.OK);
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
		switch(extractParaStyle(selectedContentType))
		{
		case PARA_STYLE_EMPTY_LINES:
		    res.doc = new Txt(Txt.ParaStyle.EMPTY_LINES, tmpFile, selectedCharset).constructDocument();
		    return res;
		case PARA_STYLE_INDENT:
		    		    res.doc = new Txt(Txt.ParaStyle.INDENT, tmpFile, selectedCharset).constructDocument();
		    return res;
		case PARA_STYLE_EACH_LINE:
		    res.doc = new Txt(Txt.ParaStyle.EACH_LINE, tmpFile, selectedCharset).constructDocument();
		    return res;
		default:
		    res.doc = new Txt(DEFAULT_PARA_STYLE, tmpFile, selectedCharset).constructDocument();
		    return res;
		}
	    default:
		return new Result(Result.Type.UNRECOGNIZED_FORMAT);
	    }
	}
	finally {
	    if (stream != null)
		stream.close();
	}
    }

    private Document readXml() throws IOException
    {
	final String doctype = getDoctypeName(Files.newInputStream(tmpFile));
	if (doctype == null || doctype.trim().isEmpty())
	{
	    Log.debug("doctree", "unable to determine doctype");
	    return null;
	}
	Log.debug("doctree", "determined doctype is \'" + doctype + "\'");
	switch(doctype.trim().toLowerCase())
	{
	case DOCTYPE_FB2:
	    return new Fb2(tmpFile, selectedCharset).createDoc();
	}
	return null;
    }

    static Format chooseFilterByContentType(String contentType)
    {
	NullCheck.notEmpty(contentType, "contentType");
	switch(contentType.toLowerCase().trim())
	{
	case CONTENT_TYPE_HTML:
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

    static private Format chooseFilterByFileName(URL url)
    {
	NullCheck.notNull(url, "url");
	final String ext = org.luwrain.core.FileTypes.getExtension(url).toLowerCase();
	if (ext.isEmpty())
	    return null;
	Log.debug("doctree", "extracted extension is \'" + ext + "\'");
	switch(ext)
	{
	case "docx":
	    return Format.DOCX;
	case "doc":
	    return Format.DOC;
	case "fb2":
	    return Format.FB2;
	default:
	    return null;
	}
    }

    static private String extractCharset(Path path) throws IOException
    {
	NullCheck.notNull(path, "path");
		Log.debug("doctree", "trying to get charset information from HTML header in " + path);
	final String res = HtmlEncoding.getEncoding(path);
	if (res == null)
	    return "";
	Log.debug("doctree", "determined charset is \'" + res + "\'");
	return res;
    }

    static String extractBaseContentType(String value)
    {
	NullCheck.notEmpty(value, "value");
	try {
	    final MimeType mime = new MimeType(value);
	    final String res = mime.getBaseType();
	    return res != null?res:"";
	}
	catch(MimeTypeParseException e)
	{
	    e.printStackTrace();
	    return "";
	}
    }

    static String extractCharset(String value)
    {
	NullCheck.notEmpty(value, "value");
	try {
	    final MimeType mime = new MimeType(value);
	    final String res = mime.getParameter("charset");
	    return res != null?res:"";
	}
	catch(MimeTypeParseException e)
	{
	    e.printStackTrace();
	    return "";
	}
    }

    static private String extractParaStyle(String value)
    {
	NullCheck.notEmpty(value, "value");
	try {
	    final MimeType mime = new MimeType(value);
	    final String res = mime.getParameter("parastyle");
	    return res != null?res:"";
	}
	catch(MimeTypeParseException e)
	{
	    e.printStackTrace();
	    return "";
	}
    }


    static private String getDoctypeName(InputStream s) throws IOException
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

	private Type type = Type.OK;
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

	public Type type() { return type; }
	public Document doc() { return doc; }
		public Book book() {return book;}
    }
}

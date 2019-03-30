/*
   Copyright 2012-2019 Michael Pozhidaev <michael.pozhidaev@gmail.com>
   Copyright 2015-2016 Roman Volovodov <gr.rPman@gmail.com>

   This file is part of LUWRAIN.

   LUWRAIN is free software; you can redistribute it and/or
   modify it under the terms of the GNU General Public
   License as published by the Free Software Foundation; either
   version 3 of the License, or (at your option) any later version.

   LUWRAIN is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
   General Public License for more details.
*/

package org.luwrain.app.reader;

import java.net.*;
import java.io.*;
import java.nio.file.*;
import java.nio.charset.*;
import java.util.*;
import java.util.zip.*;

import org.luwrain.core.*;
import org.luwrain.util.*;
import org.luwrain.reader.*;
import org.luwrain.app.reader.formats.*;
import org.luwrain.app.reader.books.*;

public final class UrlLoader
{
    static private final String LOG_COMPONENT = "reader";

    enum Format {
	TXT,
	HTML,
	XML,
	FB2,
	FB2_ZIP,
	//	EPUB,
    };

    static public final String CONTENT_TYPE_DATA = "application/octet-stream";
    static public final String CONTENT_TYPE_PDF = "application/pdf";
    static public final String CONTENT_TYPE_POSTSCRIPT = "application/postscript";
    static public final String CONTENT_TYPE_XHTML = "application/xhtml";
    static public final String CONTENT_TYPE_ZIP = "application/zip";
    static public final String CONTENT_TYPE_HTML = "text/html";
    static public final String CONTENT_TYPE_TXT = "text/plain";
    static public final String CONTENT_TYPE_XML = "application/xml";
    static public final String CONTENT_TYPE_FB2 = "application/fb2";
    static public final String CONTENT_TYPE_FB2_ZIP = "application/fb2+zip";

    static private final String DOCTYPE_FB2 = "fictionbook";

    static private final String DEFAULT_CHARSET = "UTF-8";

    private final Luwrain luwrain;
    final URL requestedUrl;
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

    public String getContentType()
    {
	if (selectedContentType != null && !selectedContentType.isEmpty())
	    return selectedContentType;
	return requestedContentType != null?requestedContentType:"";
    }

    public void setCharset(String charset)
    {
	NullCheck.notEmpty(charset, "charset");
	this.requestedCharset = charset;
    }

        public String getCharset()
    {
	if (selectedCharset != null && !selectedCharset.isEmpty())
	    return selectedCharset;
	return requestedCharset != null?requestedCharset:"";
    }

    void setTxtParaStyle(TextFiles.ParaStyle paraStyle)
    {
	NullCheck.notNull(paraStyle, "paraStyle");
	this.requestedTxtParaStyle = paraStyle;
    }

    public Result load() throws IOException
    {
	try {
	    Log.debug(LOG_COMPONENT, "fetching " + requestedUrl.toString());
	    fetch();
	    this.selectedContentType = requestedContentType.isEmpty()?responseContentType:requestedContentType;
	    if (selectedContentType.isEmpty() || ContentTypes.isUnknown(selectedContentType))
		this.selectedContentType = luwrain.suggestContentType(requestedUrl, ContentTypes.ExpectedType.TEXT);
	    if (selectedContentType.isEmpty())
		throw new IOException("Unable to understand the content type");
	    Log.debug(LOG_COMPONENT, "selected content type is " + selectedContentType);
	    final Format format = chooseFilterByContentType(Utils.extractBaseContentType(selectedContentType));
	    final Result res;
	    if (format == null)
	    {

		this.selectedCharset = Utils.extractCharset(selectedContentType);
		if (this.selectedCharset.isEmpty())
		    this.selectedCharset = requestedCharset;
		Log.debug(LOG_COMPONENT, "trying to use extensible document builders, contentType=" + selectedContentType + ", charset=" + selectedCharset);
		final DocumentBuilderHook builderHook = new DocumentBuilderHook(luwrain);
		final Document hookDoc = builderHook.build(Utils.extractBaseContentType(selectedContentType), new Properties(), tmpFile.toFile());
		if (hookDoc != null)
		{
		    Log.debug(LOG_COMPONENT, "the builder hook  has constructed the document");
		    res = new Result();
		    res.doc = hookDoc;
		} else
		{
		    Log.debug(LOG_COMPONENT, "the builder hook failed");
		    final DocumentBuilder builder = new DocumentBuilderLoader().newDocumentBuilder(luwrain, Utils.extractBaseContentType(selectedContentType));
		    if (builder == null)
			throw new IOException("No suitable handler for the content type: " + selectedContentType);
		    res = new Result();
		    final Properties props = new Properties();
		    props.setProperty("url", responseUrl.toString());
		    props.setProperty("charset", selectedCharset);
		    res.doc = builder.buildDoc(tmpFile.toFile(), props);
		}
	    } else
	    {
		selectCharset(format);
		res = parse(format);
	    }
	    if (res.doc == null)
		throw new IOException("No suitable handler for the content type: " + selectedContentType);
	    res.doc.setProperty("url", responseUrl.toString());
	    res.doc.setProperty("contenttype", selectedContentType);
	    if (format == Format.TXT)
		res.doc.setProperty("charset", selectedCharset);
	    if (requestedTagRef != null)
		res.doc.setProperty("startingref", requestedTagRef);
	    if (responseUrl.getFile().toLowerCase().endsWith("/ncc.html"))
	    {
		res.book = BookFactory.initDaisy2(luwrain, res.doc);
		res.doc = null;
	    }
	    return res;
	}
	finally {
	    if (tmpFile != null)
	    {
		Log.debug(LOG_COMPONENT, "deleting temporary file " + tmpFile.toString());
		Files.delete(tmpFile);
		tmpFile = null;
	    }
	}
    }

    private void fetch() throws IOException
    {
	final URLConnection con;
	try {
	    con = Connections.connect(requestedUrl.toURI(), 0);
	}
	catch(URISyntaxException e)
	{
	    throw new IOException(e);
	}
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
	tmpFile = Files.createTempFile("tmplwr-reader-", ".dat");
	Log.debug(LOG_COMPONENT, "creating temporary file " + tmpFile.toString());
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
	final Result res = new Result();
	final InputStream 	    stream = Files.newInputStream(tmpFile);
	try {
	    switch(format)
	    {
		/*
	    case HTML:
		res.doc = new Html(stream, selectedCharset, responseUrl).constructDocument();
		return res;
		*/
	    case XML:
		res.doc = readXml();
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
			    Log.error(LOG_COMPONENT, "unable to read FB2 subdoc in ZIP:" + e.getClass().getName() + ":" + e.getMessage());
			    return null;
			}
}).createDoc();
		return res;
	    case TXT:
		res.doc = new TextFiles(tmpFile.toFile(), makeTitleFromUrl(), selectedCharset, requestedTxtParaStyle).makeDoc();
		    return res;
	    default:
			throw new IOException("No suitable handler for the content type: " + selectedContentType);
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

    private String makeTitleFromUrl()
    {
	final String path = responseUrl.getPath();
	if (path == null || path.isEmpty())
	    return responseUrl.toString();
	final int lastSlashPos = path.lastIndexOf("/");
	final String fileName = (lastSlashPos >= 0 && lastSlashPos + 1 < path.length())?path.substring(lastSlashPos + 1):path;
	try {
	    return URLDecoder.decode(fileName, "UTF-8");
	}
	catch(IOException e)
	{
	    return fileName;
	}
    }

    static public Format chooseFilterByContentType(String contentType)
    {
	NullCheck.notEmpty(contentType, "contentType");
	switch(contentType.toLowerCase().trim())
	{
	    /*
	case ContentTypes.TEXT_HTML_DEFAULT:
	    return Format.HTML;
	    */
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
		public Book book = null;
	public Document doc = null;
    }
}

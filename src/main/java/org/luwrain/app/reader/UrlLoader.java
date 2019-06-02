/*
   Copyright 2012-2019 Michael Pozhidaev <msp@luwrain.org>
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
import java.util.*;
import java.util.zip.*;

import org.luwrain.core.*;
import org.luwrain.util.*;
import org.luwrain.reader.*;
import org.luwrain.app.reader.books.*;

import org.luwrain.app.reader.Base.ParaStyle;

public final class UrlLoader
{
    static private final String LOG_COMPONENT = Base.LOG_COMPONENT;
    static private final String DEFAULT_CHARSET = "UTF-8";

    private final Luwrain luwrain;
    final URL requestedUrl;
    private String requestedContentType = "";
    private String requestedTagRef = "";
    private String requestedCharset = "";
    private ParaStyle requestedTxtParaStyle = ParaStyle.EMPTY_LINES;

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

    void setTxtParaStyle(ParaStyle paraStyle)
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
	    final Result res;
	    this.selectedCharset = Utils.extractCharset(selectedContentType);
	    if (!this.requestedCharset.isEmpty())
		this.selectedCharset = this.requestedCharset;
	    if (this.selectedCharset.isEmpty())
		this.selectedCharset = DEFAULT_CHARSET;
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
	    if (res.doc == null)
		throw new IOException("No suitable handler for the content type: " + selectedContentType);
	    res.doc.setProperty("url", responseUrl.toString());
	    res.doc.setProperty("contenttype", selectedContentType);
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

    static public final class Result
    {
	public Book book = null;
	public Document doc = null;
    }
}

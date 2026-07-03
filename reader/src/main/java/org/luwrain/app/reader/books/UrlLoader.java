// SPDX-License-Identifier: BUSL-1.1
// Copyright 2012-2026 Michael Pozhidaev <msp@luwrain.org>

package org.luwrain.app.reader.books;

import java.net.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.zip.*;

import org.luwrain.core.*;
import org.luwrain.util.*;
import org.luwrain.io.bookdoc.*;

import static java.util.Objects.*;

public final class UrlLoader
{
    static private final String LOG_COMPONENT = "reader";
    static private final String DEFAULT_CHARSET = "UTF-8";

    private final Luwrain luwrain;
    final URL requestedUrl;
    private String requestedContentType = "";
    private String requestedTagRef = "";
    private String requestedCharset = "";

    private URL responseUrl = null;
    private String responseContentType = "";
    private String responseContentEncoding = "";

    private String selectedContentType = "";
    private String selectedCharset = "";

    private Path tmpFile;

    public UrlLoader(Luwrain luwrain, URL url) throws MalformedURLException
    {
	requireNonNull(luwrain, "luwrain can't be null");
	requireNonNull(url, "url can't be null");
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

    public Result load() throws IOException
    {
	try {
	    Log.debug(LOG_COMPONENT, "fetching " + requestedUrl.toString());
	    fetch();
	    this.selectedContentType = requestedContentType.isEmpty()?responseContentType:requestedContentType;
	    if (selectedContentType.isEmpty() || selectedContentType.equalsIgnoreCase("content/unknown"))
		this.selectedContentType = null;
	    if (selectedContentType == null || selectedContentType.isEmpty())
		throw new IOException("Unable to understand the content type");
	    Log.debug(LOG_COMPONENT, "selected content type is " + selectedContentType);
	    final Result res;
	    this.selectedCharset = Utils.extractCharset(selectedContentType);
	    if (!this.requestedCharset.isEmpty())
		this.selectedCharset = this.requestedCharset;
	    if (this.selectedCharset.isEmpty())
		this.selectedCharset = DEFAULT_CHARSET;
	    {
		final Result result = new Result();
		final Loader loader = Loader.newDefaultLoader(responseUrl.toURI(), selectedContentType);
		if (loader == null)
		    throw new IOException("No suitable loader for the content type: " + selectedContentType);
		result.doc = loader.load();
		if (result.doc == null)
		    throw new IOException("No suitable handler for the content type: " + selectedContentType);
		result.doc.setProperty("url", responseUrl.toString());
		result.doc.setProperty("contenttype", selectedContentType);
		if (requestedTagRef != null)
		    result.doc.setProperty("startingref", requestedTagRef);
		return result;
	    }
	}
	catch(URISyntaxException e)
	{
	    throw new IOException(e);
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
	requireNonNull(s, "s can't be null");
	tmpFile = Files.createTempFile("tmplwr-reader-", ".dat");
	Log.debug(LOG_COMPONENT, "creating temporary file " + tmpFile.toString());
	Files.copy(s, tmpFile, StandardCopyOption.REPLACE_EXISTING);
    }

    static public final class Result
    {
	public Book book = null;
	public Doc doc = null;
    }
}

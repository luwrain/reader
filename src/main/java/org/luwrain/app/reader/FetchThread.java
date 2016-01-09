/*
   Copyright 2012-2015 Michael Pozhidaev <michael.pozhidaev@gmail.com>

   This file is part of the LUWRAIN.

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

import org.luwrain.core.*;
import org.luwrain.util.MlReader;
import org.luwrain.doctree.filters.HtmlEncoding;

class FetchThread implements Runnable
{
    static private final String DEFAULT_ENCODING = "UTF-8";

    private Luwrain luwrain;
    private Area area;
    private URL url;
	private URL resultUrl = null;
    private boolean done = false;

    FetchThread(Luwrain luwrain,
		Area area,
		URL url)
    {
	this.luwrain = luwrain;
	this.area = area;
	this.url = url;
	NullCheck.notNull(luwrain, "luwrain");
	NullCheck.notNull(area, "area");
	NullCheck.notNull(url, "url");
    }

    @Override public void run()
    {
	done = false;
	try {
	    final String text = impl();
	    luwrain.enqueueEvent(new FetchEvent(area, text, resultUrl));
	}
	catch (Exception e)
	{
	    e.printStackTrace();
	    luwrain.enqueueEvent(new FetchEvent(area, e.getMessage()));
	}
	done = true;
    }

    private String impl() throws Exception
    {
    	System.out.println("progress");
	URLConnection con;
	InputStream inputStream = null;
	String contentTypeCharset = null;
	File tmpFile = null;
	byte[] content = null;
	try {
	    con = url.openConnection();
	    con.setRequestProperty("User-Agent", "Mozilla/4.0");
	    contentTypeCharset = getContentTypeCharset(con.getContentType());
	    inputStream = con.getInputStream();
	    tmpFile = File.createTempFile(this.getClass().getName(), null);
	    final Path path = tmpFile.toPath();
	    Files.copy(inputStream, path, StandardCopyOption.REPLACE_EXISTING);
	    //FIXME:Check the size;
	    content = Files.readAllBytes(path);
	    resultUrl = con.getURL();
	}
	finally
	{
	    if (inputStream != null)
		inputStream.close();
	    if (tmpFile != null)
		tmpFile.delete();
	}
	if (contentTypeCharset != null)
	{
	    try {
		return new String(content, contentTypeCharset);
	    }
	    catch (UnsupportedEncodingException e){}
	}
	final String encoding = htmlEncoding(new String(content, "US-ASCII"));
	if (encoding == null || encoding.trim().isEmpty())
	    return new String(content, DEFAULT_ENCODING);
	try {
	    return new String(content, encoding);
	}
	catch (UnsupportedEncodingException e)
	{
	    return new String(content, DEFAULT_ENCODING);
	}
    }

    static private String htmlEncoding(String text)
    {
	HtmlEncoding encoding = new HtmlEncoding();
	new MlReader(encoding, encoding, text).read();
	return encoding.getEncoding();
    }

    static private String getContentTypeCharset(String text)
    {
	if (text == null || text.trim().isEmpty())
	    return null;
	final String[] values = text.split(";");
	for (String v: values)
	{
	    final String adjusted = v.toLowerCase().trim();
	    if (adjusted.startsWith("charset="))
		return adjusted.substring("charset=".length());
	}
	return null;
    }
}

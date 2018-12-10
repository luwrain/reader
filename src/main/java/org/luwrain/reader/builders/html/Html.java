/*
   Copyright 2012-2018 Michael Pozhidaev <michael.pozhidaev@gmail.com>
   Copyright 2015-2016 Roman Volovodov <gr.rPman@gmail.com>
o
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

package org.luwrain.reader.builders.html;

import java.io.*;
import java.util.*;
import java.net.*;

import org.luwrain.core.*;
import org.luwrain.reader.*;

final class Html implements DocumentBuilder
{
    static private final String LOG_COMPONENT = "docbuilders";

    @Override public Document buildDoc(File file, Properties props) throws IOException
    {
	NullCheck.notNull(file, "file");
	NullCheck.notNull(props, "props");
	final String urlStr = props.getProperty("url");
	if (urlStr == null || urlStr.isEmpty())
	{
	    Log.error(LOG_COMPONENT, "no \'url\' property");
	    return null;
	}
	final URL url;
	try {
	    url = new URL(urlStr);
	}
	catch(MalformedURLException e)
	{
	    Log.error(LOG_COMPONENT, "invalid URL: " + urlStr);
	    return null;
	}
	final String charset = props.getProperty("charset");
	if (charset == null || charset.isEmpty())
	{
	    Log.error(LOG_COMPONENT, "no \'charset\' property");
	    return null;
	}
	final Document doc = new org.luwrain.app.reader.formats.Html(file.toPath(), charset, url).constructDocument();
doc.setProperty("url", url.toString());
doc.setProperty("contenttype", ContentTypes.TEXT_HTML_DEFAULT);
doc.setProperty("charset", charset);
	return doc;
    }

    @Override public Document buildDoc(String text, Properties props)
{
    NullCheck.notNull(text, "text");
    NullCheck.notNull(props, "props");
    	final String urlStr = props.getProperty("url");
	if (urlStr == null || urlStr.isEmpty())
	{
	    Log.error(LOG_COMPONENT, "no \'url\' property");
	    return null;
	}
	final URL url;
	try {
	    url = new URL(urlStr);
	}
	catch(MalformedURLException e)
	{
	    Log.error(LOG_COMPONENT, "invalid URL: " + urlStr);
	    return null;
	}
	final Document doc = new org.luwrain.app.reader.formats.Html(text, url).constructDocument();
doc.setProperty("url", url.toString());
doc.setProperty("contenttype", ContentTypes.TEXT_HTML_DEFAULT);
	return doc;
        }

    @Override public Document buildDoc(InputStream is, Properties props) throws IOException
    {
	NullCheck.notNull(is, "is");
	NullCheck.notNull(props, "props");
		final String urlStr = props.getProperty("url");
	if (urlStr == null || urlStr.isEmpty())
	{
	    Log.error(LOG_COMPONENT, "no \'url\' property");
	    return null;
	}
	final URL url;
	try {
	    url = new URL(urlStr);
	}
	catch(MalformedURLException e)
	{
	    Log.error(LOG_COMPONENT, "invalid URL: " + urlStr);
	    return null;
	}
	final String charset = props.getProperty("charset");
	if (charset == null || charset.isEmpty())
	{
	    Log.error(LOG_COMPONENT, "no \'charset\' property");
	    return null;
	}
	final Document doc = new org.luwrain.app.reader.formats.Html(is, charset, url).constructDocument();
doc.setProperty("url", url.toString());
doc.setProperty("contenttype", ContentTypes.TEXT_HTML_DEFAULT);
doc.setProperty("charset", charset);
	return doc;
    }

}

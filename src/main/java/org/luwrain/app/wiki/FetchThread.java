/*
   Copyright 2012-2015 Michael Pozhidaev <michael.pozhidaev@gmail.com>

   This file is part of the Luwrain.

   Luwrain is free software; you can redistribute it and/or
   modify it under the terms of the GNU General Public
   License as published by the Free Software Foundation; either
   version 3 of the License, or (at your option) any later version.

   Luwrain is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
   General Public License for more details.
*/

package org.luwrain.app.wiki;

import java.util.*;
import java.net.*;
import java.io.*;
import org.w3c.dom.*;
import org.xml.sax.*;
import javax.xml.parsers.*;

import org.luwrain.core.*;
import org.luwrain.util.MlTagStrip;

class FetchThread implements Runnable
{
    private Luwrain luwrain;
    private Area area;
    private String lang = "";
    private String query = "";
    private boolean done = false;
    private LinkedList<Page> res = new LinkedList<Page>();

    public FetchThread(Luwrain luwrain,
		       Area area,
		       String lang,
		       String query)
    {
	this.luwrain = luwrain;
	this.area = area;
	this.lang = lang;
	this.query = query;
	this.done = false;
	if (luwrain == null)
	    throw new NullPointerException("luwrain may not be null");
	if (area == null)
	    throw new NullPointerException("area may not be null");
	if (lang == null)
	    throw new NullPointerException("lang may not be null");
	if (query == null)
	    throw new NullPointerException("query may not be null");
    }

    @Override public void run()
    {
	done = false;
	try {
	    impl();
	}
	catch (Exception e)
	{
	    e.printStackTrace();
	    luwrain.enqueueEvent(new FetchEvent(area));
	}
	done = true;
    }

    private void impl() throws Exception
    {
	final URL url = new URL("https://" + lang + ".wikipedia.org/w/api.php?action=query&list=search&srsearch=" + URLEncoder.encode(query, "UTF-8") + "&format=xml");
	System.out.println("url=" + url.toString());
	final DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
	final Document document = builder.parse(new InputSource(url.openStream()));
	final NodeList nodes = document.getElementsByTagName("p");
	for (int i = 0;i < nodes.getLength();++i)
	{
	    final Node node = nodes.item(i);
	    if (node.getNodeType() != Node.ELEMENT_NODE)
		continue;
	    final Element el = (Element)node;
	    final NamedNodeMap attr = el.getAttributes();
	    final Node title = attr.getNamedItem("title");
	    final Node snippet = attr.getNamedItem("snippet");
	    if (title != null)
		res.add(new Page(lang, title.getTextContent(), snippet != null?MlTagStrip.run(snippet.getTextContent()):""));
	}
	luwrain.enqueueEvent(new FetchEvent(area, res.toArray(new Page[res.size()])));
	res.clear();
    }

    public boolean done()
    {
	return done;
    }
}

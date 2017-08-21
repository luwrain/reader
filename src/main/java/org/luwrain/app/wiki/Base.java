/*
   Copyright 2012-2017 Michael Pozhidaev <michael.pozhidaev@gmail.com>

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

package org.luwrain.app.wiki;

import java.util.*;
import java.util.concurrent.*;
import java.net.*;
import java.io.*;
import org.w3c.dom.*;
import org.xml.sax.*;
import javax.xml.parsers.*;

import org.luwrain.core.*;
import org.luwrain.controls.*;
import org.luwrain.util.MlTagStrip;

class Base
{
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private final Luwrain luwrain;
    private final Strings strings;
    private FutureTask task;
    private Page[] searchResult = new Page[0];

    Base(Luwrain luwrain, Strings strings)
    {
	NullCheck.notNull(luwrain, "luwrain");
	NullCheck.notNull(strings, "strings");
	this.luwrain = luwrain;
	this.strings = strings;
    }

    boolean search(String lang, String query, ConsoleArea2 area)
    {
	NullCheck.notEmpty(lang, "lang");
	NullCheck.notEmpty(query, "query");
	NullCheck.notNull(area, "area");
	if (task != null && !task.isDone())
	    return false;
	task = createTask(area, lang, query);
	executor.execute(task);
	luwrain.onAreaNewBackgroundSound(area);
	return true;
    }

    boolean isBusy()
    {
	return task != null && !task.isDone();
    }

    ConsoleArea2.Model getModel()
    {
	NullCheck.notNullItems(searchResult, "searchResult");
	return new ConsoleArea2.Model(){
	    @Override public int getConsoleItemCount()
	    {
		NullCheck.notNullItems(searchResult, "searchResult");
		return searchResult.length;
	    }
	    @Override public Object getConsoleItem(int index)
	    {
		if (index < 0 || index >= searchResult.length)
		    throw new IllegalArgumentException("Illegal index value (" + index + ")");
		return searchResult[index];
	    }
	};
    }

    ConsoleArea2.Appearance getAppearance()
    {
	return new ConsoleArea2.Appearance(){
	    @Override public void announceItem(Object item)
	    {
		NullCheck.notNull(item, "item");
		if (!(item instanceof Page))
		    return;
		luwrain.playSound(Sounds.LIST_ITEM);
		final Page page = (Page)item ;
		luwrain.say(item.toString());
	    }
	    @Override public String getTextAppearance(Object item)
	    {
		NullCheck.notNull(item, "item");
		return item.toString();
	    }
	};
    }

    private FutureTask createTask(ConsoleArea2 area, String lang, String query)
    {
	NullCheck.notNull(area, "area");
	NullCheck.notNull(lang, "lang");
	NullCheck.notNull(query, "query");
	return new FutureTask(()->{
		final List<Page> res = new LinkedList<Page>();
		final URL url;
		try {
		    url = new URL("https://" + URLEncoder.encode(lang) + ".wikipedia.org/w/api.php?action=query&list=search&srsearch=" + URLEncoder.encode(query, "UTF-8") + "&format=xml");
		}
		catch(MalformedURLException | UnsupportedEncodingException e)
		{
		    luwrain.crash(e);
		    return;
		}
		try {
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
		}
		catch(ParserConfigurationException | IOException | SAXException e)
		{
		    luwrain.message(e.getMessage(), Luwrain.MessageType.ERROR);
		}
		luwrain.runInMainThread(()->{
			task = null;
			searchResult = res.toArray(new Page[res.size()]);
	luwrain.onAreaNewBackgroundSound(area);
			if (searchResult.length > 0)
			    luwrain.message(strings.querySuccess("" + searchResult.length), Luwrain.MessageType.DONE); else
			    luwrain.message(strings.nothingFound(), Luwrain.MessageType.DONE);
			area.refresh();
		    });
	}, null);
    }
}


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

    private Luwrain luwrain;
    private ListUtils.FixedModel model;
    private Appearance appearance;
    private FutureTask task;

    boolean init(Luwrain luwrain, Strings strings)
    {
	NullCheck.notNull(luwrain, "luwrain");
	NullCheck.notNull(strings, "strings");
	this.luwrain = luwrain;
	this.model = new ListUtils.FixedModel();
	this.appearance = new Appearance(luwrain, strings);
	return true;
    }

    boolean search(String lang, String query,
		   WikiApp actions)
    {
	NullCheck.notNull(lang, "lang");
	NullCheck.notNull(query, "query");
	NullCheck.notNull(actions, "actions");
	if (task != null && !task.isDone())
	    return false;
	task = createTask(actions, lang, query);
	executor.execute(task);
	return true;
    }

    boolean isBusy()
    {
	return task != null && !task.isDone();
    }

ConsoleArea2.Model getModel()
    {
	return null;
    }

    ConsoleArea2.Appearance getAppearance()
    {
	return null;
    }

    private FutureTask createTask(WikiApp actions,
				  String lang, String query)
    {
	return new FutureTask(()->{
		final LinkedList<Page> res = new LinkedList<Page>();
		URL url = null;
		try {
		    url = new URL("https://" + URLEncoder.encode(lang) + ".wikipedia.org/w/api.php?action=query&list=search&srsearch=" + URLEncoder.encode(query, "UTF-8") + "&format=xml");
		}
		catch(MalformedURLException | UnsupportedEncodingException e)
		{
		    e.printStackTrace();
		    luwrain.runInMainThread(()->luwrain.message(e.getMessage(), Luwrain.MESSAGE_ERROR));
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
		    e.printStackTrace();
		    luwrain.runInMainThread(()->luwrain.message(e.getMessage(), Luwrain.MESSAGE_ERROR));
		}
		luwrain.runInMainThread(()->actions.showQueryRes(res.toArray(new Page[res.size()])));
	}, null);
    }
}

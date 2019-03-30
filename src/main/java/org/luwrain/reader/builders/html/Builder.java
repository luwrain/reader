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

package org.luwrain.reader.builders.html;

import java.io.*;
import java.util.*;
import java.net.*;

import org.jsoup.*;
import org.jsoup.nodes.*;
import org.jsoup.select.*;

import org.luwrain.core.*;
import org.luwrain.reader.NodeBuilder;

final class Builder extends Base implements org.luwrain.reader.DocumentBuilder
{
    private org.jsoup.nodes.Document jsoupDoc = null;
    private URL docUrl = null;

    private final LinkedList<String> hrefStack = new LinkedList();
    private final List<String> allHrefs = new LinkedList();

    @Override public org.luwrain.reader.Document buildDoc(File file, Properties props) throws IOException
    {
	NullCheck.notNull(file, "file");
	NullCheck.notNull(props, "props");
	final InputStream is = new FileInputStream(file);
	try {
	    return buildDoc(is, props);
	}
	finally {
	    is.close();
	}
    }

    @Override public org.luwrain.reader.Document buildDoc(String text, Properties props)
{
    throw new RuntimeException("Not implemented");
        }

    @Override public org.luwrain.reader.Document buildDoc(InputStream is, Properties props) throws IOException
    {
	NullCheck.notNull(is, "is");
	NullCheck.notNull(props, "props");
		final String urlStr = props.getProperty("url");
	if (urlStr == null || urlStr.isEmpty())
throw new IOException("no \'url\' property");
	    this.docUrl = new URL(urlStr);
	final String charset = props.getProperty("charset");
	if (charset == null || charset.isEmpty())
	    throw new IOException("no \'charset\' property");
	this.jsoupDoc = Jsoup.parse(is, charset, docUrl.toString());
	final org.luwrain.reader.Document doc = constructDoc();
doc.setProperty("url", docUrl.toString());
doc.setProperty("contenttype", ContentTypes.TEXT_HTML_DEFAULT);
doc.setProperty("charset", charset);
	return doc;
    }

	    private org.luwrain.reader.Document constructDoc()
    {
	final NodeBuilder nodeBuilder = new NodeBuilder();
	final Map<String, String> meta = new HashMap();
	collectMeta(jsoupDoc.head(), meta);
	nodeBuilder.addSubnodes(onNode(jsoupDoc.body()));
	final org.luwrain.reader.Document doc = new org.luwrain.reader.Document(jsoupDoc.title(), nodeBuilder.newRoot());
	doc.setHrefs(allHrefs.toArray(new String[allHrefs.size()]));
	return doc;
    }

    private org.luwrain.reader.Node[] onNode(org.jsoup.nodes.Node node)
    {
	NullCheck.notNull(node, "node");
	final List<org.luwrain.reader.Node> resNodes = new LinkedList();
	final List<org.luwrain.reader.Run> runs = new LinkedList();
	final List<Node> nodes = node.childNodes();
	if (nodes == null)
	    return new org.luwrain.reader.Node[0];
	for(Node n: nodes)
	{
	    if (n instanceof TextNode)
	    {
		final TextNode textNode = (TextNode)n;
		onTextNode(textNode, runs);
		/*
		final String text = textNode.text();
		if (text != null && !text.isEmpty())
		    runs.add(new org.luwrain.reader.TextRun(text, !hrefStack.isEmpty()?hrefStack.getLast():"", getCurrentExtraInfo()));
		*/
		continue;
	    }
	    if (n instanceof Element)
	    {
		final Element el = (Element)n;
		{
		    onElement((Element)n, resNodes, runs);
		    continue;
		}
	    }

	    	    if (n instanceof Comment)
			continue;
			
	    
	    		Log.warning(LOG_COMPONENT, "unprocessed node of class " + n.getClass().getName());
	}
	commitParagraph(resNodes, runs);
	return resNodes.toArray(new org.luwrain.reader.Node[resNodes.size()]);
    }

    private void onElementInPara(Element el,
				 List<org.luwrain.reader.Node> nodes, List<org.luwrain.reader.Run> runs)
    {
	NullCheck.notNull(el, "el");
	NullCheck.notNull(nodes, "nodes");
	NullCheck.notNull(runs, "runs");
	final String tagName = el.nodeName();
	String href = null;
	//img
	if (tagName.toLowerCase().trim().equals("img"))
	{
	    final String value = el.attr("alt");
	    if (value != null && !value.isEmpty())
		runs.add(new org.luwrain.reader.TextRun("[" + value + "]", !hrefStack.isEmpty()?hrefStack.getLast():"", getCurrentExtraInfo()));
	    //Do nothing else here	    
	    return;
	}

	//a
	if (tagName.toLowerCase().trim().equals("a"))
	{
	    final String value = el.attr("href");
	    if (value != null)
	    {
	    allHrefs.add(value);
	    
		try {
		    href = new URL(docUrl, value).toString();
		}
		catch(MalformedURLException e)
		{
		    href = value;
		}
	    } else
		href = value;
	}
	if (href != null)
	{
	    hrefStack.add(href);
	}
	try {
	    final List<Node> nn = el.childNodes();
	    for(Node n: nn)
	    {
		if (n instanceof TextNode)
		{
		    onTextNode((TextNode)n, runs);
		    continue;
		}
		if (n instanceof Element)
		{
		    onElement((Element)n, nodes, runs);
		    continue;
		}
		if (n instanceof Comment)
		    continue;
		Log.warning(LOG_COMPONENT, "encountering unexpected node of class " + n.getClass().getName());
	    }
	}
	finally
	{
	    if (href != null)
		hrefStack.pollLast();
	}
    }

    private void onElement(Element el,
			   List<org.luwrain.reader.Node> nodes, List<org.luwrain.reader.Run> runs)
    {
	NullCheck.notNull(el, "el");
	NullCheck.notNull(nodes, "nodes");
	NullCheck.notNull(runs, "runs");
	final String tagName;
	{
	final String name = el.nodeName();
	if (name == null || name.trim().isEmpty())
	    return;
tagName = name.trim().toLowerCase();
	}
	if (tagName.startsWith("g:") ||
	    tagName.startsWith("g-") ||
	    tagName.startsWith("fb:"))
	    return;
	switch(tagName)
	{
	case "script":
	case "style":
	case "hr":
	case "input":
	case "button":
	case "nobr":
	case "wbr":
	case "map":
	case "svg":
	    return;
	}
	switch(tagName)
	{
	case "br":
	    commitParagraph(nodes, runs);
	    break;
	case "p":
	case "div":
	case "main":
	case "noscript":
	case "header":
	case "footer":
	case "center":
	case "blockquote":
	case "tbody":
	case "figure":
	case "figcaption":
	case "caption":
	case "address":
	case "nav":
	case "article":
	case "noindex":
	case "iframe":
	case "form":
	case "section":
	case "dl":
	case "dt":
	case "dd":
	case "time":
	case "aside":
	    {
	    commitParagraph(nodes, runs);
	addExtraInfo(el);
	final org.luwrain.reader.Node[] nn = onNode(el);
	releaseExtraInfo();
	for(org.luwrain.reader.Node i: nn)
	    nodes.add(i);
	    }
	break;
	case "h1":
	case "h2":
	case "h3":
	case "h4":
	case "h5":
	case "h66":
	case "h7":
	case "h8":
	case "h9":
	    {
	    commitParagraph(nodes, runs);
	addExtraInfo(el);
	final NodeBuilder builder = new NodeBuilder();
	builder.addSubnodes(onNode(el));
		final org.luwrain.reader.Node n = builder.newSection(tagName.trim().charAt(1) - '0');
	n.extraInfo = getCurrentExtraInfo();
	releaseExtraInfo();
	nodes.add(n);
	    }
	break;
	case "ul":
	case "ol":
	case "li":
	case "table":
	case "th":
	case "tr":
	case "td":
	    {
	    commitParagraph(nodes, runs);
	addExtraInfo(el);
	final NodeBuilder builder = new NodeBuilder();
	builder.addSubnodes(onNode(el));
	final org.luwrain.reader.Node n = createNode(tagName, builder);
	n.extraInfo = getCurrentExtraInfo();
	releaseExtraInfo();
	nodes.add(n);
	    }
	break;
	case "img":
	case "a":
	case "tt":
	case "code":
	case "b":
	case "s":
	case "ins":
	case "em":
	case "i":
	case "u":
	case "big":
	case "small":
	case "strong":
	case "span":
	case "cite":
	case "font":
	case "sup":
	case "label":
	    addExtraInfo(el);
	onElementInPara(el, nodes, runs);
	releaseExtraInfo();
	break;
	default:
	    Log.warning(LOG_COMPONENT, "unprocessed tag:" + tagName);
	}
    }

    private void onTextNode(TextNode textNode, List<org.luwrain.reader.Run> runs)
    {
	NullCheck.notNull(textNode, "textNode");
	NullCheck.notNull(runs, "runs");
	final String text = textNode.text();
	if (text != null && !text.isEmpty())
	    runs.add(new org.luwrain.reader.TextRun(text, !hrefStack.isEmpty()?hrefStack.getLast():"", getCurrentExtraInfo()));
    }

    private void commitParagraph(List<org.luwrain.reader.Node> nodes, List<org.luwrain.reader.Run> runs)
    {
	NullCheck.notNull(nodes, "nodes");
	NullCheck.notNull(runs, "runs");
	if (runs.isEmpty())
	    return;
	final org.luwrain.reader.Paragraph p = NodeBuilder.newParagraph(runs.toArray(new org.luwrain.reader.Run[runs.size()]));
	p.extraInfo = getCurrentExtraInfo();
	nodes.add(p);
	runs.clear();
    }

    private org.luwrain.reader.Node createNode(String tagName, NodeBuilder builder)
    {
	NullCheck.notEmpty(tagName, "tagName");
	switch(tagName)
	{
	case "ul":
	    return builder.newUnorderedList();
	case "ol":
	    return builder.newOrderedList();
	case "li":
	    return builder.newListItem();
	case "table":
	    return builder.newTable();
	case "tr":
	    return builder.newTableRow();
	case "th":
	case "td":
	    return builder.newTableCell();
	default:
	    Log.warning(LOG_COMPONENT, "unable to create the node for tag \'" + tagName + "\'");
	    return null;
	}
    }
}

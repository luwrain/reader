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
import org.luwrain.reader.Run;

final class Builder extends Base implements org.luwrain.reader.DocumentBuilder
{
    static private final String DEFAULT_CHARSET = "UTF-8";

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
    NullCheck.notNull(text, "text");
    NullCheck.notNull(props, "props");
    final InputStream is = new ByteArrayInputStream(text.getBytes());
    try {
	try {
	    return buildDoc(is, props);
	}
	finally {
	    is.close();
	}
    }
    catch(IOException e)
    {
	Log.error(LOG_COMPONENT, "unable to read HTML from a string:" + e.getClass().getName() + ":" + e.getMessage());
	return null;
    }
    }

    @Override public org.luwrain.reader.Document buildDoc(InputStream is, Properties props) throws IOException
    {
	NullCheck.notNull(is, "is");
	NullCheck.notNull(props, "props");
		final String urlStr = props.getProperty("url");
	if (urlStr == null || urlStr.isEmpty())
throw new IOException("no \'url\' property");
	    this.docUrl = new URL(urlStr);
	final String charsetValue = props.getProperty("charset");
	final String charset;
		if (charsetValue != null && !charsetValue.isEmpty())
		    charset = charsetValue; else
		    charset = DEFAULT_CHARSET;
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
	nodeBuilder.addSubnodes(onNode(jsoupDoc.body(), false));
	final org.luwrain.reader.Document doc = new org.luwrain.reader.Document(jsoupDoc.title(), nodeBuilder.newRoot());
	doc.setHrefs(allHrefs.toArray(new String[allHrefs.size()]));
	return doc;
    }

    private org.luwrain.reader.Node[] onNode(org.jsoup.nodes.Node node, boolean preMode)
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
		onTextNode(textNode, resNodes, runs, preMode);
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
		    onElement((Element)n, resNodes, runs, preMode);
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

    private void onElementInPara(Element el, List<org.luwrain.reader.Node> nodes, List<Run> runs, boolean preMode)
    {
	NullCheck.notNull(el, "el");
	NullCheck.notNull(nodes, "nodes");
	NullCheck.notNull(runs, "runs");
	final String tagName;
	{
	    final String name = el.nodeName();
	    if (name == null || name.isEmpty())
		return;
	    tagName = name.trim().toLowerCase();
	}
	if (tagName.equals("img"))
	{
	    onImg(el, runs);
	    return;
	}
	final String href;
	if (tagName.equals("a"))
	    href = extractHref(el); else
	    href = null;
	if (href != null)
	    hrefStack.add(href);
	try {
	    final List<Node> nn = el.childNodes();
	    if (nn == null)
		return;
	    for(Node n: nn)
	    {
		if (n instanceof TextNode)
		{
		    onTextNode((TextNode)n, nodes, runs, preMode);
		    continue;
		}
		if (n instanceof Element)
		{
		    onElement((Element)n, nodes, runs, preMode);
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

    private void onElement(Element el, List<org.luwrain.reader.Node> nodes, List<Run> runs, boolean preMode)
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
	case "pre":
	    onPre(el, nodes, runs);
	    break;
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
	final org.luwrain.reader.Node[] nn = onNode(el, preMode);
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
	builder.addSubnodes(onNode(el, preMode));
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
	builder.addSubnodes(onNode(el, preMode));
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
	onElementInPara(el, nodes, runs, preMode);
	releaseExtraInfo();
	break;
	default:
	    Log.warning(LOG_COMPONENT, "unprocessed tag:" + tagName);
	}
    }

    private void onTextNode(TextNode textNode, List<org.luwrain.reader.Node> nodes, List<Run> runs, boolean preMode)
    {
	NullCheck.notNull(textNode, "textNode");
	NullCheck.notNull(nodes, "nodes");
	NullCheck.notNull(runs, "runs");
	final String text = textNode.text();
	if (text == null || text.isEmpty())
	    return;
	if (!preMode)
	{
	    runs.add(new org.luwrain.reader.TextRun(text, !hrefStack.isEmpty()?hrefStack.getLast():"", getCurrentExtraInfo()));
	    return;
	}
	final String[] lines = text.split("\n", -1);
	if (lines.length == 0)
	    return;
		    runs.add(new org.luwrain.reader.TextRun(lines[0], !hrefStack.isEmpty()?hrefStack.getLast():"", getCurrentExtraInfo()));
		    for(int i = 1;i < lines.length;i++)
		    {
			commitParagraph(nodes, runs);
					    runs.add(new org.luwrain.reader.TextRun(lines[i], !hrefStack.isEmpty()?hrefStack.getLast():"", getCurrentExtraInfo()));
		    }
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

    private void onImg(Element el, List<Run> runs)
    {
	 NullCheck.notNull(el, "el");
	 NullCheck.notNull(runs, "runs");
	 final String value = el.attr("alt");
	 if (value != null && !value.isEmpty())
	     runs.add(new org.luwrain.reader.TextRun("[" + value + "]", !hrefStack.isEmpty()?hrefStack.getLast():"", getCurrentExtraInfo()));
	 }

    private String extractHref(Element el)
    {
	final String value = el.attr("href");
	if (value == null)
	    return null;
	allHrefs.add(value);
	try {
	    return new URL(docUrl, value).toString();
	}
	catch(MalformedURLException e)
	{
	    return value;
	}
    }

    private void onPre(Element el, List<org.luwrain.reader.Node> nodes, List<Run> runs)
    {
	NullCheck.notNull(el, "el");
	NullCheck.notNull(nodes, "nodes");
	NullCheck.notNull(runs, "runs");
	commitParagraph(nodes, runs);
	addExtraInfo(el);
	try {
	    for(org.luwrain.reader.Node n: onNode(el, true))
		nodes.add(n);
	    commitParagraph(nodes, runs);
	}
	finally {
	    releaseExtraInfo();
	}
    }
}

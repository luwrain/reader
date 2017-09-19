
package org.luwrain.extensions.dthtml;

import java.util.*;
import java.io.*;
//import java.nio.charset.*;
import java.net.*;

import org.jsoup.*;
import org.jsoup.nodes.*;
import org.jsoup.select.*;

import org.luwrain.doctree.NodeFactory;
import org.luwrain.doctree.ExtraInfo;
import org.luwrain.core.NullCheck;
import org.luwrain.core.Log;

class Html
{
    private final Document jsoupDoc;
    private final URL docUrl;

    private final LinkedList<String> hrefStack = new LinkedList<String>();
    private final LinkedList<ExtraInfo> extraInfoStack = new LinkedList<ExtraInfo>();
    private final List<String> allHrefs = new LinkedList<String>();

    Html(InputStream stream, String charset, URL docUrl) throws IOException
    {
	NullCheck.notNull(stream, "stream");
	NullCheck.notNull(charset, "charset");
	NullCheck.notNull(docUrl, "docUrl");
	this.jsoupDoc = Jsoup.parse(stream, charset, docUrl.toString());
	this.docUrl = docUrl;
    }

org.luwrain.doctree.Document constructDocument()
    {
	final org.luwrain.doctree.Node res = NodeFactory.newNode(org.luwrain.doctree.Node.Type.ROOT);
	final Map<String, String> meta = new HashMap<String, String>();
	collectMeta(jsoupDoc.head(), meta);
	res.setSubnodes(onNode(jsoupDoc.body()));
	final org.luwrain.doctree.Document doc = new org.luwrain.doctree.Document(jsoupDoc.title(), res);
	doc.setProperty("url", docUrl.toString());
	doc.setHrefs(allHrefs.toArray(new String[allHrefs.size()]));
	return doc;
    }

    private void collectMeta(Element el, Map<String, String> meta)
    {
	if (el.nodeName().equals("meta"))
	{
	    final String name = el.attr("name");
	    final String content = el.attr("content");
	    if (name != null && !name.isEmpty() && content != null)
		meta.put(name, content);
	}
	if (el.childNodes() != null)
	    for(Node n: el.childNodes())
		if (n instanceof Element)
		    collectMeta((Element)n, meta);
    }

    private org.luwrain.doctree.Node[] onNode(org.jsoup.nodes.Node node)
    {
	NullCheck.notNull(node, "node");
	final LinkedList<org.luwrain.doctree.Node> resNodes = new LinkedList<org.luwrain.doctree.Node>();
	final LinkedList<org.luwrain.doctree.Run> runs = new LinkedList<org.luwrain.doctree.Run>();
	final List<Node> nodes = node.childNodes();
	for(Node n: nodes)
	{
	    final String name = n.nodeName();
	    if (n instanceof TextNode)
	    {
		final TextNode textNode = (TextNode)n;
		final String text = textNode.text();
		if (text != null && !text.isEmpty())
		    runs.add(new org.luwrain.doctree.TextRun(text, !hrefStack.isEmpty()?hrefStack.getLast():"", getCurrentExtraInfo()));
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
	}
	commitPara(resNodes, runs);
	return resNodes.toArray(new org.luwrain.doctree.Node[resNodes.size()]);
    }

    private void onElementInPara(Element el,
				 LinkedList<org.luwrain.doctree.Node> nodes, LinkedList<org.luwrain.doctree.Run> runs)
    {
	NullCheck.notNull(el, "el");
	final String tagName = el.nodeName();
	String href = null;
	//img
	if (tagName.toLowerCase().trim().equals("img"))
	{
	    final String value = el.attr("alt");
	    if (value != null && !value.isEmpty())
		runs.add(new org.luwrain.doctree.TextRun("[" + value + "]", !hrefStack.isEmpty()?hrefStack.getLast():"", getCurrentExtraInfo()));
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
		    e.printStackTrace();
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
		Log.warning("doctree-html", "encountering unexpected node of class " + n.getClass().getName());
	    }
	}
	finally
	{
	    if (href != null)
		hrefStack.pollLast();
	}
    }

    private void onElement(Element el,
			   LinkedList<org.luwrain.doctree.Node> nodes, LinkedList<org.luwrain.doctree.Run> runs)
    {
	final String name = el.nodeName();
	if (name == null || name.trim().isEmpty())
	    return;
	if (name.toLowerCase().trim().startsWith("g:") ||
	    name.toLowerCase().trim().startsWith("fb:"))
	    return;
	switch(name.toLowerCase().trim())
	{
	case "script":
	case "style":
	case "hr":
	case "input":
	case "button":
	case "nobr":
	case "wbr":
	case "map":
	    return;
	}
	org.luwrain.doctree.Node n = null;
	org.luwrain.doctree.Node[] nn = null;
	switch(name.toLowerCase().trim())
	{
	case "br":
	    commitPara(nodes, runs);
	    break;

	case "p":
	case "div":
	case "noscript":
	case "header":
	case "footer":
	case "center":
	case "blockquote":
	case "tbody":
	case "figure":
	case "figcaption":
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
	    commitPara(nodes, runs);
	addExtraInfo(el);
	nn = onNode(el);
	releaseExtraInfo();
	for(org.luwrain.doctree.Node i: nn)
	    nodes.add(i);
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
	    commitPara(nodes, runs);
	addExtraInfo(el);
	n = NodeFactory.newSection(name.trim().charAt(1) - '0');
	n.setSubnodes(onNode(el));
	n.extraInfo = getCurrentExtraInfo();
	releaseExtraInfo();
	nodes.add(n);
	break;

	case "ul":
	case "ol":
	case "li":
	case "table":
	case "th":
	case "tr":
	case "td":
	    commitPara(nodes, runs);
	addExtraInfo(el);
	n = NodeFactory.newNode(getNodeType(name));
	n.setSubnodes(onNode(el));
	n.extraInfo = getCurrentExtraInfo();
	releaseExtraInfo();
	nodes.add(n);
	break;

	case "img":
	case "a":
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
	    Log.warning("doctree-html", "unprocessed tag:" + name);
	}
    }

    private void onTextNode(TextNode textNode, LinkedList<org.luwrain.doctree.Run> runs)
    {
	final String text = textNode.text();
	if (text != null && !text.isEmpty())
	    runs.add(new org.luwrain.doctree.TextRun(text, !hrefStack.isEmpty()?hrefStack.getLast():"", getCurrentExtraInfo()));
    }

    private void commitPara(LinkedList<org.luwrain.doctree.Node> nodes, LinkedList<org.luwrain.doctree.Run> runs)
    {
	if (runs.isEmpty())
	    return;
	final org.luwrain.doctree.Paragraph para = NodeFactory.newPara();
	para.runs = runs.toArray(new org.luwrain.doctree.Run[runs.size()]);
	para.extraInfo = getCurrentExtraInfo();
	nodes.add(para);
	runs.clear();
    }

    private org.luwrain.doctree.Node.Type getNodeType(String tagName)
    {
	NullCheck.notEmpty(tagName, "tagName");
	switch(tagName)
	{
	case "ul":
	    return org.luwrain.doctree.Node.Type.UNORDERED_LIST;
	case "ol":
	    return org.luwrain.doctree.Node.Type.ORDERED_LIST;
	case "li":
	    return org.luwrain.doctree.Node.Type.LIST_ITEM;
	case "table":
	    return org.luwrain.doctree.Node.Type.TABLE;
	case "tr":
	    return org.luwrain.doctree.Node.Type.TABLE_ROW;
	case "th":
	case "td":
	    return org.luwrain.doctree.Node.Type.TABLE_CELL;
	default:
	    Log.warning("doctree-html", "unable to create the node for tag \'" + tagName + "\'");
	    return null;
	}
    }

    private void addExtraInfo(Element el)
    {
	NullCheck.notNull(el, "el");
	final ExtraInfo info = new ExtraInfo();
	info.name = el.nodeName();
	final Attributes attrs = el.attributes();
	if (attrs != null)
	    for(Attribute a: attrs.asList())
	    {
		final String key = a.getKey();
		final String value = a.getValue();
		if (key != null && !key.isEmpty() && value != null)
		    info.attrs.put(key, value);
	    }
	if (!extraInfoStack.isEmpty())
	    info.parent = extraInfoStack.getLast(); else
	    info.parent = null;
	extraInfoStack.add(info);
    }

    private void releaseExtraInfo()
    {
	if (!extraInfoStack.isEmpty())
	    extraInfoStack.pollLast();
    }

    private ExtraInfo getCurrentExtraInfo()
    {
	return extraInfoStack.isEmpty()?null:extraInfoStack.getLast();
    }
}

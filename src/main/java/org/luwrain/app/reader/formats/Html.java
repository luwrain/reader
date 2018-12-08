
package org.luwrain.app.reader.formats;

import java.util.*;
import java.io.*;
import java.nio.file.*;
import java.nio.charset.*;
import java.net.*;

import org.jsoup.*;
import org.jsoup.nodes.*;
import org.jsoup.select.*;

import org.luwrain.reader.NodeFactory;
import org.luwrain.reader.ExtraInfo;
import org.luwrain.core.NullCheck;
import org.luwrain.core.Log;

public class Html
{
    private final Document jsoupDoc;
    private final URL docUrl;

    private final LinkedList<String> hrefStack = new LinkedList<String>();
    private final LinkedList<ExtraInfo> extraInfoStack = new LinkedList<ExtraInfo>();
    private final LinkedList<String> allHrefs = new LinkedList<String>();

    public Html(Path path, String charset,
		URL docUrl) throws IOException
    {
	NullCheck.notNull(path, "path");
	NullCheck.notNull(charset, "charset");
	NullCheck.notNull(docUrl, "docUrl");
	Log.debug("doctree-html", "reading " + path.toString() + " with charset " + charset);
	jsoupDoc = Jsoup.parse(Files.newInputStream(path), charset, path.toString());
	this.docUrl = docUrl;
    }

    public Html(String text, /*String charset,*/
		URL docUrl)
    {
	NullCheck.notNull(text, "text");
	//	NullCheck.notNull(charset, "charset");
	NullCheck.notNull(docUrl, "docUrl");
	jsoupDoc = Jsoup.parse(text);
	this.docUrl = docUrl;
    }


    public Html(InputStream is, String charset,
		URL docUrl) throws IOException
    {
	NullCheck.notNull(is, "is");
	NullCheck.notNull(charset, "charset");
	Log.debug("doctree-html", "reading input stream with charset " + charset);
	jsoupDoc = Jsoup.parse(is, charset, docUrl.toString());
	this.docUrl = docUrl;
    }

    public org.luwrain.reader.Document constructDocument()
    {
	final org.luwrain.reader.Node res = NodeFactory.newNode(org.luwrain.reader.Node.Type.ROOT);
	final HashMap<String, String> meta = new HashMap<String, String>();
	collectMeta(jsoupDoc.head(), meta);
	res.setSubnodes(onNode(jsoupDoc.body()));
	final org.luwrain.reader.Document doc = new org.luwrain.reader.Document(jsoupDoc.title(), res);
	doc.setProperty("url", docUrl.toString());
	doc.setHrefs(allHrefs.toArray(new String[allHrefs.size()]));
	return doc;
    }

    private void collectMeta(Element el, HashMap<String, String> meta)
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

    private org.luwrain.reader.Node[] onNode(org.jsoup.nodes.Node node)
    {
	NullCheck.notNull(node, "node");
	final LinkedList<org.luwrain.reader.Node> resNodes = new LinkedList<org.luwrain.reader.Node>();
	final LinkedList<org.luwrain.reader.Run> runs = new LinkedList<org.luwrain.reader.Run>();
	final List<Node> nodes = node.childNodes();
	for(Node n: nodes)
	{
	    final String name = n.nodeName();
	    if (n instanceof TextNode)
	    {
		final TextNode textNode = (TextNode)n;
		final String text = textNode.text();
		if (text != null && !text.isEmpty())
		    runs.add(new org.luwrain.reader.TextRun(text, !hrefStack.isEmpty()?hrefStack.getLast():"", getCurrentExtraInfo()));
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
	return resNodes.toArray(new org.luwrain.reader.Node[resNodes.size()]);
    }

    private void onElementInPara(Element el,
				 LinkedList<org.luwrain.reader.Node> nodes, LinkedList<org.luwrain.reader.Run> runs)
    {
	NullCheck.notNull(el, "el");
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
			   LinkedList<org.luwrain.reader.Node> nodes, LinkedList<org.luwrain.reader.Run> runs)
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
	org.luwrain.reader.Node n = null;
	org.luwrain.reader.Node[] nn = null;
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
	for(org.luwrain.reader.Node i: nn)
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

    private void onTextNode(TextNode textNode, LinkedList<org.luwrain.reader.Run> runs)
    {
	final String text = textNode.text();
	if (text != null && !text.isEmpty())
	    runs.add(new org.luwrain.reader.TextRun(text, !hrefStack.isEmpty()?hrefStack.getLast():"", getCurrentExtraInfo()));
    }

    private void commitPara(LinkedList<org.luwrain.reader.Node> nodes, LinkedList<org.luwrain.reader.Run> runs)
    {
	if (runs.isEmpty())
	    return;
	final org.luwrain.reader.Paragraph para = NodeFactory.newPara();
	para.runs = runs.toArray(new org.luwrain.reader.Run[runs.size()]);
	para.extraInfo = getCurrentExtraInfo();
	nodes.add(para);
	runs.clear();
    }

    private org.luwrain.reader.Node.Type getNodeType(String tagName)
    {
	NullCheck.notEmpty(tagName, "tagName");
	switch(tagName)
	{
	case "ul":
	    return org.luwrain.reader.Node.Type.UNORDERED_LIST;
	case "ol":
	    return org.luwrain.reader.Node.Type.ORDERED_LIST;
	case "li":
	    return org.luwrain.reader.Node.Type.LIST_ITEM;
	case "table":
	    return org.luwrain.reader.Node.Type.TABLE;
	case "tr":
	    return org.luwrain.reader.Node.Type.TABLE_ROW;
	case "th":
	case "td":
	    return org.luwrain.reader.Node.Type.TABLE_CELL;
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

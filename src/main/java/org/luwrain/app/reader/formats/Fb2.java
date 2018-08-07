
package org.luwrain.app.reader.formats;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.function.Consumer;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;

import org.luwrain.core.Log;
import org.luwrain.core.NullCheck;
import org.luwrain.doctree.*;

public class Fb2
{
	    private Document jdoc = null;

    public Fb2(Path path,String charset) throws IOException
    {
	NullCheck.notNull(path, "path");
	NullCheck.notNull(charset, "charset");
	InputStream is = null;
	try {
	    is = Files.newInputStream(path);
	jdoc = Jsoup.parse(is, charset, "", Parser.xmlParser());
	}
	finally {
	    if (is != null)
		is.close();
	};
    }

    public Fb2(InputStream is,String charset) throws IOException
    {
	NullCheck.notNull(is, "is");
	NullCheck.notNull(charset, "charset");
	jdoc = Jsoup.parse(is, charset, "", Parser.xmlParser());
    }


    public org.luwrain.doctree.Document createDoc()
    {
	try {
	    final Node root = NodeFactory.newNode(Node.Type.ROOT);
	    final LinkedList<Node> subnodes = new LinkedList<Node>();
	    final Elements descr=jdoc.select("FictionBook > description");
	    if(!descr.isEmpty())
	    {

		// title
		Elements title=descr.first().getElementsByTag("book-title");
		if(!title.isEmpty())
		{
		    final Node h1=NodeFactory.newSection(1);
		    h1.setSubnodes(new Node[]{NodeFactory.newPara(title.first().text())});
		    subnodes.add(h1);
		}

		// genre
		/*
		final Elements genre=descr.first().getElementsByTag("genre");
		if(!genre.isEmpty())
		{
		    String str="";
		    for(org.jsoup.nodes.Element e:genre) str+=" "+e.text();
		    subnodes.add(NodeFactory.newPara(str));
		}
		*/

		// author, each per new line
		final Elements author=descr.first().getElementsByTag("author");
		if(!author.isEmpty())
		{
		    for(org.jsoup.nodes.Element e:author)
		    {
			String str="";
			for(org.jsoup.nodes.Element i:e.children())
			    if(i.hasText())
				str+=" "+i.text();
			subnodes.add(NodeFactory.newPara(str));
		    }
		}

		// annotation, as usual text
		final Elements annotation=descr.first().getElementsByTag("annotation");
		if(!annotation.isEmpty())
		{
		    for(org.jsoup.nodes.Element e:annotation)
			complexContent(subnodes,e);
		}
	    }
	    Elements body=jdoc.select("FictionBook > body");
	    if(!body.isEmpty())
	    {
		body.forEach((org.jsoup.nodes.Element e)->
			     { // enumeraty body esctions
				 if(e.hasAttr("name"))
				 { // body name as h2
				     Node h2=NodeFactory.newSection(2);
				     h2.setSubnodes(new Node[]{NodeFactory.newPara(e.attr("name"))});
				     subnodes.add(h2);
				 }
				 complexContent(subnodes,e);
			     });
	    }
	    root.setSubnodes ( subnodes.toArray(new Node[subnodes.size()]));
	    return new org.luwrain.doctree.Document(root);
	} catch (Exception e)
	{
	    e.printStackTrace();
	    return null;
	}
    }
    
    private void complexContent(LinkedList<Node> subnodes,org.jsoup.nodes.Element element)
    {
    	for(org.jsoup.nodes.Element e:element.children())
    	{
	    switch(e.tagName())
	    {
	    case "title":
	    case "section":
	    case "epigraph":
	    case "subtitle":
		Node h2=NodeFactory.newSection(3);
	    final LinkedList<Node> sn = new LinkedList<Node>();
	    complexContent(sn,e);
	    h2.setSubnodes(sn.toArray(new Node[sn.size()]));
	    subnodes.add(h2);
	    break;
	    case "empty-line":
		subnodes.add(NodeFactory.newPara(" "));
		break;
	    case "p":
		subnodes.add(NodeFactory.newPara(paraContent(e)));
		break;
	    case "binary":
	    case "image":
		break;
	    default:
		break;
	    }
    	}
    }

    private String paraContent(org.jsoup.nodes.Element element)
    {
    	String text="";
    	ListIterator<org.jsoup.nodes.Node> list=element.childNodes().listIterator();
    	while(list.hasNext())
    	{
	    org.jsoup.nodes.Node n=list.next();
	    switch(n.nodeName())
	    {
	    case "#text":
		text+=((org.jsoup.nodes.TextNode)n).text();
		break;
	    case "strong":
	    case "emphasis":
	    case "style":
	    case "strikethrough":
	    case "sub":
	    case "sup":
	    case "code":
		text+=paraContent((org.jsoup.nodes.Element)n);
	    break;
	    case "date":
		if(n.hasAttr("value"))
		    text+=n.attr("value");
		else
		    text+=((org.jsoup.nodes.TextNode)n).text();
		break;
	    case "a":
		break;
	    case "image":
		break;
	    default:
		break;
	    }
    	}
    	return text;
    }
}


package org.luwrain.app.opds;

import java.util.*;
import java.io.IOException;
import java.net.*;

import org.jsoup.*;
import org.jsoup.nodes.*;
import org.jsoup.select.Elements;

import org.luwrain.core.*;

/**
 * OPDS (Open Publication Distribution System) parser. This class
 * contains a number of utilities to fetch and parse data provided by
 * OPDS resources (usually digital libraries). Basically, it just reads
 * XML and saves necessary data in corresponding classes for further
 * using in client applications.
 */
final class Opds
{
    final static private int BUFFER_SIZE=32*1024;

    static public class Link
    {
	public final String url;
	public final String rel;
	public final String type;
	public final String profile;

	Link(String url, String rel,
	     String type, String profile)
	{
	    NullCheck.notNull(url, "url");
	    this.url = url;
	    this.rel = rel;
	    this.type = type;
	    this.profile = profile;
	}

	@Override public String toString()
	{
	    return "url=" + url + ",rel=" + rel + ",type=" + type;
	}
    }

static public class Author
    {
	public final String name;

	Author(String name)
	{
	    NullCheck.notNull(name, "name");
	    this.name = name;
	    //	    Log.debug("opds", "name:" + name);
	}

	@Override public String toString()
	{
	    return name;
	}
    }

    static public class Entry 
    {
	public final String id;
	public final URL parentUrl;
	public final String title;
	public final Link[] links;
	public final Author[] authors;

	Entry(String id, URL parentUrl,
	      String title, Link[] links,
Author[] authors)
	{
	    NullCheck.notNull(id, "id");
	    NullCheck.notNull(parentUrl, "parentUrl");
	    NullCheck.notNull(title, "title");
	    NullCheck.notNullItems(links, "links");
	    NullCheck.notNullItems(authors, "authors");
	    this.id = id;
	    this.parentUrl = parentUrl;;
	    this.title = title;
	    this.links = links != null?links:new Link[0];
	    this.authors = authors;
	}

	@Override public String toString()
	{
	    return title;
	}
    }

    static public class Result
    {
	public enum Errors {FETCHING_PROBLEM, OK};

	private final Entry[] entries;
	private final Errors error;
	// file link if result is not a directory entry and mime type of it
	//	private String filename;
	//	private String mime;

	Result(Errors error)
	{
	    NullCheck.notNull(error, "error");
	    this.error = error;
	    this.entries = null;
	}

	Result(Entry[] entries)
	{
	    NullCheck.notNullItems(entries, "entries");
	    this.error = Errors.OK;
	    this.entries = entries;
    	}

	public Entry[] getEntries()
	{
	    return entries;
	}

	public Errors getError(){return error;}

	public boolean hasEntries()
	{
	    return error == Errors.OK && entries != null;
	}
    }

    static public Result fetch(URL url)
    {
	NullCheck.notNull(url, "url");
	final LinkedList<Entry> res = new LinkedList<Entry>();
	final org.jsoup.nodes.Document doc;
	try {
	    final Connection con=Jsoup.connect(url.toString());
	    con.userAgent(org.luwrain.doctree.loading.UrlLoader.USER_AGENT);
	    con.timeout(30000);
	    doc = con.get();
	}
	catch(IOException e)
	{
	    Log.error("doctree-opds", "unable to fetch " + url.toString() + ":" + e.getClass().getName() + ":" + e.getMessage());
	    e.printStackTrace(); 
	    return new Result(Result.Errors.FETCHING_PROBLEM);
	}
	for(org.jsoup.nodes.Element node:doc.getElementsByTag("entry"))
	    try {
		final Entry entry = parseEntry(url, node);
		    res.add(entry);
	    }
	    catch (Exception e)
	    {
		Log.warning("doctree-opds", "reading " + url.toString() + ":" + e.getClass().getName() + ":" + e.getMessage());
		e.printStackTrace();
	    }
	return new Result(res.toArray(new Entry[res.size()]));
    }

    static private Entry parseEntry(URL parentUrl, Element el) throws Exception
    {
	NullCheck.notNull(el, "el");
	String id = "";
	String title = "";
	final LinkedList<Link> links = new LinkedList<Link>();
	final LinkedList<Author> authors = new LinkedList<Author>();
	for(Element node:el.getElementsByTag("title"))
	    title = node.text();
	for(Element node:el.getElementsByTag("id"))
	    id = node.text();
	for(Element node:el.getElementsByTag("link"))
	    links.add(new Link(node.attributes().get("href"),
			       node.attributes().get("rel"),
			       node.attributes().get("type"),
			       node.attributes().get("profile")));
	for(Element node:el.getElementsByTag("author"))
	{
	    String name = null;
	for(Element nameNode:el.getElementsByTag("name"))
	    name = nameNode.text();
	if (name != null)
	    authors.add(new Author(name));
	}
			if (id == null)
			    id = "---";
			if (title == null)
			    title = "---";
			return new Entry(id, parentUrl, title, links.toArray(new Link[links.size()]), authors.toArray(new Author[authors.size()]));
    }
}

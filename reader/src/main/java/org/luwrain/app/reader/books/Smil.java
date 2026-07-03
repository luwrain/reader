// SPDX-License-Identifier: BUSL-1.1
// Copyright 2012-2026 Michael Pozhidaev <msp@luwrain.org>

package org.luwrain.app.reader.books;

import java.util.*;
import java.util.regex.*;
import java.net.*;
import java.io.*;

import org.jsoup.*;
import org.jsoup.nodes.*;
import org.jsoup.select.*;
import org.jsoup.parser.*;

import org.luwrain.core.*;

import static java.util.Objects.*;

final class Smil
{
    static private final String LOG_COMPONENT = "smil";

    static class Entry
    {
	public enum Type {
	    SEQ,
	    PAR,
	    AUDIO,
	    TEXT,
	    FILE};

	final Type type;
	final String id;
	final Entry[] entries;
	private String src = null;
	private final AudioFragment audioInfo;

	Entry(Type type)
	{
	    requireNonNull(type, "type can't be null");
	    this.type = type;
	    this.id = "";
	    this.audioInfo = null;
	    this.entries = new Entry[0];
	}

	Entry(Type type, Entry[] entries)
	{
	    requireNonNull(type, "type can't be null");
	    NullCheck.notNullItems(entries, "entries");
	    this.type = type;
	    this.id = "";
	    this.audioInfo = null;
	    this.entries = entries;
	}

	Entry(Type type, String id, Entry[] entries)
	{
	    requireNonNull(type, "type can't be null");
	    requireNonNull(id, "id can't be null");
	    NullCheck.notNullItems(entries, "entries");
	    this.type = type;
	    this.id = id;
	    this.audioInfo = null;
	    this.entries = entries;
	}

	Entry (Type type, String id, String src)
	{
	    requireNonNull(type, "type can't be null");
	    requireNonNull(id, "id can't be null");
	    requireNonNull(src, "src can't be null");
	    this.type = type;
	    this.id = id;
	    this.src = src;
	    this.audioInfo = null;
	    this.entries = new Entry[0];
	}

	Entry (String id, String src, AudioFragment audioInfo)
	{
	    requireNonNull(id, "id can't be null");
	    requireNonNull(src, "src can't be null");
	    requireNonNull(audioInfo, "audioInfo can't be null");
	    this.type = Type.AUDIO;
	    this.id = id;
	    this.src = src;
	    this.audioInfo = audioInfo;
	    this.entries = new Entry[0];
	}

	void saveTextSrc(List<String> res)
	{
	    if (type == Type.TEXT &&
		src != null && !src.isEmpty())
		res.add(src);
	    if (entries != null)
		for(Entry e: entries)
		    e.saveTextSrc(res);
	}

	void allSrcToUrls(URL base) throws MalformedURLException
	{
	    requireNonNull(base, "base can't be null");
	    if (src != null && !src.isEmpty())
		src = new URL(base, src).toString();
	    if (entries != null)
		for(Entry e: entries)
		    e.allSrcToUrls(base);
	}

	Entry findById(String id)
	{
	    requireNonNull(id, "id can't be null");
	    if (this.id != null && this.id.equals(id))
		return this;
	    if (entries == null)
		return null;
	    for(Entry e: entries)
	    {
		final Entry res = e.findById(id);
		if (res != null)
		    return res;
	    }
	    return null;
	}

	AudioFragment getAudioFragment()
	{
	    return audioInfo;
	}

	String src()
	{
	    return src;
	}
    }

    static final class File extends Entry
    {
	File()
	{
	    super(Type.FILE);
	}
    }

    static public Entry fromUrl(URL url)
    {
	requireNonNull(url, "url can't be null");
	final org.jsoup.nodes.Document doc;
	try {
	    if (!url.getProtocol().equals("file"))
	    {
		final Connection con=Jsoup.connect(url.toString());
		con.userAgent(org.luwrain.util.Connections .DEFAULT_USER_AGENT);
		con.timeout(30000);
		doc = con.get();
	    } else
		doc = Jsoup.parse(url.openStream(), "utf-8", "", Parser.xmlParser());
	}
	catch(Exception e)
	{
	    Log.error(LOG_COMPONENT, "unable to fetch SMIL from URL " + url.toString() + ":" + e.getClass().getName() + ":" + e.getMessage());
	    return null;
	}
	return new Entry(Entry.Type.FILE, onNode(doc.body()));
    }

    static Entry fromFile(java.io.File file)
    {
	requireNonNull(file, "file can't be null");
	final org.jsoup.nodes.Document doc;
	try {
	    doc = Jsoup.parse(new FileInputStream(file), "utf-8", "", Parser.xmlParser());
	}
	catch(Exception e)
	{
	    Log.error(LOG_COMPONENT, "unable to parse " + file.getAbsolutePath() + ":" + e.getClass().getName() + ":" + e.getMessage());
	    return null;
	}
	return new Entry(Entry.Type.FILE, onNode(doc.body()));
    }

    static private Entry[] onNode(Node node)
    {
	requireNonNull(node, "node can't be null");
	final LinkedList<Entry> res = new LinkedList<Entry>();
	final List<Node> childNodes = node.childNodes();
	for(Node n: childNodes)
	{
	    final String name = n.nodeName();
	    if (n instanceof TextNode)
	    {
		final TextNode textNode = (TextNode)n;
		final String text = textNode.text();
		if (!text.trim().isEmpty())
		    Log.warning("smil", "unexpected text content:" + text);
		continue;
	    }
	    if (n instanceof Element)
	    {
		final Element el = (Element)n;
		switch(name.trim().toLowerCase())
		{
		case "seq":
		    res.add(new Entry(Entry.Type.SEQ, el.attr("id"), onNode(el)));
		    break;
		case "par":
		    res.add(new Entry(Entry.Type.PAR, el.attr("id"), onNode(el)));
		    break;
		case "audio":
		    res.add(onAudio(el));
		    break;
		case "text":
		    res.add(onText(el));
		    break;
		default:
		    Log.warning("smil", "unknown tag:" + name);
		}
		continue;
	    }
	}
	return res.toArray(new Entry[res.size()]);
    }

    static private Entry onAudio(Element el)
    {
	requireNonNull(el, "el can't be null");
	final String id = el.attr("id");
	final String src = el.attr("src");
	final String beginValue = el.attr("clip-begin");
	final String endValue = el.attr("clip-end");
	long beginPos = -1, endPos = -1;
	if (beginValue != null)
	    beginPos = parseTime(beginValue);
	if (endValue != null)
	    endPos = parseTime(endValue);
	return new Entry(id, src, new AudioFragment(src, beginPos, endPos));
    }

    static private Entry onText(Element el)
    {
	requireNonNull(el, "el can't be null");
	final String id = el.attr("id");
	final String src = el.attr("src");
	return new Entry(Entry.Type.TEXT, id, src);
    }

    static private final Pattern TIME_PATTERN = Pattern.compile("^npt=(?<sec>\\d+.\\d+)s$");
    static private long parseTime(String value)
    {
	final Matcher m = TIME_PATTERN.matcher(value);
	if(m.matches())
	{
	    try {
		float f = Float.parseFloat(m.group("sec"));
		f *= 1000;
		return new Float(f).longValue();
	    }
	    catch(NumberFormatException e)
	    {
		e.printStackTrace();
	    }
	}
	return -1;
    }
}

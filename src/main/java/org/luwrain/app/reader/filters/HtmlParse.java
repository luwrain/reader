
package org.luwrain.app.reader.filters;

import java.util.*;

import org.luwrain.util.*;
import org.luwrain.app.reader.doctree.*;

class HtmlParse implements MlReaderListener, MlReaderConfig
{
    private class Level
    {
	public Node node;
	public LinkedList<Node> subnodes = new LinkedList<Node>();

	public Level(Node node)
	{
	    this.node = node;
	    if (node == null)
		throw new NullPointerException("node may not be null");
	}
    }

    final String[] nonClosingTags = new String[]{
	"!doctype",
	"input",
	"br",
	"link",
	"img",
	"meta"
    }; 

    public LinkedList<Level> levels = new LinkedList<Level>();
    private LinkedList<Run> runs = new LinkedList<Run>();
    private String title;

    public HtmlParse()
    {
	levels.add(new Level(new Node(Node.ROOT)));
    }

    @Override public void onMlTagOpen(String tagName, Map<String, String> attrs)
    {
	switch (tagName)
	{
	case "span":
	case "sup":
	case "a":
	case "font":
	case "b":
	case "i":
	    return;
	case "br":
	    runs.add(new Run(" "));
	    return;
	case "table":
	    startLevel(Node.TABLE);
	    return;
	case "tr":
	    startLevel(Node.TABLE_ROW);
	    return;
	case "th":
	    startLevel(Node.TABLE_CELL);
	    return;
	case "ul":
	    startLevel(Node.UNORDERED_LIST);
	    return;
	case "ol":
	    startLevel(Node.ORDERED_LIST);
	    return;
	case "li":
	    startLevel(Node.LIST_ITEM);
	    return;
	default:
	    commitPara();
	    System.out.println("html:unhandled tag:" + tagName);
	}
    }

    @Override public void onMlTagClose(String tagName)
    {
	switch(tagName)
	{
	case "span":
	case "a":
	case "sup":
	case "font":
	case "b":
	case "i":
	    return;
	case "th":
	case 	    "tr":
	case "table":
	case "ol":
	case "ul":
	case "li":
	    commitLevel();
	return;
	default:
	    commitPara();
	    return;
	}
    }

    @Override public void onMlText(String text, LinkedList<String> tagsStack)
    {
	if (tagsStack.contains("script") ||
	    tagsStack.contains("style"))
	    return;
	if (!tagsStack.isEmpty() && tagsStack.getLast().equals("title"))
	{
	    title = text.trim();
	    return;
	}
	if (text == null || text.isEmpty())
	    return;
	String prepared = text.replaceAll("\n", " ");
	if (runs.isEmpty())
	{
	    int firstNonSpace = 0;
	    while (firstNonSpace < prepared.length() && Character.isSpace(prepared.charAt(firstNonSpace)))
		++firstNonSpace;
	    if (firstNonSpace >= prepared.length())
		return;
	    prepared = prepared.substring(firstNonSpace);
	}
	runs.add(new Run(text));
    }

    @Override public boolean mlTagMustBeClosed(String tag)
    {
	final String adjusted = tag.toLowerCase().trim();
	for(String s: nonClosingTags)
	    if (s.equals(adjusted))
		return false;
	return true;
    }

    @Override public boolean mlAdmissibleTag(String tagName)
    {
	for(int i = 0;i < tagName.length();++i)
	{
	    final char c = tagName.charAt(i);
	    if (!Character.isLetter(c) && !Character.isDigit(c))
		return false;
	}
	return true;
    }

    public Node constructRoot()
    {
	final Level firstLevel = levels.getFirst();
	firstLevel.node.subnodes = firstLevel.subnodes.toArray(new Node[firstLevel.subnodes.size()]);
	return firstLevel.node;
    }

    public String getTitle()
    {
	return title != null?title:"";
    }

    private void startLevel(int type)
    {
	commitPara();
	final Level lastLevel = levels.getLast();
	final Node node = constructNode(type);
	lastLevel.subnodes.add(node);
	levels.add(new Level(node));
    }

    private void commitLevel()
    {
	commitPara();
	final Level lastLevel = levels.pollLast();
	lastLevel.node.subnodes = lastLevel.subnodes.toArray(new Node[lastLevel.subnodes.size()]);
    }

    private Node constructNode(int type)
    {
	if (type == Node.TABLE)
	    return new Table();
	return new Node(type);
    }

    private void commitPara()
    {
	if (runs.isEmpty())
	    return;
	final Paragraph para = new Paragraph();
	para.runs = runs.toArray(new Run[runs.size()]);
	runs.clear();
	final int lastLevelType = levels.getLast().node.type;
	if (lastLevelType == Node.TABLE ||
	    lastLevelType == Node.TABLE_ROW ||
	    lastLevelType == Node.ORDERED_LIST ||
	    lastLevelType == Node.UNORDERED_LIST)
	{
	    //	    System.out.println("reader:warning:ignoring to put a paragraph into a level with inappropriate type " + lastLevelType);
	    return;
	}
	levels.getLast().subnodes.add(para);
    }
}

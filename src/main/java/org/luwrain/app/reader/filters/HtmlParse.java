
package org.luwrain.app.reader.filters;

import java.util.*;

import org.luwrain.app.reader.doctree.*;
import org.luwrain.util.MlTagStrip;

class HtmlParse extends MlTagStrip
{
class Level
{
    public int type;
    public LinkedList<Node> subnodes = new LinkedList<Node>();

    public Level(int type)
    {
	this.type = type;
    }
}

final String[] nonClosingTags = new String[]
{
    "br",
"meta"
}; 

    public LinkedList<Level> levels = new LinkedList<Level>();
    private LinkedList<Run> runs = new LinkedList<Run>();
    private LinkedList<String> tagsStack = new LinkedList<String>();

    public HtmlParse(String text)
    {
	super(text);
	levels.add(new Level(Node.ROOT));
    }

    @Override protected void onOpeningTag(String name)
    {
	final String adjustedName = name.trim().toLowerCase();
	if (!isNonClosingTag(adjustedName))
	    tagsStack.add(adjustedName);

	if (adjustedName.equals("span") ||
	    adjustedName.equals("font") ||
	    adjustedName.equals("b") ||
	    adjustedName.equals("i"))
	    return;
	if (adjustedName.equals("br"))
	{
	    runs.add(new Run(" "));
	    return;
	}
	int type;
	if (adjustedName.equals("table"))
	    type = Node.TABLE; else
	    if (adjustedName.equals("tr"))
		type = Node.TABLE_ROW; else
		if (adjustedName.equals("td"))
		    type = Node.TABLE_CELL; else
		    if (adjustedName.equals("ul"))
			type = Node.UNORDERED_LIST; else
			if (adjustedName.equals("ol"))
			    type = Node.ORDERED_LIST; else
			    if (adjustedName.equals("li"))
				type = Node.LIST_ITEM; else
			    {
				commitPara();
				return;
			    }
	commitPara();
	levels.add(new Level(type)); 
    }

    @Override protected void onClosingTag(String name)
    {
	final String adjustedName = name.trim().toLowerCase();
	final String lastTag = tagsStack.pollLast();
	if (!lastTag.equals(adjustedName))
	    System.out.println("reader:warning:expecting the closing tag to be \'" + lastTag + "\' but it is \'" + adjustedName + "\'");

	if (adjustedName.equals("span") ||
	    adjustedName.equals("font") ||
	    adjustedName.equals("b") ||
	    adjustedName.equals("i"))
	    return;

	int type;
	if (adjustedName.equals("td"))
	    type = Node.TABLE_CELL; else
	    if (adjustedName.equals("tr"))
		type = Node.TABLE_ROW; else
		if (adjustedName.equals("table"))
		    type = Node.TABLE; else
		    if (adjustedName.equals("ol"))
			type = Node.ORDERED_LIST; else
			if (adjustedName.equals("ul"))
			    type = Node.UNORDERED_LIST; else
			    if (adjustedName.equals("li"))
				type = Node.LIST_ITEM; else
			    {
				commitPara();
				return;
			    }
	commitLevel(type);
    }

    @Override protected void onEntity(String name)
    {
    }

    @Override protected void onText(String str)
    {
	if (str == null || str.isEmpty())
	    return;
	if (isTagOpened("head"))
	    return; 
	String text = str;
	if (runs.isEmpty())
	{
	    int firstNonSpace = 0;
	    while (firstNonSpace < text.length() && Character.isSpace(text.charAt(firstNonSpace)))
		++firstNonSpace;
	    if (firstNonSpace >= text.length())
		return;
	    text = text.substring(firstNonSpace);
	}
	    runs.add(new Run(str));
    }

    @Override protected void onCdata(String value)
    {
    }

    private void commitLevel(int type)
    {
	if (levels.isEmpty())
	    System.out.println("reader:warning:trying to commit subnodes with an empty levels stack");
	commitPara();
	final Level lastLevel = levels.pollLast();
	if (type != lastLevel.type)
	    System.out.println("reader:warning:expecting the last level on committing to be " + type + " but it is " + lastLevel.type);
	final Node node = new Node(lastLevel.type, lastLevel.subnodes.toArray(new Node[lastLevel.subnodes.size()]));
	levels.getLast().subnodes.add(node);
    }

    private void commitPara()
    {
	if (runs.isEmpty())
	    return;
	final Paragraph para = new Paragraph();
	para.runs = runs.toArray(new Run[runs.size()]);
	runs.clear();
	final int lastLevelType = levels.getLast().type;
	if (lastLevelType == Node.TABLE ||
lastLevelType == Node.TABLE_ROW ||
lastLevelType == Node.ORDERED_LIST ||
lastLevelType == Node.UNORDERED_LIST)
	{
	    System.out.println("reader:warning:ignoring to put a paragraph into a level with inappropriate type " + lastLevelType);
	    return;
	}
	levels.getLast().subnodes.add(para);
    }

    public Node constructRoot()
    {
	if (levels.size() > 1)
	    System.out.println("reader:warning:constructing a root node but there are " + levels.size() + " levels");
	final Level firstLevel = levels.getFirst();
	final Node[] subnodes = firstLevel.subnodes.toArray(new Node[firstLevel.subnodes.size()]);
	return new Node(Node.ROOT, subnodes);
    }

    private String getCurrentTag()
    {
	if (tagsStack == null || tagsStack.isEmpty())
	    return null;
	return tagsStack.getLast();
    }

    private boolean isTagOpened(String tag)
    {
	final String adjusted = tag.toLowerCase().trim();
	for(String s: tagsStack)
	    if (s.equals(adjusted))
		return true;
	return false;
    }

    private boolean isNonClosingTag(String tag)
    {
	final String adjusted = tag.toLowerCase().trim();
	for(String s: nonClosingTags)
	    if (s.equals(adjusted))
		return true;
	return false;
    }
}

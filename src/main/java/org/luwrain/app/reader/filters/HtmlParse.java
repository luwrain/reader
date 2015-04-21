
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

    public LinkedList<Level> levels = new LinkedList<Level>();
    private LinkedList<Run> runs = new LinkedList<Run>();

    public HtmlParse(String text)
    {
	super(text);
	levels.add(new Level(Node.ROOT));
    }

    protected void onOpeningTag(String name)
    {
	final String adjustedName = name.trim().toLowerCase();
	if (adjustedName.equals("span"))
	    return;
	if (adjustedName.equals("table"))
	{
	    commitPara();
	    levels.add(new Level(Node.TABLE)); 
	    return;
	}

	if (adjustedName.equals("tr"))
	{
	    commitPara();
	    levels.add(new Level(Node.TABLE_ROW)); 
	    return;
	}

	if (adjustedName.equals("td"))
	{
	    commitPara();
	    levels.add(new Level(Node.TABLE_CELL)); 
	    return;
	}

	commitPara();
    }

    @Override protected void onClosingTag(String name)
    {
	final String adjustedName = name.trim().toLowerCase();
	if (adjustedName.equals("span"))
	    return;
	if (adjustedName.equals("td") ||
	    adjustedName.equals("tr") ||
	    adjustedName.equals("table"))
	{
	    commitLevel();
	    return;
	}
	commitPara();
    }

    @Override protected void onEntity(String name)
    {
    }

    @Override protected void onText(String str)
    {
	if (str == null || str.isEmpty())
	    return;
	    runs.add(new Run(str));
    }

    @Override protected void onCdata(String value)
    {
    }

    private void commitLevel()
    {
	commitPara();
	final Level lastLevel = levels.pollLast();
	final Node node = new Node(lastLevel.type, lastLevel.subnodes.toArray(new Node[lastLevel.subnodes.size()]));
	levels.getLast().subnodes.add(node);
    }

    private void commitPara()
    {
	if (runs.isEmpty())
	    return;
	Paragraph para = new Paragraph();
	para.runs = runs.toArray(new Run[runs.size()]);
	runs.clear();
	levels.getLast().subnodes.add(para);
    }

    public Node constructRoot()
    {
	final Level firstLevel = levels.getFirst();
	final Node[] subnodes = firstLevel.subnodes.toArray(new Node[firstLevel.subnodes.size()]);
	return new Node(Node.ROOT, subnodes);
    }
}


package org.luwrain.app.reader.filters;

import java.util.*;

import org.luwrain.core.*;
import org.luwrain.doctree.*;

public class GDotCom implements Visitor
{
    @Override public void visitNode(Node node)
    {
	NullCheck.notNull(node, "node");
	if (node.getType() != Node.Type.ORDERED_LIST)
	    return;
	ExtraInfo info = node.extraInfo;
	while (info != null)
	{
	    if (info.getName().equals("ol") && info.getAttr("id").equals("gbzc"))
		node.setImportance(-1);  else
		if (info.getName().equals("ol") && info.getAttr("id").equals("gbmm"))
		    node.setImportance(-1);  else
		    if (info.getName().equals("ol") && info.getAttr("class").equals("gbtc"))
			node.setImportance(-1);  /*else
			if (info.getName().equals("ol"))
			{
			    Log.debug("filter", info.name);
			    if (info.attrs != null)
				for(Map.Entry<String, String> e: info.attrs.entrySet())
				    Log.debug("filter", e.getKey() + "=" + e.getValue());
				    }*/
	    info = info.parent;
	}
	//	Log.debug("filter", "---");
    }

    @Override public void visit(ListItem node)
    {
	NullCheck.notNull(node, "node");
	Log.debug("item", node.getCompleteText());
    }

    @Override public void visit(Paragraph para)
    {
	NullCheck.notNull(para, "para");
	if (para.getCompleteText().equals("‎"))
	{
	    Log.debug("para", "found!");
	    para.setImportance(-1);
	}
    }

    @Override public void visit(Section node)
    {
	NullCheck.notNull(node, "node");
	if (node.getCompleteText().toLowerCase().matches("account options"))
	    node.setImportance(-1);
    }

    @Override public void visit(TableCell node)
    {
	NullCheck.notNull(node, "node");
	if (!hasSection(node))
	{
	    node.setImportance(-1);
	    return;
	}

	/*
	    final String sectText = getFirstSectionText(node);
	    if (sectText != null)
		Log.debug("text", sectText);
	    if (sectText == null || sectText.toLowerCase().matches("search options"))
	    {
		node.setImportance(-1);
		return;
	    }

	*/
	Log.debug("filter", "text \"" + node.getCompleteText() + "\"");
	ExtraInfo info = node.extraInfo;
	while (info != null)
	{



	    if (info.getName().equals("th"))
		node.setImportance(-1); else
		/*
		if (info.getName().equals("td") && info.getAttr("class").equals("sfbgg"))
		node.setImportance(-1); else
		*/

		if (info.getName().equals("td") && info.getAttr("id").equals("leftnav"))
		node.setImportance(-1);/* else


		if (info.getName().equals("td") && info.getAttr("class").equals("lst-td tia"))
		node.setImportance(-1); else
		if (info.getName().equals("td") && info.getAttr("class").equals("lst-td"))
		node.setImportance(-1); else
		    if (info.getName().equals("td"))
		{
	    Log.debug("filter", info.name);
	    if (info.attrs != null)
		for(Map.Entry<String, String> e: info.attrs.entrySet())
		    Log.debug("filter", e.getKey() + "=" + e.getValue());
		}
				       */
	    info = info.parent;
	    }
	//	Log.debug("filter", "---");
    }

    @Override public void visit(Table node)
    {
    }

    @Override public void visit(TableRow node)
    {
    }

    static private boolean hasSection(Node node)
    {
	NullCheck.notNull(node, "node");
	if (node instanceof Section)
	    return true;
	for(Node n: node.getSubnodes() )
	    if (hasSection(n))
		return true;
	return false;
    }

    static private String getFirstSectionText(Node node)
    {
	NullCheck.notNull(node, "node");
	if (node instanceof Section)
	    return node.getCompleteText();
	for(Node n: node.getSubnodes() )
	{
	    final String res = getFirstSectionText(n);
	    if (res != null)
		return res;
	}
	return null;
    }
}

/*
   Copyright 2012-2018 Michael Pozhidaev <michael.pozhidaev@gmail.com>

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

package org.luwrain.app.reader.filters;

import java.util.*;

import org.luwrain.core.*;
import org.luwrain.reader.*;

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
		node.setImportance(-1); else
		if (info.getName().equals("ol") && info.getAttr("id").equals("gbmm"))
		    node.setImportance(-1); else
		    if (info.getName().equals("ol") && info.getAttr("class").equals("gbtc"))
			node.setImportance(-1);  
	    info = info.parent;
	}
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
	ExtraInfo info = node.extraInfo;
	while (info != null)
	{
	    if (info.getName().equals("th"))
		node.setImportance(-1); else
		if (info.getName().equals("td") && info.getAttr("id").equals("leftnav"))
		    node.setImportance(-1);
	    info = info.parent;
	}
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
}

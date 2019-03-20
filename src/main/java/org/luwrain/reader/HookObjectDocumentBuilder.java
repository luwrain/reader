/*
   Copyright 2012-2019 Michael Pozhidaev <michael.pozhidaev@gmail.com>
   Copyright 2015-2016 Roman Volovodov <gr.rPman@gmail.com>

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

//LWR_API 1.0

package org.luwrain.reader;

import java.util.*;

import org.luwrain.core.*;
import org.luwrain.script.*;

public final class HookObjectDocumentBuilder
{
    Document build(Object hookObj)
    {
	NullCheck.notNull(hookObj, "hookObj");
	final Object nodesObj = ScriptUtils.getMember(hookObj, "nodes");
	if (nodesObj == null)
	    return null;
	final List nodes = ScriptUtils.getArray(nodesObj);
	if (nodes == null)
	    return null;
	final NodeBuilder nodeBuilder = new NodeBuilder();
	for(Object nodeObj: nodes)
	{
	}
	return new Document(nodeBuilder.newRoot());
    }

    private Node onNode(Object nodeObj)
    {
	NullCheck.notNull(nodeObj, "nodeObj");
	final String type = ScriptUtils.getStringValue("type");
	if (type == null || type.isEmpty())
	    return null;
	if (type.equals("paragraph"))
	    return onParagraph(nodeObj);
	final Object nodesObj = ScriptUtils.getMember(nodeObj, "nodes");
	if (nodesObj == null)
	    return null;
	final List nodes = ScriptUtils.getArray(nodesObj);
	if (nodes == null)
	    return null;
	final NodeBuilder nodeBuilder = new NodeBuilder();
	for(Object subnodeObj: nodes)
	{
	    if (subnodeObj == null)
		return null;
	    final Node node = onNode(subnodeObj);
	    if (node == null)
		continue;
	    nodeBuilder.addSubnode(node);
	}
	switch(type)
	{
	case "section":
	    return nodeBuilder.newSection(1);
	default:
	    return null;
	}
    }

    private Paragraph onParagraph(Object paraObj)
    {
	NullCheck.notNull(paraObj, "paraObj");
	final Object runsObj = ScriptUtils.getMember(paraObj, "runs");
	if (runsObj == null)
	    return null;
	final List runs = ScriptUtils.getArray(runsObj);
	if (runs == null)
	    return null;
	final List<Run> res = new LinkedList();
	for(Object runObj: runs)
	{
	    if (runObj == null)
		continue;
	    final Run run = onRun(runObj);
	    if (run != null)
		res.add(run);
	}
	return new Paragraph(res.toArray(new Run[res.size()]));
    }

    private Run onRun(Object runObj)
    {
	NullCheck.notNull(runObj, "runObj");
	final Object textObj = ScriptUtils.getMember(runObj, "text");
	if (textObj == null)
	    return null;
	final String text = ScriptUtils.getStringValue(textObj);
	if (text == null || text.isEmpty())
	    return null;
	final Object hrefObj = ScriptUtils.getMember(runObj, "href");
	if (hrefObj == null)
	    return new TextRun(text);
	final String href = ScriptUtils.getStringValue(hrefObj);
	if (href == null || href.isEmpty())
	    return new TextRun(text);
	return new TextRun(text, href);
    }
}

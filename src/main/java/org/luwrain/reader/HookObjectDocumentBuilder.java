
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
	    final Node node = onNode(subnodeObj);
	    if (node == null)
		continue;
	    nodeBuilder.addSubnode(node);
	}
	return null;
    }


    private Paragraph onParagraph(Object paraObj)
    {
	NullCheck.notNull(paraObj, "paraObj");
	return null;
    }
}

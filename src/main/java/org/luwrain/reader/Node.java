/*
   Copyright 2012-2016 Michael Pozhidaev <michael.pozhidaev@gmail.com>
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

package org.luwrain.reader;

import java.util.*;

import org.luwrain.core.*;

public class Node extends org.luwrain.reader.view.NodeBase
{
    public enum Type {
	ROOT, SECTION, PARAGRAPH,
	TABLE, TABLE_ROW, TABLE_CELL,
	UNORDERED_LIST, ORDERED_LIST, LIST_ITEM,
    };

    static public final int IMPORTANCE_REGULAR = 0;

    protected Type type;
    public ExtraInfo extraInfo = null;
    protected int importance = IMPORTANCE_REGULAR;
    protected Node[] subnodes = new Node[0];
    Node parentNode;
    protected final TitleRun titleRun = new TitleRun(this.getClass().getName());
    protected Object associatedObject = null;

    /** The exact meaning of a level depends on the node type*/
    int level = 0;

    protected boolean empty = false;

	Node(Type type)
    {
	this.type = type;
    }

    public final Node[] getSubnodes()
    {
	return subnodes != null?subnodes:new Node[0];
    }

    public final int getSubnodeCount()
    {
	return subnodes != null?subnodes.length:0;
    }

    public final boolean noSubnodes()
    {
	return subnodes == null || subnodes.length < 1;
    }

    void preprocess()
    {
	if (subnodes == null)
	    subnodes = new Node[0];
	if (type == Type.ROOT)
	{
	    parentNode = null;
	    if (subnodes.length != 0 && !(subnodes[subnodes.length - 1] instanceof EmptyLine))
	    {
		subnodes = Arrays.copyOf(subnodes, subnodes.length + 1);
		subnodes[subnodes.length - 1] = new EmptyLine();
	    }
	}
	if (titleRun != null)
	    titleRun.setParentNode(this);
	for(Node n: subnodes)
	{
	    n.parentNode = this;
	    n.preprocess();
	}
	if (type == Type.ORDERED_LIST || type == Type.UNORDERED_LIST)
	    arrangeListItems();
    }

void setEmptyMark()
    {
	empty = true;
	if (importance < 0)
	    return;
	if (subnodes == null || subnodes.length < 1)
	    return;
	for(Node n:subnodes)
	{
	    n.setEmptyMark();
	    if (!n.empty)
		empty = false;
	}
    }

    //Must return the number of deleted subnodes
    int prune()
    {
	if (subnodes == null)
	    return 0;
	int k = 0;
	for(int i = 0;i < subnodes.length;++i)
	    if (subnodes[i].empty)
		++k; else
		subnodes[i - k] = subnodes[i];
	if (k > 0)
	    subnodes = Arrays.copyOf(subnodes, subnodes.length - k);
	for(Node n: subnodes)
	    k += n.prune();
	return k;
    }

    @Override public String toString()
    {
	if (type == null)
	    return "";
	return type.toString() + " \"" + getCompleteText() + "\"";
    }

    public final boolean hasNodeInAllParents(Node toCheck)
    {
	NullCheck.notNull(toCheck, "toCheck");
		Node p = getParentNode();
	while(p != null)
	{
	    if (p == toCheck)
		return true;
	    p = p.getParentNode();
	}
	return false;
    }

    public final boolean isInTable()
    {
	Node p = getParentNode();
	while(p != null)
	{
	    if (p instanceof Table)
		return true;
	    p = p.getParentNode();
	}
	return false;
    }

    public String getCompleteText()
    {
	if (subnodes == null)
	    return "";
	final StringBuilder b = new StringBuilder();
	boolean first = true;
	for(Node n: subnodes)
	{
	    final String value = n.getCompleteText();
	    if (value.isEmpty())
		continue;
	    if (!first)
		b.append(" ");
	    first = false;
	    b.append(value);
	}
	return new String(b);
    }

    /** 
     * @return -1 if there is no a parent node or there is a consistency error
     */
    public Node.Type getParentType()
    {
	return parentNode != null && parentNode.subnodes != null?parentNode.type:null;
    }

    /** @return -1 if there is no a parent node or there is a consistency error*/
    int getParentSubnodeCount()
    {
	return parentNode != null && parentNode.subnodes != null?parentNode.subnodes.length:-1;
    }

    /** @return -1 if it is impossible to understand;*/
    public int getIndexInParentSubnodes()
    {
	if (parentNode == null || parentNode.subnodes == null)
	    return -1;
	for(int i = 0;i < parentNode.subnodes.length;++i)
	    if (parentNode.subnodes[i] == this)
		return i;
	return -1;
    }

    public Node getParentNode()
    {
	return parentNode;
    }

    public TitleRun getTitleRun()
    {
	return titleRun;
    }

    public Type getType()
    {
	return type;
    }

    public void setSubnodes(Node[] subnodes)
    {
	NullCheck.notNullItems(subnodes, "subnodes");
	this.subnodes = subnodes;
    }

    public int getImportance()
    {
	return importance;
    }

    public void setImportance(int importance)
    {
	this.importance = importance;
    }

    public boolean hasNonParagraphs()
    {
	if (subnodes == null)
	    return false;
	for(Node n: subnodes)
	    if (!(n instanceof Paragraph))
		return true;
	return false;
    }

    public void addSubnode(Node subnode)
    {
	NullCheck.notNull(subnode, "subnode");
	if (subnodes == null)
	{
	    subnodes = new Node[]{subnode};
	    return;
	}
	subnodes = Arrays.copyOf(subnodes, subnodes.length + 1);
	subnodes[subnodes.length - 1] = subnode;
    }

    protected void arrangeListItems()
    {
	NullCheck.notNullItems(subnodes, "subnodes");
	final LinkedList<Node> nodes = new LinkedList<Node>();
	final LinkedList<Node> nextNodes = new LinkedList<Node>();
	for(int i = 0;i < subnodes.length;++i)
	{
	    final Node n = subnodes[i];
	    if (!(subnodes[i] instanceof ListItem))
	    {
		Log.warning("doctree", "subnode of type " + subnodes[i].type + " found inside of a list" );
		nextNodes.add(n);
		continue;
	    }
	    if (!nextNodes.isEmpty())
	    {
		final Node item = NodeFactory.newNode(Type.LIST_ITEM);
		item.setSubnodes(nextNodes.toArray(new Node[nextNodes.size()]));
		nextNodes.clear();
		item.parentNode = this;
		for(Node k: item.getSubnodes())
		    k.parentNode = item;
		nodes.add(item);
	    }
	    nodes.add(n);
	}
	if (!nextNodes.isEmpty())
	{
	    final Node item = NodeFactory.newNode(Type.LIST_ITEM);
	    item.setSubnodes(nextNodes.toArray(new Node[nextNodes.size()]));
	    nextNodes.clear();
	    item.parentNode = this;
	    for(Node k: item.getSubnodes())
		k.parentNode = item;
	    nodes.add(item);
	}
	subnodes = nodes.toArray(new Node[nodes.size()]);
	for(Node n: subnodes)
	    n.preprocess();
    }

    public Object getAssociatedObject()
    {
	return associatedObject;
    }

    public void setAssociatedObject(Object associatedObject)
    {
	this.associatedObject = associatedObject;
    }
}

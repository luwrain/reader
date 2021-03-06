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

public final class NodeBuilder
{
    private final List<Node> nodes = new LinkedList();

    public NodeBuilder addSubnode(Node node)
    {
	NullCheck.notNull(node, "node");
	nodes.add(node);
	return this;
    }

    public void addSubnodes(List<Node> nodes)
    {
	NullCheck.notNull(nodes, "nodes");
	this.nodes.addAll(nodes);
    }

    public NodeBuilder addSubnodes(Node[] nodes)
    {
	NullCheck.notNullItems(nodes, "nodes");
	for(Node n: nodes)
	    this.nodes.add(n);
	return this;
    }

    public Paragraph addParagraph()
    {
	final Paragraph para = new Paragraph();
	nodes.add(para);
	return para;
    }

        public Paragraph addParagraph(String text)
    {
	NullCheck.notNull(text, "text");
	final Paragraph p;
	if (text.isEmpty())
	    	p = new EmptyLine(); else
	    p = new Paragraph(new Run[]{new TextRun(text)});
	nodes.add(p);
	return p;
    }

    public Paragraph addEmptyLine()
    {
	return addParagraph("");
    }

    static public Paragraph newParagraph(Run[] runs)
    {
	NullCheck.notNullItems(runs, "runs");
	final Paragraph p = new Paragraph();
	p.setRuns(runs);
	return p;
    }

        static public Paragraph newParagraph(String text)
    {
	NullCheck.notNull(text, "text");
	final Paragraph p;
	if (text.isEmpty())
	    p = new EmptyLine(); else
	    p = new Paragraph(new Run[]{new TextRun(text)});
	return p;
    }
    
    public Node newRoot()
    {
	final Node  node = new Node(Node.Type.ROOT);
	node.setSubnodes(nodes.toArray(new Node[nodes.size()]));
	return node;
    }

    public Table newTable()
    {
	final Table table = new Table();
	table.setSubnodes(nodes.toArray(new Node[nodes.size()]));
	return table;
    }

    public TableRow newTableRow()
    {
	final TableRow tableRow = new TableRow();
	tableRow.setSubnodes(nodes.toArray(new Node[nodes.size()]));
	return tableRow;
    }

        public TableCell newTableCell()
    {
	final TableCell tableCell = new TableCell();
	tableCell.setSubnodes(nodes.toArray(new Node[nodes.size()]));
	return tableCell;
    }


    public ListItem newListItem()
    {
	final ListItem listItem = new ListItem();
	listItem.setSubnodes(nodes.toArray(new Node[nodes.size()]));
	return listItem;
    }

    public Node newOrderedList()
    {
	final Node orderedList = new Node(Node.Type.ORDERED_LIST);
	orderedList.setSubnodes(nodes.toArray(new Node[nodes.size()]));
	return orderedList;
    }

    public Node newUnorderedList()
    {
	final Node unorderedList = new Node(Node.Type.UNORDERED_LIST);
	unorderedList.setSubnodes(nodes.toArray(new Node[nodes.size()]));
	return unorderedList;
    }

    public Section newSection(int level)
    {
	if (level < 1)
	    throw new IllegalArgumentException("level (" + level + ") may not be less than 1");
	final Section sect = new Section(level);
	sect.setSubnodes(nodes.toArray(new Node[nodes.size()]));
	return sect;
    }
}

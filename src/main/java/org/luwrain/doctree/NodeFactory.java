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

package org.luwrain.doctree;

public class NodeFactory
{
    static public Node newNode(Node.Type type)
    {
	switch(type)
	{
	case PARAGRAPH:
	    throw new IllegalArgumentException("doctree.Paragraph may not be created through NodeFactory.newNode(), use NodeFactory.newPara() instead");
	case SECTION:
	    throw new IllegalArgumentException("doctree.Section may not be created through NodeFactory.newNode(), use NodeFactory.newSection() instead");
	case TABLE:
	    return new Table();
	case TABLE_ROW:
	    return new TableRow();
	case TABLE_CELL:
	    return new TableCell();
	case LIST_ITEM:
	    return new ListItem();
	default:
	return new Node(type);
	}
    }

    static public Node newSection(int level)
    {
	return new Section(level);
    }

    static public Paragraph newPara()
    {
	return new Paragraph();
    }

    static public Paragraph newPara(String text)
    {
	final Paragraph para = newPara();
	para.runs = new Run[]{new TextRun(text)};
	return para;
    }

    static public EmptyLine newEmptyLine()
    {
	return new EmptyLine();
    }
}

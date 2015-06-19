/*
   Copyright 2012-2015 Michael Pozhidaev <michael.pozhidaev@gmail.com>

   This file is part of the Luwrain.

   Luwrain is free software; you can redistribute it and/or
   modify it under the terms of the GNU General Public
   License as published by the Free Software Foundation; either
   version 3 of the License, or (at your option) any later version.

   Luwrain is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
   General Public License for more details.
*/

package org.luwrain.app.reader.doctree;

import org.junit.*;

public class NodeGeomTest extends Assert
{
    private Node testRoot;

    @Before public void createTestRoot()
    {
	testRoot = new Node(Node.ROOT);
	Paragraph para = new Paragraph();
	para.runs = new Run[]{
		new Run("First testing run"),
		    new Run("Second testing run")
	    };
	//	para.setParentOfRuns();

	Node table1 = new Node(Node.TABLE);
	table1.subnodes = new Node[]{new Node(Node.TABLE_ROW), new Node(Node.TABLE_ROW)};
	table1.subnodes[0].subnodes = new Node[]{new Node(Node.TABLE_CELL), new Node(Node.TABLE_CELL)};
	table1.subnodes[1].subnodes = new Node[]{new Node(Node.TABLE_CELL), new Node(Node.TABLE_CELL)};
	table1.subnodes[0].subnodes[0].subnodes = new Node[]{new Paragraph(new Run("1"))};
	table1.subnodes[0].subnodes[1].subnodes = new Node[]{new Paragraph(new Run("2"))};
	table1.subnodes[1].subnodes[0].subnodes = new Node[]{new Paragraph(new Run("3"))};
	table1.subnodes[1].subnodes[1].subnodes = new Node[]{new Paragraph(new Run("4"))};

	testRoot.subnodes = new Node[]{para, table1};
    }

    @Test public void normalWidth()
    {
	final int WIDTH = 20;
	testRoot.calcWidth(WIDTH);
	assertTrue(testRoot.width == WIDTH);
	assertTrue(testRoot .subnodes != null);
	assertTrue(testRoot.subnodes.length == 2);
	assertTrue(testRoot.subnodes[0].width == WIDTH);
	assertTrue(testRoot.subnodes[1].width == WIDTH);
    }

    @Test public void insufficientWidth()
    {
	final int WIDTH = 10;
	testRoot.calcWidth(WIDTH);
	assertTrue(testRoot.width == 17);
	assertTrue(testRoot .subnodes != null);
	assertTrue(testRoot.subnodes.length == 2);
	assertTrue(testRoot.subnodes[0].width == WIDTH);
	assertTrue(testRoot.subnodes[1].width == 17);
	assertTrue(testRoot.subnodes[1].subnodes[0].width == 17);
	assertTrue(testRoot.subnodes[1].subnodes[1].width == 17);
	assertTrue(testRoot.subnodes[1].subnodes[0].subnodes[0].width == 8);
	assertTrue(testRoot.subnodes[1].subnodes[0].subnodes[1].width == 8);
	assertTrue(testRoot.subnodes[1].subnodes[1].subnodes[0].width == 8);
	assertTrue(testRoot.subnodes[1].subnodes[1].subnodes[1].width == 8);
    }

    @Test public void height()
    {
	final int WIDTH = 20;
	testRoot.calcWidth(WIDTH);
	RowPartsBuilder partsBuilder = new RowPartsBuilder();
	partsBuilder.onNode(testRoot);
	RowPart[] parts = partsBuilder.parts();
	/*
	for(RowPart p: parts)
	    p.run.parentParagraph.containsRow(p.rowNum);
	*/
	testRoot.calcHeight();
	assertTrue(testRoot.height == 7);
assertTrue(testRoot.subnodes[0].height == 3);
	//Whole table;
assertTrue(testRoot.subnodes[1].height == 4);
	//Rows;
assertTrue(testRoot.subnodes[1].subnodes[0].height == 2);
assertTrue(testRoot.subnodes[1].subnodes[1].height == 2);
    }

    @Test public void position()
    {
	final int WIDTH = 20;
	testRoot.calcWidth(WIDTH);
	RowPartsBuilder partsBuilder = new RowPartsBuilder();
	partsBuilder.onNode(testRoot);
	RowPart[] parts = partsBuilder.parts();
	/*
	for(RowPart p: parts)
	    p.run.parentParagraph.containsRow(p.rowNum);
	*/
	testRoot.calcHeight();
	testRoot.calcPosition();
	assertTrue(testRoot.x == 0);
	assertTrue(testRoot.y == 0);
	assertTrue(testRoot.subnodes[0].x == 0);
	assertTrue(testRoot.subnodes[0].y == 0);
	assertTrue(testRoot.subnodes[1].x == 0);
	assertTrue(testRoot.subnodes[1].y == 3);
	final Node table = testRoot.subnodes[1];
	//Rows;
	assertTrue(table.subnodes[0].x == 0);
	assertTrue(table.subnodes[0].y == 3);
	assertTrue(table.subnodes[1].x == 0);
	assertTrue(table.subnodes[1].y == 5);
	//Cells;
		assertTrue(table.subnodes[0].subnodes[0].x == 0);
		assertTrue(table.subnodes[0].subnodes[0].y == 3);
		assertTrue(table.subnodes[0].subnodes[1].x == 10);
		assertTrue(table.subnodes[0].subnodes[1].y == 3);
		assertTrue(table.subnodes[1].subnodes[0].x == 0);
		assertTrue(table.subnodes[1].subnodes[0].y == 5);
		assertTrue(table.subnodes[1].subnodes[1].x == 10);
		assertTrue(table.subnodes[1].subnodes[1].y == 5);
    }
}

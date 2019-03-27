
package org.luwrain.reader;

import org.junit.*;

public class RowPartsBuilderTest extends Assert
{
    private Node testRoot;

    @Before public void createTestRoot()
    {
	/*
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
	*/
    }

    @Ignore @Test public void runsSingle()
    {
	/*
	RowPartsBuilder builder = new RowPartsBuilder();
	builder.onRun(new Run("abc abc abc"), 16);
	RowPart[] parts = builder.parts();
	assertTrue(parts != null);
	assertTrue(parts.length == 1);
	assertTrue(parts[0].text().equals("abc abc abc"));
	assertTrue(parts[0].posFrom == 0);
	assertTrue(parts[0].posTo== 11);
	*/
    }

    @Ignore @Test public void runsDouble()
    {
	/*
	RowPartsBuilder builder = new RowPartsBuilder();
	builder.onRun(new Run("abc abc abc "), 16);
	builder.onRun(new Run("abc abc abc"), 16);
	RowPart[] parts = builder.parts();
	assertTrue(parts != null);
	assertTrue(parts.length == 3);
	//	assertTrue(parts[0].rowNum == 0);
	assertTrue(parts[0].text().equals("abc abc abc "));
	//	assertTrue(parts[1].rowNum == 0);
	assertTrue(parts[1].text().equals("abc"));
	//	assertTrue(parts[2].rowNum == 1);
	assertTrue(parts[2].text().equals("abc abc"));
	//Checking a space in the second node;
	builder = new RowPartsBuilder();
	builder.onRun(new Run("abc abc abc"), 16);
	builder.onRun(new Run(" abc abc abc"), 16);
	parts = builder.parts();
	assertTrue(parts != null);
	assertTrue(parts.length == 3);
	//	assertTrue(parts[0].rowNum == 0);
	assertTrue(parts[0].text().equals("abc abc abc"));
	//	assertTrue(parts[1].rowNum == 0);
	assertTrue(parts[1].text().equals(" abc"));
	//	assertTrue(parts[2].rowNum == 1);
	assertTrue(parts[2].text().equals("abc abc"));
	*/
    }

    @Ignore @Test public void runsNonSpacesBreak()
    {
	/*
	RowPartsBuilder builder = new RowPartsBuilder();
	builder.onRun(new Run("123456789"), 5);
	RowPart[] parts = builder.parts();
	assertTrue(parts != null);
	assertTrue(parts.length == 2);
	//	assertTrue(parts[0].rowNum == 0);
	assertTrue(parts[0].text().equals("12345"));
	//	assertTrue(parts[1].rowNum == 1);
	assertTrue(parts[1].text().equals("6789"));
	//Spaces only;
	builder = new RowPartsBuilder();
	builder.onRun(new Run("       "), 5);
	parts = builder.parts();
	assertTrue(parts != null);
	assertTrue(parts.length == 2);
	//	assertTrue(parts[0].rowNum == 0);
	assertTrue(parts[0].text().equals("     "));
	//	assertTrue(parts[1].rowNum == 1);
	assertTrue(parts[1].text().equals("  "));
	*/
    }

    @Ignore @Test public void runsInsufficientForFirstWord()
    {
	/*
	RowPartsBuilder builder = new RowPartsBuilder();
	builder.onRun(new Run("abc def 9"), 10);
	builder.onRun(new Run("abcdef bb 12345"), 10);
	RowPart[] parts = builder.parts();
	assertTrue(parts != null);
	assertTrue(parts.length == 3);
	//	assertTrue(parts[0].rowNum == 0);
	//	assertTrue(parts[1].rowNum == 1);
	//	assertTrue(parts[2].rowNum == 2);
	assertTrue(parts[0].text().equals("abc def 9"));
	assertTrue(parts[1].text().equals("abcdef bb"));
	assertTrue(parts[2].text().equals("12345"));
	*/
    }

    @Ignore @Test public void nodes()
    {
	/*
	final int WIDTH = 24;
	testRoot.calcWidth(WIDTH);
	RowPartsBuilder builder = new RowPartsBuilder();
	builder.onNode(testRoot);
	RowPart[] parts = builder.parts();
	assertTrue(parts != null);
	assertTrue(parts.length == 7);
	//	assertTrue(parts[0].rowNum == 0);
	//	assertTrue(parts[1].rowNum == 0);
	//	assertTrue(parts[2].rowNum == 1);
	//	assertTrue(parts[3].rowNum == 3);
	//	assertTrue(parts[4].rowNum == 5);
	//	assertTrue(parts[5].rowNum == 7);
	//	assertTrue(parts[6].rowNum == 9);
	*/
    }
}
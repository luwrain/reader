
package org.luwrain.app.reader.doctree;

import org.junit.*;

public class LinePartsBuilderTest extends Assert
{
    @Test public void runsSingle()
    {
	LinePartsBuilder builder = new LinePartsBuilder(16);
	builder.onRun(new Node(Node.RUN, "abc abc abc"));
	LinePart[] parts = builder.parts();
	assertTrue(parts != null);
	assertTrue(parts.length == 1);
	assertTrue(parts[0].text().equals("abc abc abc"));
	assertTrue(parts[0].posFrom == 0);
	assertTrue(parts[0].posTo== 11);
    }

    @Test public void runsDouble()
    {
	LinePartsBuilder builder = new LinePartsBuilder(16);
	builder.onRun(new Node(Node.RUN, "abc abc abc "));
	builder.onRun(new Node(Node.RUN, "abc abc abc"));
	LinePart[] parts = builder.parts();
	assertTrue(parts != null);
	assertTrue(parts.length == 3);
	assertTrue(parts[0].lineNum == 0);
	assertTrue(parts[0].text().equals("abc abc abc "));
	assertTrue(parts[1].lineNum == 0);
	assertTrue(parts[1].text().equals("abc"));
	assertTrue(parts[2].lineNum == 1);
	assertTrue(parts[2].text().equals("abc abc"));
	//Checking a space in the second node;
	builder = new LinePartsBuilder(16);
	builder.onRun(new Node(Node.RUN, "abc abc abc"));
	builder.onRun(new Node(Node.RUN, " abc abc abc"));
	parts = builder.parts();
	assertTrue(parts != null);
	assertTrue(parts.length == 3);
	assertTrue(parts[0].lineNum == 0);
	assertTrue(parts[0].text().equals("abc abc abc"));
	assertTrue(parts[1].lineNum == 0);
	assertTrue(parts[1].text().equals(" abc"));
	assertTrue(parts[2].lineNum == 1);
	assertTrue(parts[2].text().equals("abc abc"));
    }

    @Test public void runsNonSpacesBreak()
    {
	LinePartsBuilder builder = new LinePartsBuilder(5);
	builder.onRun(new Node(Node.RUN, "123456789"));
	LinePart[] parts = builder.parts();
	assertTrue(parts != null);
	assertTrue(parts.length == 2);
	assertTrue(parts[0].lineNum == 0);
	assertTrue(parts[0].text().equals("12345"));
	assertTrue(parts[1].lineNum == 1);
	assertTrue(parts[1].text().equals("6789"));
	//Spaces only;
	builder = new LinePartsBuilder(5);
	builder.onRun(new Node(Node.RUN, "       "));
	parts = builder.parts();
	assertTrue(parts != null);
	assertTrue(parts.length == 2);
	assertTrue(parts[0].lineNum == 0);
	assertTrue(parts[0].text().equals("     "));
	assertTrue(parts[1].lineNum == 1);
	assertTrue(parts[1].text().equals("  "));
    }
}

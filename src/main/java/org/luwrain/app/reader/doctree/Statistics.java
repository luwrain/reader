
package org.luwrain.app.reader.doctree;

public class Statistics
{
    public int numNodes = 0;
    public int numParagraphs = 0;
    public int numRuns = 0;

    public void print()
    {
	System.out.println("Nodes: " + numNodes + "(" + numParagraphs + " paragraphs)");
	System.out.println("Runs: " + numRuns);
    }
}

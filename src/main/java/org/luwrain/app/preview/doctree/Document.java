
package org.luwrain.app.preview.doctree;

public class Document 
{
    private Node root;
    public LinePart[] lineParts;

    public Document(Node root)
    {
	this.root = root;
	if (root == null)
	    throw new NullPointerException("root may not be null");
    }

    public void constructLineParts()
    {

    }
}


package org.luwrain.app.preview.doctree;

public class Node
{
    public final static int ROOT = 1;
    public final static int SECTION = 2;
    public final static int PARAGRAPH = 3;
    public final static int RUN = 4;

    private int type;
    private Node[] subnodes;
    private String body = "";
    private TextAttr attr = new TextAttr();
    private String href = "";

    public String text()
    {
	return text;
    }

    public TextAttr textAttr()
    {
	return textAttr;
    }

    public subnodeCount()
    {
	return subnodes != null?subnodes.length:0;
    }

    public Node subnode(int index)
    {
	if (subnodes == null)
	    throw new NullPointerException("subnodes may not be null");
	if (index < 0 || index > subnodes.length)
	    throw new NullPointerException("index may not be less than zero or be equal or greater than " + subnodes.length + " (index = " + index + ")");
	return subnodes[index];
    }
}

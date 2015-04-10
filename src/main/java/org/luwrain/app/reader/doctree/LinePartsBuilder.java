
package org.luwrain.app.preview.doctree;

class LinePartsBuilder
{
    private int index = 1;
    private int offset = 0;

    private void onNode(Node node)
    {
	if (node == null)
	    throw new NullPointerException("node may not be null");
	switch(node.type())
	{
	case Node.SECTION:
	    onSection(node);
	    break;
	case Node.PARAGRAPH:
	    onParagraph(node);
	    break;
	case Node.RUN:
	    onRun(node);
	    break;
	}
	final int count = node.subnodeCount();
	for (int i = 0;i < count;++i)
	    onNode(node.subnode(i));
    }

    private void onSection(Node node)
    {
	LinePart part = new LinePart();
	part.isSection = true;
	part.isHRef = !node.href().isEmpty(); 
    }

    public void onParagraph(Node node)
    {
	offset = 0;
	index += 2;
    }

    public void onRun(Node node)
    {
	int startFrom = offset;	
	int pos = 0;
	while (pos < text.length())
	{
	    int i = pos;
	    while (i < text.length() && Character.isSpace(text.charAt(i)))
		++i;
	    if (i >= text.length())
		break;
	    while (i < text.length() && !Character.isSpace(text.charAt(i)))
		++i;
	    if (i - startPos + offset >  maxLineLen)
	    {
		LinePart part = new LinePart();
		part.node = node;
		part.posFrom = startPos;
		part.posTo = pos;
		part.textAttr = node.textAttr();
		save(part);
		offset = 0;
		++index;
		startPos = pos;
		while (startpos < text.length() && Character.isSpace(text.charAt(startPos)))
		    ++startPos;
		continue;
	    }
	    pos = i;
	}
    }

    private void save(LinePart part)
    {
    }
}

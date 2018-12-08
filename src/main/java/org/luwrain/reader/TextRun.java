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

import org.luwrain.core.NullCheck;

public class TextRun implements Run
{
    private String text = "";
    private String href = "";
private TextAttr textAttr = new TextAttr();
    private Object associatedObject = null;
    private Paragraph parentPara;
    public ExtraInfo extraInfo = null;

    public TextRun(String text)
    {
	NullCheck.notNull(text, "text");
	this.text = text;
    }

    public TextRun(String text, String href)
    {
	NullCheck.notNull(text, "text");
	this.text = text;
	this.href = href;
    }

    public TextRun(String text, String href,
ExtraInfo extraInfo)
    {
	NullCheck.notNull(text, "text");
	NullCheck.notNull(href, "href");
	this.text = text;
	this.href = href;
	this.extraInfo = extraInfo;
    }

    public String text()
    {
	return text != null?text:"";
    }

    public boolean isEmpty()
    {
	return text == null || text.isEmpty();
    }

    public String href()
    {
	return href != null?href:"";
    }

    public TextAttr textAttr()
    {
	return textAttr;
    }

    @Override public String toString()
    {
	return text != null?text:"";
    }

    @Override public void prepareText()
    {
	final StringBuilder b = new StringBuilder();
	boolean wasSpace = false;
	for(int i = 0;i < text.length();++i)
	{
	    char c = text.charAt(i);
	    if (c == '\n' || c == '\t' || c == ' ')
		c = ' ';
	    if (Character.isISOControl(c))
		continue;
	    if (wasSpace && Character.isSpace(c))
		continue;
	    b.append(c);
	    wasSpace = Character.isSpace(c);
	}
	text = new String(b);
    }

    public ExtraInfo extraInfo()
    {
	return extraInfo;
    }

@Override public void setParentNode(Node node)
    {
	NullCheck.notNull(node, "node");
	if (!(node instanceof Paragraph))
	    throw new IllegalArgumentException("node must be an instance of Paragraph");
	parentPara = (Paragraph)node;
    }

    public Node getParentNode()
    {
	return parentPara;
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

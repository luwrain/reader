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

public class Node
{
    public final static int ROOT = 1;
    public final static int SECTION = 2;
    public final static int PARAGRAPH = 3;
    public final static int RUN = 4;

    private int type;
    private Node[] subnodes = new Node[0];
    private String text = "";
    private String href = "";
    private TextAttr textAttr = new TextAttr();

    public Node(int type, String text)
    {
	this.type = type;
	this.text = text;
	if (text == null)
	    throw new NullPointerException("text may not be null");
    }

    public int type()
    {
	return type;
    }

    public String text()
    {
	return text;
    }

    public String href()
    {
	return href != null?href:"";
    }

    public TextAttr textAttr()
    {
	return textAttr;
    }

    public int subnodeCount()
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

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

public class LinePart
{
    public Node node;
    public int posFrom = 0;
    public int posTo = 0;
    public int lineNum = 0;
    public boolean isSection = false;
    public boolean isHRef = false;
    public TextAttr textAttr = new TextAttr();

    public String text()
    {
	if (node == null)
	    throw new NullPointerException("node may not be null");
	return node.text().substring(posFrom, posTo);
    }
}

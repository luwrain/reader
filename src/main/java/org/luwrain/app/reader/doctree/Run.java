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

public class Run
{
    public String text = "";
    public String href = "";
    public TextAttr textAttr = new TextAttr();
    public Paragraph parentParagraph;

    public Run(String text)
    {
	this.text = text;
	if (text == null)
	    throw new NullPointerException("text may not be null");
    }

    @Override public String toString()
    {
	return text != null?text:"";
    }
}

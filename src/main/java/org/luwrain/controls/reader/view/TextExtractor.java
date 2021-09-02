/*
   Copyright 2012-2021 Michael Pozhidaev <msp@luwrain.org>
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

package org.luwrain.controls.reader.view;

import java.util.*;

import org.luwrain.core.*;
import org.luwrain.reader.*;

class TextExtractor
{
    static protected final String LOG_COMPONENT = "document";

    final LinkedList<String> lines = new LinkedList();

    protected void onParagraphLines(RowPart[] rowParts)
    {
	NullCheck.notNullItems(rowParts, "rowParts");
	if (rowParts.length == 0)
	    return;
	int lineNum = rowParts[0].relRowNum;
	int i = 0;
	while (i < rowParts.length)
	{
	    final StringBuilder b = new StringBuilder();
	    while(i < rowParts.length && rowParts[i].relRowNum == lineNum)
	    {
		b.append(rowParts[i].getText());
		++i;
	    }
	    final String s = new String(b);
	    if (!s.isEmpty())
		lines.add(s);
	    if (i < rowParts.length)
		lineNum = rowParts[i].relRowNum;
	}
    }

    protected void addEmptyLine()
    {
	if (lines.isEmpty())
	    return;
	if (lines.getLast().isEmpty())
	    return;
	lines.add("");
    }

    public String[] getLines()
    {
	return lines.toArray(new String[lines.size()]);
    }
}

/*
   Copyright 2012-2019 Michael Pozhidaev <michael.pozhidaev@gmail.com>
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

package org.luwrain.reader.builders.pdf;

import java.io.*;
import java.util.*;
import java.net.*;

import org.luwrain.core.*;
import org.luwrain.reader.*;

final class Builder implements DocumentBuilder
{
    @Override public org.luwrain.reader.Document buildDoc(File file, Properties props) throws IOException
    {
	NullCheck.notNull(file, "file");
	NullCheck.notNull(props, "props");

		final PdfPage[] pages = new PdfCharsExtractor().getChars(file);
		//	for(PdfPage p: pages)
		return null;


    }

    @Override public org.luwrain.reader.Document buildDoc(String text, Properties props)
{
    NullCheck.notNull(text, "text");
    NullCheck.notNull(props, "props");
    return null;
    }

    @Override public org.luwrain.reader.Document buildDoc(InputStream is, Properties props) throws IOException
    {
	NullCheck.notNull(is, "is");
	NullCheck.notNull(props, "props");
	return null;
    }

    private Node[] processPage(PdfPage page)
    {
	NullCheck.notNull(page, "page");
	final List<Node> res = new LinkedList();
res.add(new NodeBuilder().addSubnode(NodeBuilder.newParagraph("Страница " + new Integer(page.num).toString())).newSection(1));
	    if (page.chars.length == 0)
		return res.toArray(new Node[res.size()]);
	    StringBuilder b = new StringBuilder();
	    double y = page.chars[0].y;
	    for(PdfChar c: page.chars)
	    {
		if (y - 0.1 > c.y)
		{
		    res.add(NodeBuilder.newParagraph(new String(b)));
			    b = new StringBuilder();
			    b.append(c.ch);
			    y = c.y;
			    			    continue;
		}
		b.append(c.ch);
	    }
	    if (b.length() > 0)
		res.add(NodeBuilder.newParagraph(new String(b)));
	return res.toArray(new Node[res.size()]);
    }
}

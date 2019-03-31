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

import java.awt.geom.AffineTransform;
import org.apache.pdfbox.pdmodel.font.*;
import org.apache.pdfbox.util.*;
import org.apache.pdfbox.pdmodel.*;

import org.apache.pdfbox.contentstream.operator.DrawObject;
import org.apache.pdfbox.contentstream.operator.state.*;
import org.apache.pdfbox.contentstream.operator.text.*;
import org.apache.pdfbox.text.*;




import org.luwrain.core.*;
import org.luwrain.reader.*;

final class Builder implements DocumentBuilder
{
    @Override public org.luwrain.reader.Document buildDoc(File file, Properties props) throws IOException
    {
	NullCheck.notNull(file, "file");
	NullCheck.notNull(props, "props");

		PDDocument pdf = PDDocument.load(new FileInputStream(file));
            PDFTextStripper pdfStripper = new PDFTextStripper();
            pdfStripper.setSortByPosition(true);
            String text = pdfStripper.getText(pdf);

	    
	final NodeBuilder builder = new NodeBuilder();
	for(String s: text.split("\n", -1))
	    builder.addParagraph(s);

	return new Document(builder.newRoot());
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
	    double x = page.chars[0].x;
	    	    double  y = page.chars[0].y;
	    for(PdfChar c: page.chars)
	    {
		if (y - 0.1 > c.y)
		{
		    res.add(NodeBuilder.newParagraph(new String(b)));
			    b = new StringBuilder();
			    b.append(c.ch);
			    y = c.y;
			    x = c.x;
			    			    continue;
		}
		b.append(c.ch);
		Log.debug("offset", String.format("%.2f", c.x - x));
		x = c.x;
	    }
	    if (b.length() > 0)
		res.add(NodeBuilder.newParagraph(new String(b)));
	return res.toArray(new Node[res.size()]);
    }
}

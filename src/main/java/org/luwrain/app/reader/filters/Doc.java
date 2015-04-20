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

package org.luwrain.app.reader.filters;

import java.util.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.luwrain.app.reader.doctree.Document;
import org.luwrain.app.reader.doctree.Node;

import org.apache.poi.hwpf.*;
import org.apache.poi.hwpf.usermodel.*;
import org.apache.poi.hwpf.extractor.WordExtractor;

class Doc implements Filter
{
    private String fileName = "";

    public Doc(String fileName)
    {
	this.fileName = fileName;
	if (fileName == null)
	    throw new NullPointerException("fileName may not be null");
    }

	@Override public Document constructDocument()
    {
	System.out.println("reader:doc running");
	FileInputStream  s = null;
	try {
	    File docFile=new File(fileName);
	    s = new FileInputStream(docFile.getAbsolutePath());
	    HWPFDocument doc = new HWPFDocument(s);
	    Document res = transform(doc);
	    s.close(); //closing fileinputstream
	    return res;
	} catch (IOException e)
	{
	    e.printStackTrace();
	    try {
		if (s != null)
		    s.close();
	    }
	    catch (IOException ee) {}
	    return null;
	}
}

	private Document transform(HWPFDocument doc)
	{
	    //	    System.out.println();
	    final String wholeText = doc.getDocumentText();

	    //	    Node root = new Node(Node.ROOT);
	    Vector<Node> subnodes = new Vector<Node>();

	    Range range = doc.getRange();
	    //	    System.out.println("reader:" + range.getStartOffset() + ":" + range.getEndOffset());
	    final int begin = range.getStartOffset();
	    final int end = range.getEndOffset();
	    System.out.println("reader:range:" + begin + ":" + end);
	    System.out.println("reader:text len=" + wholeText.length() );
	    for(int i = 0;i < range.numParagraphs();++i)
	    {
		//		System.out.println("reader:i=" + i);
final Paragraph para = range.getParagraph(i);
final String paraText = wholeText.substring(para.getStartOffset(), para.getEndOffset());
int k = 0;
while(k < paraText.length() && !Character.isLetter(paraText.charAt(k)) && !Character.isDigit(paraText.charAt(k)))
    ++k;
if (k >= paraText.length())
    continue;

		subnodes.add(new org.luwrain.app.reader.doctree.Paragraph(new org.luwrain.app.reader.doctree.Run(paraText)));
	    }
	    Node root = new Node(Node.ROOT);
	    root.subnodes = subnodes.toArray(new Node[subnodes.size()]);
	    root.setParentOfSubnodes();
	    Document res = new Document(root);
	    res.buildView(50);
	    return res;
	}
}

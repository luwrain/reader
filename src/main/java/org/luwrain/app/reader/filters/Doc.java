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
    private String wholeText;

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
wholeText = doc.getDocumentText();

	    Vector<Node> subnodes = new Vector<Node>();

	    Range range = doc.getRange();
	    final int begin = range.getStartOffset();
	    final int end = range.getEndOffset();
	    System.out.println("reader:range:" + begin + ":" + end);
	    System.out.println("reader:text len=" + wholeText.length() );
	    for(int i = 0;i < range.numParagraphs();++i)
	    {
final Paragraph para = range.getParagraph(i);
if (para.getTableLevel() > 0)
{
		Table table = range.getTable(para);
		if (table != null)
		{
		    subnodes.add(onTable(table));
		    continue;
		}



}

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

    private Node onTable(Table table)
    {
	final Vector<Node> resRows = new Vector<Node>();
	for(int i = 0;i < table.numRows();++i)
	{
	    final Vector<Node> resCells = new Vector<Node>();
	    final TableRow row = table.getRow(i);
	    for(int j = 0;j < row.numCells();++j)
	    {
		final TableCell cell = row.getCell(j);
		resCells.add(new Node(Node.TABLE_CELL, transformRange(cell)));
	    }
	    resRows.add(new Node(Node.TABLE_ROW, resCells.toArray(new Node[resCells.size()])));
	}
	return new Node(Node.TABLE, resRows.toArray(new Node[resRows.size()]));
    }

    private Node[] transformRange(Range range)
    {
	LinkedList<Node> nodes = new LinkedList<Node>();
	for(int i = 0;i < range.numParagraphs();++i)
	{
	    org.luwrain.app.reader.doctree.Paragraph para = onParagraph(range.getParagraph(i));
	    if (para != null)
		nodes.add(para);
	}
	return nodes.toArray(new Node[nodes.size()]);
    }

    private org.luwrain.app.reader.doctree.Paragraph onParagraph(Paragraph para)
    {
	final String paraText = wholeText.substring(para.getStartOffset(), para.getEndOffset());
	int k = 0;
	while(k < paraText.length() && !Character.isLetter(paraText.charAt(k)) && !Character.isDigit(paraText.charAt(k)))
	    ++k;
	if (k >= paraText.length())
	    return null;
	return new org.luwrain.app.reader.doctree.Paragraph(new org.luwrain.app.reader.doctree.Run(paraText));
    }
}

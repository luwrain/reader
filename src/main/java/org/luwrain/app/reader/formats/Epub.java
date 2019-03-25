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

package org.luwrain.app.reader.formats;

import java.util.*;
import java.util.Map.Entry;
import java.io.*;

import org.luwrain.reader.Document;
import org.luwrain.reader.Node;
import org.luwrain.reader.Node;
import org.luwrain.reader.NodeFactory;
import org.luwrain.reader.Paragraph;

import nl.siegmann.epublib.domain.Book;
import nl.siegmann.epublib.domain.Resource;
import nl.siegmann.epublib.domain.SpineReference;
import nl.siegmann.epublib.epub.EpubReader;

import org.apache.poi.hwpf.*;
import org.apache.poi.hwpf.usermodel.*;
import org.apache.poi.hwpf.extractor.WordExtractor;

public class Epub
{
    private String fileName = "";
    private String wholeText;

    public Epub(String fileName)
    {
	this.fileName = fileName;
	if (fileName == null)
	    throw new NullPointerException("fileName may not be null");
    }

    public Document constructDocument()
    {
	try {
		final Node root = NodeFactory.newNode(Node.Type.ROOT);
		final LinkedList<Node> subnodes = new LinkedList<Node>();
		EpubReader epubReader = new EpubReader();
		Book book = epubReader.readEpub(new FileInputStream(fileName));
		List<String> titles = book.getMetadata().getTitles();
		for(String s: titles)
		{
			Node h1=NodeFactory.newSection(1);
			h1.setSubnodes(new Node[]{NodeFactory.newPara(s)});
			subnodes.add(h1);
		}
		for(SpineReference r: book.getSpine().getSpineReferences())
		{
		    Resource res = r.getResource();
		    BufferedReader reader = new BufferedReader(res.getReader());
		    String result="";
		    String line;
		    while ( (line = reader.readLine()) != null)
		    {
		    	if(line.startsWith("<?xml")) continue; // FIXME: make this fix inside Html parser
		    	result+=line+"\n";
		    }
		    //FIXME:
		    //		    				Html html=new Html(false,result);
		    Document subdoc = null;//html.constructDocument("UTF-8");
		    for(Node node:subdoc.getRoot().getSubnodes())
				subnodes.add(node);
		}
		//Range range = doc.getRange();
		root.setSubnodes(subnodes.toArray(new Node[subnodes.size()]));
		return new Document(root);
	} catch (IOException e)
	{
	    e.printStackTrace();
	    return null;
	}
    }

}

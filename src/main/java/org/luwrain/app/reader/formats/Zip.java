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
import java.util.zip.*;
import java.io.*;
import java.net.*;
import java.nio.file.*;

import org.luwrain.reader.*;
import org.luwrain.core.*;

public class Zip
{
    private final Path path;
    private ItemLoader itemLoader;

    public Zip(Path path, ItemLoader itemLoader)
    {
	NullCheck.notNull(path, "path");
	NullCheck.notNull(itemLoader, "itemLoader");
	this.path = path;
	this.itemLoader = itemLoader;
    }

    public Document createDoc() throws IOException
    {
	Log.debug("doctree-zip", "reading zip file " + path.toString());
	    final Node root = NodeFactory.newNode(Node.Type.ROOT);
	    final LinkedList<Node> subnodes = new LinkedList<Node>();
	ZipFile zip = null;
	try {
	    zip = new ZipFile(path.toString());
	    for(Enumeration e = zip.entries();e.hasMoreElements();)
	    {
		final ZipEntry entry = (ZipEntry)e.nextElement();
		Log.debug("doctree-zip", "reading zip entry with name \'" + entry.getName() + "\'");
		if(entry.isDirectory()) 
		    continue;
		final Document subdoc = itemLoader.load(zip.getInputStream(entry));
		if (subdoc != null)
		{
		    for(Node node: subdoc.getRoot().getSubnodes())
			subnodes.add(node);
		} else
		    Log.error("doctree-zip", "unable to read zip subdoc with name " + entry.getName());
	    }
	    root.setSubnodes(subnodes.toArray(new Node[subnodes.size()]));
	    return new Document(root);
	}
	finally {
	    if (zip != null)
		zip.close();
	}
    }

public interface ItemLoader
{
    Document load(InputStream stream);
}
}

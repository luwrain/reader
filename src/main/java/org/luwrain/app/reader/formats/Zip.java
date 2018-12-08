
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



package org.luwrain.app.reader.doctree;

import java.io.*;

public class DumpInFileSystem
{
    private Node root;

    public DumpInFileSystem(Node root) 
    {
	this.root = root;
	if (root == null)
	    throw new NullPointerException("root may not be null");
    }

    public boolean dump(String path)
    {
	try {
	    dump(root, new File(path));
	    return true;
	}
	catch (IOException e)
	{
	    e.printStackTrace();
	    return false;
	}
    }

    private void dump(Node n, File f) throws IOException
    {
	if (n.type == Node.PARAGRAPH && (n instanceof Paragraph))
	{
	    dumpParagraph((Paragraph)n, f);
	    return;
	}
	f.mkdir();
	for(int i = 0;i < n.subnodes.length;++i)
	{
	    Node sn = n.subnodes[i];
	    if (sn == null)
	    {
		System.out.println("warning:" + f.getAbsolutePath() + ":has null subnode");
		continue;
	    }
	    String name = i < 10?"0" + i:"" + i;
	    name += " " + typeName(sn.type);
	    dump(sn, new File(f, name));
	}
    }

    private void dumpParagraph(Paragraph p, File f) throws IOException
    {
	f.mkdir();
	for(int i = 0;i < p.runs.length;++i)
	{
	    final Run r = p.runs[i];
	    if (r == null)
	    {
		System.out.println("warning:" + f.getAbsolutePath() + ":has null run");
		continue;
	    }
	    String name = i < 10?"0" + i:"" + i;
	    final File ff = new File(f, name);
	    ff.mkdir();
	}
    }

    private static String typeName(int type)
    {
	switch(type)
	{
	case Node.ROOT:
	    return "root";
	case Node.PARAGRAPH:
	    return "paragraph";
	case Node.TABLE:
	    return "table";
	case Node.TABLE_ROW:
	    return "tablerow";
	case Node.TABLE_CELL:
	    return "tablecell";
	default:
	    return "unknown"  + type;
	}
    }
}

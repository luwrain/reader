
package org.luwrain.app.reader.formats;

import java.util.*;
import java.io.*;
import java.nio.file.*;
import java.nio.charset.*;
import java.nio.*;

import org.luwrain.core.*;
import org.luwrain.doctree.*;

public class Txt
{
    public enum ParaStyle {EMPTY_LINES, EACH_LINE, INDENT};

    private Path path;
    private String charset;
    private ParaStyle paraStyle;

    public Txt(ParaStyle paraStyle,
	       Path path, String charset)
    {
	NullCheck.notNull(paraStyle, "paraStyle");
	NullCheck.notNull(path, "path");
	NullCheck.notNull(charset, "charset");
	this.paraStyle = paraStyle;
	this.path = path;
	this.charset = charset;
    }

    public Document constructDocument() throws IOException
    {
	final String[] lines = read(path, Charset.forName(charset));
	switch(paraStyle)
	{
	case EMPTY_LINES:
	    return formatParaEmptyLines(lines);
	case INDENT:
	    return formatParaIndent(lines);
	case EACH_LINE:
	    return formatParaEachLine(lines);
	default:
	    return null;
	}
    }

    private Document formatParaEmptyLines(String[] lines)
    {
	final LinkedList<String> paraLines = new LinkedList<String>();
	final LinkedList<Node> nodes = new LinkedList<Node>();
	for(String line: lines)
	{
	    if (line.trim().isEmpty())
	    {
		final Paragraph para = createPara(paraLines);
		if (para != null)
		    nodes.add(para);
		continue;
	    }
	    paraLines.add(line.trim());
	}
	final Paragraph para = createPara(paraLines);
	if (para != null)
	    nodes.add(para);
	final Node root = NodeFactory.newNode(Node.Type.ROOT); 
	root.setSubnodes(nodes.toArray(new Node[nodes.size()]));
	return new Document(root);
    }

    private Document formatParaEachLine(String[] lines)
    {
	NullCheck.notNullItems(lines, "lines");
	final LinkedList<Node> nodes = new LinkedList<Node>();
	for(String line: lines)
	{
	    if (line.trim().isEmpty())
		continue;
	    nodes.add(NodeFactory.newPara(line.trim()));
	}
	final Node root = NodeFactory.newNode(Node.Type.ROOT); 
	root.setSubnodes(nodes.toArray(new Node[nodes.size()]));
	return new Document(root);
    }

    private Document formatParaIndent(String[] lines)
    {
	final LinkedList<String> paraLines = new LinkedList<String>();
	final LinkedList<Node> nodes = new LinkedList<Node>();
	for(String line: lines)
	{
	    if (line.trim().isEmpty())
	    {
		final Node para = createPara(paraLines);
		if (para != null)
		    nodes.add(para);
		continue;
	    }
	    int indent = 0;
	    while(indent < line.length() && Character.isSpace(line.charAt(indent)))
		indent++;
	    if (indent > 0)
	    {
		final Node para = createPara(paraLines);
		if (para != null)
		    nodes.add(para);
		paraLines.add(line.trim());
		continue;
	    }
	    paraLines.add(line.trim());
	}
	final Node para = createPara(paraLines);
	if (para != null)
	    nodes.add(para);
	final Node root = NodeFactory.newNode(Node.Type.ROOT); 
	root.setSubnodes(nodes.toArray(new Node[nodes.size()]));
	return new Document(root);
    }

    private Paragraph createPara(LinkedList<String> linesList)
    {
	final String[] lines = linesList.toArray(new String[linesList.size()]);
	linesList.clear();
	if (lines.length < 1)
	    return null;
	if (lines.length == 1)
	    return NodeFactory.newPara(lines[0]);
	final StringBuilder b = new StringBuilder();
	b.append(lines[0]);
	for(int i = 1;i < lines.length;++i)
	    b.append(" " + lines[i]);
	return NodeFactory.newPara(b.toString());
    }



    static private String[] read(Path path, Charset charset) throws IOException
    {
	NullCheck.notNull(path, "path");
	NullCheck.notNull(charset, "charset");
	    final byte[] bytes = Files.readAllBytes(path);
	    final CharBuffer charBuf = charset.decode(ByteBuffer.wrap(bytes));
	    return new String(charBuf.array(), charBuf.arrayOffset(), charBuf.length()).split("\n", -1);
    }
}

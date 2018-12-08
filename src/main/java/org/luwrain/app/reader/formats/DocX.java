
package org.luwrain.app.reader.formats;

import java.util.*;
import java.util.Map.Entry;
import java.io.*;
import java.math.BigInteger;
import java.nio.file.*;

import org.luwrain.reader.*;
import org.apache.poi.xwpf.usermodel.*;

import org.luwrain.core.Log;
import org.luwrain.core.NullCheck;

public class DocX
{
    private Path path;

    DocX(Path path)
    {
	NullCheck.notNull(path, "path");
	this.path = path;
    }

    private  org.luwrain.reader.Document process()
    {
	Log.debug("doctree-docx", "starting reading of " + path.toString());
    	try
    	{
	    final InputStream s = Files.newInputStream(path);
	    final org.luwrain .reader.Document res;
	    try {
		XWPFDocument doc = new XWPFDocument(s);
		res = transform(doc);
		res.setProperty("format", "DOCX");
		res.setProperty("url", path.toUri().toURL().toString());
	    }
	    finally {
		s.close();
	    }
	    Log.debug("doctree-docx", "reading of " + path.toString() + " finished");
	    return res;
    	} catch (IOException e)
    	{
	    Log.error("doctree-docx", "unable to parse " + path.toString() + ":" + e.getClass().getName() + ":" + e.getMessage());
	    e.printStackTrace();
	    return null;
	}
    }

    private org.luwrain.reader.Document transform(XWPFDocument doc)
    {
	NullCheck.notNull(doc, "doc");
	final LinkedList<Node> subnodes = new LinkedList<Node>();
	transformNodes(subnodes, doc.getBodyElements());
	final Node root = NodeFactory.newNode(Node.Type.ROOT);
	root.setSubnodes(subnodes.toArray(new Node[subnodes.size()]));
	return new org.luwrain.reader.Document(root);
    }

    private void transformNodes(LinkedList<Node> subnodes, List<IBodyElement> range)
    {
	NullCheck.notNull(subnodes, "subnodes");
	NullCheck.notNull(range, "range");
	for (IBodyElement p: range)
	    if (p instanceof XWPFTable)
		subnodes.add(transformTable((XWPFTable) p)); else
		parseParagraph(subnodes, p);
    }

    private Node transformTable(XWPFTable table)
    {
	NullCheck.notNull(table, "table");
	final Node res = NodeFactory.newNode(Node.Type.TABLE);
	final LinkedList<Node> rows = new LinkedList<Node>();
	for (final XWPFTableRow row: table.getRows())
	{ // для каждой строки таблицы
	    final Node rowNode = NodeFactory.newNode(Node.Type.TABLE_ROW);
	    rows.add(rowNode);
	    final LinkedList<Node> cells = new LinkedList<Node>();
	    for (final XWPFTableCell cell: row.getTableCells())
	    { // для каждой ячейки таблицы
		final Node cellNode = NodeFactory.newNode(Node.Type.TABLE_CELL);
		final LinkedList<Node> nodes = new LinkedList<Node>();
		cells.add(cellNode);
		transformNodes(nodes, cell.getBodyElements());
		cellNode.setSubnodes(nodes.toArray(new Node[nodes.size()]));
		//		checkNodesNotNull(cellNode.subnodes);
	    }
	    rowNode.setSubnodes(cells.toArray(new Node[cells.size()]));
	    //	    checkNodesNotNull(rowNode.subnodes);
	} // for(trows);
	res.setSubnodes(rows.toArray(new Node[rows.size()]));
	//	checkNodesNotNull(res.subnodes);
	return res;
    }

    private void parseParagraph(LinkedList<Node> subnodes, IBodyElement el)
    {
	NullCheck.notNull(subnodes, "subnodes");
	NullCheck.notNull(el, "el");
	if (el instanceof XWPFParagraph)
	{
	    final XWPFParagraph para = (XWPFParagraph) el;
	    //FIXME:Proper lists processing
	    //	    if (para.getNumIlvl() != null)
	    //		transformListItem(subnodes, para); else 
		subnodes.add(NodeFactory.newPara(para.getText().trim()));
	} else
	    Log.warning("doctree-docx", "unhandled element of class " + el.getClass().getName());
    }

    private void checkNodesNotNull(Node[] nodes)
    {
	if (nodes == null)
	    throw new NullPointerException("nodes is null");
	for (int i = 0; i < nodes.length; ++i)
	    if (nodes[i] == null)
		throw new NullPointerException("nodes[" + i + "] is null");
    }

    static public org.luwrain.reader.Document read(Path path)
    {
	NullCheck.notNull(path, "path");
	return new DocX(path).process();
    }
}

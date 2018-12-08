
package org.luwrain.app.reader.formats;

import java.util.*;
import java.util.Map.Entry;
import java.io.*;
import java.nio.file.*;

import org.luwrain.core.NullCheck;
import org.luwrain.reader.Document;
import org.luwrain.reader.Node;
import org.luwrain.reader.Node;
import org.luwrain.reader.NodeFactory;
import org.luwrain.reader.Paragraph;

import org.apache.poi.hwpf.*;
import org.apache.poi.hwpf.usermodel.*;
import org.apache.poi.hwpf.extractor.WordExtractor;

public class Doc
{
    private final Path path;
    private String wholeText;

    private Doc(Path path)
    {
	NullCheck.notNull(path, "path");
	this.path = path;
    }

    public Document process()
    {
	try {
	    final InputStream s = Files.newInputStream(path);
	    final HWPFDocument doc = new HWPFDocument(s);
	    final Document res = transform(doc);
	    res.setProperty("format", "DOC");
	    res.setProperty("url", path.toUri().toURL().toString());
	    s.close();
	    return res;
	} catch (IOException e)
	{
	    e.printStackTrace();
	    return null;
	}
    }

    private Document transform(HWPFDocument doc)
    {
	wholeText = doc.getDocumentText();
	final LinkedList<Node> subnodes = new LinkedList<Node>();
	Range range = doc.getRange();
	anyRangeAsParagraph(subnodes,range,0);
	final Node root = NodeFactory.newNode(Node.Type.ROOT);
	root.setSubnodes(subnodes.toArray(new Node[subnodes.size()]));
	return new Document(root);
    }

    /* рекурсивный метод, вызывается для любого места в документе, способного содержать несколько элементов, представляя их как список параграфов
     * @param	subnodes	The list of nodes to get all new on current level
     * @param	range	The range to look through
     * @param	lvl	Current recurse level (must be zero for the root)
     */
    private void anyRangeAsParagraph(LinkedList<Node> subnodes,
				    Range range,
				    int lvl)
    {
	int i=0;
	final int num=range.numParagraphs();
	Boolean inTable=false;//Allows to silently skip all table cells except of first;
	while(i<num)
	{
	    final org.apache.poi.hwpf.usermodel.Paragraph paragraph = range.getParagraph(i);
	    if (paragraph.getTableLevel() > lvl)
	    {
		if(!inTable)
		{
		    //We do this processing for the first cell only, skipping all others
		    final Node table_node = NodeFactory.newNode(Node.Type.TABLE);
		    subnodes.add(table_node);
		    final LinkedList<Node> rows_subnodes = new LinkedList<Node>();
		    inTable=true;//We came to the table;
		    final org.apache.poi.hwpf.usermodel.Table table = range.getTable(paragraph);
		    final int rnum=table.numRows();
		    for(int r=0;r<rnum;r++)
		    { // для каждой строки таблицы
			// создаем элементы структуры Node и добавляем текущую ноду в список потомка
			final Node rowtable_node = NodeFactory.newNode(Node.Type.TABLE_ROW);
			rows_subnodes.add(rowtable_node);
			final LinkedList<Node> cels_subnodes = new LinkedList<Node>();
			final TableRow trow=table.getRow(r);
			final int cnum=trow.numCells();
			for(int c=0;c<cnum;c++)
			{ // для каждой ячейки таблицы
			    //Creating a node for table cell
			    final Node celltable_node = NodeFactory.newNode(Node.Type.TABLE_CELL);
			    final LinkedList<Node> incell_subnodes = new LinkedList<Node>();
			    cels_subnodes.add(celltable_node);
			    final TableCell cell=trow.getCell(c);
			    //Trying to figure out that we have just a text in the table cell
			    if(cell.numParagraphs()>1)
				anyRangeAsParagraph(incell_subnodes,cell,lvl+1); else
				parseParagraph(incell_subnodes,cell.getParagraph(0));
			    celltable_node.setSubnodes(incell_subnodes.toArray(new Node[incell_subnodes.size()]));
			    //			    checkNodesNotNull(celltable_node.subnodes);
			} //for(cells);
			rowtable_node.setSubnodes(cels_subnodes.toArray(new Node[cels_subnodes.size()]));
			//			checkNodesNotNull(rowtable_node.subnodes);
		    } //for(rows);
		    table_node.setSubnodes(rows_subnodes.toArray(new Node[rows_subnodes.size()]));
		    //		    checkNodesNotNull(table_node.subnodes);
		} // if(!inTable);
	    } else //if(paragraph.getTableLevel() > lvl);
	    {
		inTable=false;//We are not in table any more
		parseParagraph(subnodes,paragraph);
	    }
	    i++;
	} //while();
    }

    // listInfo[id of list][level]=counter;
    public HashMap<Integer,HashMap<Integer,Integer>> listInfo=new HashMap<Integer, HashMap<Integer,Integer>>();
    public int lastLvl=-1;

	/* Анализирует тип параграфа и выделяет в соответствии с ним данные
	 * @param	subnodes	список нод на текущем уровне собираемой структуры, в этот список будут добавлены новые элементы
	 * @param	paragraph	элемент документа (параграф или элемент списка) или ячейка таблицы
	 */
private void parseParagraph(LinkedList<Node> subnodes, org.apache.poi.hwpf.usermodel.Paragraph paragraph)
	{
		String className=paragraph.getClass().getSimpleName();
		String paraText="";
		switch(className)
		{
			case "ListEntry":
				// создаем элементы структуры Node и добавляем текущую ноду в список потомка
				Node node = NodeFactory.newNode(Node.Type.LIST_ITEM);
				subnodes.add(node);
				//
				ListEntry elem=(ListEntry)paragraph;
				int sindex=elem.getStyleIndex();
				//StyleDescription style=that.doc.getStyleSheet().getStyleDescription(sindex);
				int listId=elem.getList().getLsid();
				int listLvl=elem.getIlvl();
				// если это новый список, то добавим пустой подсписок его счетчиков
				if(!listInfo.containsKey(listId)) listInfo.put(listId,new HashMap<Integer,Integer>());
				// если уровень списка уменьшился, то очищаем счетчики выше уровнем
				if(lastLvl>listLvl)
				{
					for(Entry<Integer,Integer> lvls:listInfo.get(listId).entrySet())
						if(lvls.getKey()>listLvl)
							listInfo.get(listId).put(lvls.getKey(), 1);
				}
				lastLvl=listLvl;
				// если в списке счетчиков значения нет, то иннициализируем его 0 (позже он будет обязательно увеличен на 1)
				if(!listInfo.get(listId).containsKey(listLvl)) listInfo.get(listId).put(listLvl, 0);
				// так как это очередной элемент списка, то увеличиваем его счетчик на 1
				listInfo.get(listId).put(listLvl, listInfo.get(listId).get(listLvl)+1);
				// формируем строку-номер
				String numstr="";
				for(int lvl=0;lvl<=listLvl;lvl++) numstr+=listInfo.get(listId).get(lvl)+".";
				paraText=paragraph.text().trim();
				final LinkedList<Node> item_subnodes = new LinkedList<Node>();
				item_subnodes.add(NodeFactory.newPara(paraText));
				node.setSubnodes(item_subnodes.toArray(new Node[item_subnodes.size()]));
			break;
			case "Paragraph":
				paraText=paragraph.text().trim();
				subnodes.add(NodeFactory.newPara(paraText));
				/*
				// получение стилей текста
				int nrun=paragraph.numCharacterRuns();
				if(nrun>1) for(int r=0;r<nrun;r++)
				{
					CharacterRun run=paragraph.getCharacterRun(r);
				}
				*/
			break;
			default:
				paraText=paragraph.text().trim();
				subnodes.add(NodeFactory.newPara(paraText));
			break;
		}
	}

    /*
    private void checkNodesNotNull(Node[] nodes)
    {
	if (nodes == null)
	    throw new NullPointerException("nodes is null");
	for(int i = 0;i < nodes.length;++i)
	    if (nodes[i] == null)
		throw new NullPointerException("nodes[" + i + "] is null");
    }
    */

    static public org.luwrain.reader.Document read(Path path)
    {
	NullCheck.notNull(path, "path");
	return new Doc(path).process();
    }


}

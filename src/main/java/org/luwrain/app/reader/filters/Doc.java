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
import java.util.Map.Entry;
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
		// System.out.println();
		wholeText = doc.getDocumentText();

		Vector<Node> subnodes = new Vector<Node>();

		Range range = doc.getRange();
		anyRangeAsParagraph(subnodes,range,0);
		
		Node root = new Node(Node.ROOT);
		root.subnodes = subnodes.toArray(new Node[subnodes.size()]);
		root.setParentOfSubnodes();
		Document res = new Document(root);
		res.buildView(50);
		return res;
	}

	/* рекурсивный метод, вызывается для любого места в документе, способного содержать несколько элементов, представляя их как список параграфов
	 * @param	subnodes	список нод на текущем уровне собираемой структуры, в этот список будут добавлены новые элементы
	 * @param	range	текущий элемент в документе
	 * @param	lvl	текущий уровень вложенности, для корневого элемента должен быть установлен в 0
	 */
	public void anyRangeAsParagraph(Vector<Node> subnodes,Range range,int lvl)
	{
		int i=0,num=range.numParagraphs();
		Boolean inTable=false;
		while(i<num)
		{
			final Paragraph paragraph = range.getParagraph(i);
			if (paragraph.getTableLevel() > lvl)
			{
				if(!inTable)
				{
					// первая ячейка таблицы, для остальных ячеек ЭТОЙ же таблицы, этот участок не вызывается
					Node table_node = new Node(Node.TABLE);
					Vector<Node> rows_subnodes = new Vector<Node>();
					//
					inTable=true;
					Table table = range.getTable(paragraph);
					System.out.println(lvl+", is a table: "+table.numRows()+" rows");
					int rnum=table.numRows();
					for(int r=0;r<rnum;r++)
					{ // для каждой строки таблицы
						// создаем элементы структуры Node и добавляем текущую ноду в список потомка
						Node rowtable_node=new Node(Node.TABLE_ROW);
						rows_subnodes.add(rowtable_node);
						//
						Vector<Node> cels_subnodes = new Vector<Node>();
						//
						TableRow trow=table.getRow(r);
						int cnum=trow.numCells();
						for(int c=0;c<cnum;c++)
						{ // для каждой ячейки таблицы
							// создаем элементы структуры Node
							Node celltable_node=new Node(Node.TABLE_CELL);
							Vector<Node> incell_subnodes = new Vector<Node>();
							cels_subnodes.add(celltable_node);
							//
							System.out.print("* cell["+r+","+c+"]: ");
							TableCell cell=trow.getCell(c);
							// определим что содержимое ячейки просто текст
							if(cell.numParagraphs()>1)
							{
								System.out.println("");
								anyRangeAsParagraph(incell_subnodes,cell,lvl+1);
								
							} else
							{
								parseParagraph(incell_subnodes,cell.getParagraph(0));
							}
							celltable_node.subnodes = incell_subnodes.toArray(new Node[incell_subnodes.size()]);
							celltable_node.setParentOfSubnodes();
						}
						rowtable_node.subnodes = cels_subnodes.toArray(new Node[cels_subnodes.size()]);
						rowtable_node.setParentOfSubnodes();
					}
					table_node.subnodes = rows_subnodes.toArray(new Node[rows_subnodes.size()]);
					table_node.setParentOfSubnodes();
				}
			} else
			{
				inTable=false;
				System.out.print(lvl+", not table: ");
				parseParagraph(subnodes,paragraph);
			}
			i++;
		}
	}
	// listInfo[id of list][level]=counter;
	public HashMap<Integer,HashMap<Integer,Integer>> listInfo=new HashMap<Integer, HashMap<Integer,Integer>>();
	public int lastLvl=-1;
	/* Анализирует тип параграфа и выделяет в соответствии с ним данные
	 * @param	subnodes	список нод на текущем уровне собираемой структуры, в этот список будут добавлены новые элементы
	 * @param	paragraph	элемент документа (параграф или элемент списка) или ячейка таблицы
	 */
	public void parseParagraph(Vector<Node> subnodes,Paragraph paragraph)
	{
		String className=paragraph.getClass().getSimpleName();
		String paraText="";
		switch(className)
		{
			case "ListEntry":
				// создаем элементы структуры Node и добавляем текущую ноду в список потомка
				Node node=new Node(Node.LIST_ITEM);
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
				System.out.println("LIST ENTRY:"+listLvl+", "+listId+", "+numstr+"["+paraText+"]");
				//
				Vector<Node> item_subnodes = new Vector<Node>();
				item_subnodes.add(new org.luwrain.app.reader.doctree.Paragraph(new org.luwrain.app.reader.doctree.Run(paraText)));
				node.subnodes = item_subnodes.toArray(new Node[item_subnodes.size()]);
				node.setParentOfSubnodes();
			break;
			case "Paragraph":
				paraText=paragraph.text().trim();
				System.out.println("PARAGRAPH:["+paraText+"]");
				subnodes.add(new org.luwrain.app.reader.doctree.Paragraph(new org.luwrain.app.reader.doctree.Run(paraText)));
				/*
				// получение стилей текста
				int nrun=paragraph.numCharacterRuns();
				if(nrun>1) for(int r=0;r<nrun;r++)
				{
					CharacterRun run=paragraph.getCharacterRun(r);
					System.out.println("RUN: ["+run.text()+"]");
				}
				*/
			break;
			default:
				paraText=paragraph.text().trim();
				System.out.println(className+"["+paraText+"]");
				subnodes.add(new org.luwrain.app.reader.doctree.Paragraph(new org.luwrain.app.reader.doctree.Run(paraText)));
			break;
		}
	}

/*	
	private Document transform(HWPFDocument doc)
	{
		// System.out.println();
		wholeText = doc.getDocumentText();

		Vector<Node> subnodes = new Vector<Node>();

		Range range = doc.getRange();
		final int begin = range.getStartOffset();
		final int end = range.getEndOffset();
		System.out.println("reader:range:" + begin + ":" + end);
		System.out.println("reader:text len=" + wholeText.length());
		anyRangeAsParagraph(subnodes,range);
		
		Node root = new Node(Node.ROOT);
		root.subnodes = subnodes.toArray(new Node[subnodes.size()]);
		root.setParentOfSubnodes();
		Document res = new Document(root);
		res.buildView(50);
		return res;
	}
	
	private void anyRangeAsParagraph(Vector<Node> subnodes, Range range)
	{
		int i=0,num=range.numParagraphs();
		Boolean inTable=false;
		while(i<num)
		{
			final Paragraph para = range.getParagraph(i);
			if (para.getTableLevel() > 0)
			{
				if(!inTable)
				{
					inTable=true;
					// первая ячейка таблицы, для следующих метод не вызывается
					Table table = range.getTable(para);
					if (table != null)
					{
						subnodes.add(onTable(table));
						continue;
					}

				}
			}

			final String paraText = wholeText.substring(para.getStartOffset(),
					para.getEndOffset());
			int k = 0;
			while (k < paraText.length()
					&& !Character.isLetter(paraText.charAt(k))
					&& !Character.isDigit(paraText.charAt(k)))
				++k;
			if (k >= paraText.length())
				continue;

			subnodes.add(new org.luwrain.app.reader.doctree.Paragraph(
					new org.luwrain.app.reader.doctree.Run(paraText)));
			i++;
		}
	}
	private Node onTable(Table table)
	{
		final Vector<Node> resRows = new Vector<Node>();
		for (int i = 0; i < table.numRows(); ++i)
		{
			final Vector<Node> resCells = new Vector<Node>();
			final TableRow row = table.getRow(i);
			for (int j = 0; j < row.numCells(); ++j)
			{
				final TableCell cell = row.getCell(j);
				resCells.add(new Node(Node.TABLE_CELL, transformRange(cell)));
			}
			resRows.add(new Node(Node.TABLE_ROW, resCells
					.toArray(new Node[resCells.size()])));
		}
		return new Node(Node.TABLE, resRows.toArray(new Node[resRows.size()]));
	}

	private Node[] transformRange(Range range)
	{
		LinkedList<Node> nodes = new LinkedList<Node>();
		for (int i = 0; i < range.numParagraphs(); ++i)
		{
			org.luwrain.app.reader.doctree.Paragraph para = onParagraph(range
					.getParagraph(i));
			if (para != null)
				nodes.add(para);
		}
		return nodes.toArray(new Node[nodes.size()]);
	}

	private org.luwrain.app.reader.doctree.Paragraph onParagraph(Paragraph para)
	{
		final String paraText = wholeText.substring(para.getStartOffset(),
				para.getEndOffset());
		int k = 0;
		while (k < paraText.length() && !Character.isLetter(paraText.charAt(k))
				&& !Character.isDigit(paraText.charAt(k)))
			++k;
		if (k >= paraText.length())
			return null;
		return new org.luwrain.app.reader.doctree.Paragraph(
				new org.luwrain.app.reader.doctree.Run(paraText));
	}
*/
}

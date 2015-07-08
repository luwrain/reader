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
import java.io.IOException;
import java.math.BigInteger;

import org.luwrain.app.reader.doctree.Document;
import org.luwrain.app.reader.doctree.Node;
import org.apache.poi.xwpf.usermodel.*;

class Docx implements Filter
{
    private String fileName = "";
    private String wholeText;

    public Docx(String fileName)
    {
	this.fileName = fileName;
	if (fileName == null)
	    throw new NullPointerException("fileName may not be null");
    }

    @Override public Document constructDocument()
    {
    	FileInputStream  s = null;
    	try
    	{
    		File docFile=new File(fileName);
    		s = new FileInputStream(docFile.getAbsolutePath());
    		XWPFDocument doc = new XWPFDocument(s);
    		Document res = transform(doc);
    		s.close(); //closing fileinputstream
    		return res;
    	} catch (IOException e)
    	{
    		e.printStackTrace();
    		try
    		{
    			if (s != null) s.close();
    		}
    		catch (IOException ee) {}
    		return null;
    	}
    }

    private Document transform(XWPFDocument doc)
    {
		wholeText = ""; // упрощенное текстовое представление будет заполнено в процессе разбора
		LinkedList<Node> subnodes = new LinkedList<Node>();
		anyRangeAsParagraph(subnodes,doc.getBodyElements(),0);
		final Node root = new Node(Node.ROOT);
		root.subnodes = subnodes.toArray(new Node[subnodes.size()]);
		final Document res = new Document(root);
		return res;
    }

	/* рекурсивный метод, вызывается для любого места в документе, способного содержать несколько элементов, представляя их как список параграфов
	 * @param subnodes The list of nodes to get all new on current level
	 * @param range The range to look through
	 * @param lvl Current recurse level (must be zero for the root)
	 */
	private void anyRangeAsParagraph(LinkedList<Node> subnodes,
			List<IBodyElement> range, int lvl)
	{
		int i = 0;
		for (IBodyElement paragraph : range)
		{
			if (paragraph.getClass() == XWPFTable.class)
			{
				// We do this processing for the first cell only, skipping all others
				final Node table_node = new org.luwrain.app.reader.doctree.Table();
				subnodes.add(table_node);
				final LinkedList<Node> rows_subnodes = new LinkedList<Node>();
				
				final XWPFTable table = (XWPFTable) paragraph;
				wholeText+=table.getText();
				System.out.println(lvl + ", is a table: " + table.getRows().size() + " rows");
				int r = 0;
				for (final XWPFTableRow trow : table.getRows())
				{ // для каждой строки таблицы
					r++;
					// создаем элементы структуры Node и добавляем текущую ноду
					// в список потомка
					final Node rowtable_node = new Node(Node.TABLE_ROW);
					rows_subnodes.add(rowtable_node);
					final LinkedList<Node> cels_subnodes = new LinkedList<Node>();
					int c = 0;
					for (final XWPFTableCell cell : trow.getTableCells())
					{ // для каждой ячейки таблицы
						c++;
						// Creating a node for table cell
						final Node celltable_node = new Node(Node.TABLE_CELL);
						final LinkedList<Node> incell_subnodes = new LinkedList<Node>();
						cels_subnodes.add(celltable_node);
						System.out.print("* cell[" + r + "," + c + "]: ");
						anyRangeAsParagraph(incell_subnodes, cell.getBodyElements(), lvl + 1);
						celltable_node.subnodes = incell_subnodes.toArray(new Node[incell_subnodes.size()]);
						checkNodesNotNull(celltable_node.subnodes);
					} // for(cells);
					rowtable_node.subnodes = cels_subnodes.toArray(new Node[cels_subnodes.size()]);
					checkNodesNotNull(rowtable_node.subnodes);
				} // for(trows);
				table_node.subnodes = rows_subnodes.toArray(new Node[rows_subnodes.size()]);
				checkNodesNotNull(table_node.subnodes);
			} else
			{
				System.out.print(lvl + ", not table: ");
				parseParagraph(subnodes, paragraph);
			}
			i++;
		} // for(body elements)
	}

	// listInfo[id of list][level]=counter;
	public HashMap<BigInteger, HashMap<Integer, Integer>> listInfo = new HashMap<BigInteger, HashMap<Integer, Integer>>();
	public int lastLvl = -1;

	/*
	 * Анализирует тип параграфа и выделяет в соответствии с ним данные
	 * @param subnodes список нод на текущем уровне собираемой структуры, в этот список будут добавлены новые элементы
	 * @param paragraph элемент документа (параграф или элемент списка) или ячейка таблицы
	 */
	public void parseParagraph(LinkedList<Node> subnodes, IBodyElement element)
	{
		String className = element.getClass().getSimpleName();
		String paraText = "";
		if (element.getClass() == XWPFParagraph.class)
		{ // все есть параграф
			final XWPFParagraph paragraph = (XWPFParagraph) element;
			wholeText+=paragraph.getText();
			if (paragraph.getNumIlvl() != null)
			{ // параграф с установленным уровнем - элемент списка
				// создаем элементы структуры Node и добавляем текущую ноду в
				// список потомка
				Node node = new Node(Node.LIST_ITEM);
				subnodes.add(node);
				//
				BigInteger listId = paragraph.getNumID();
				int listLvl = paragraph.getNumIlvl().intValue();
				// если это новый список, то добавим пустой подсписок его
				// счетчиков
				if (!listInfo.containsKey(listId))
					listInfo.put(listId, new HashMap<Integer, Integer>());
				// если уровень списка уменьшился, то очищаем счетчики выше
				// уровнем
				if (lastLvl > listLvl)
				{
					for (Entry<Integer, Integer> lvls : listInfo.get(listId).entrySet())
						if (lvls.getKey() > listLvl)
							listInfo.get(listId).put(lvls.getKey(), 1);
				}
				lastLvl = listLvl;
				// если в списке счетчиков значения нет, то иннициализируем его
				// 0 (позже он будет обязательно увеличен на 1)
				if (!listInfo.get(listId).containsKey(listLvl))listInfo.get(listId).put(listLvl, 0);
				// так как это очередной элемент списка, то увеличиваем его
				// счетчик на 1
				listInfo.get(listId).put(listLvl,listInfo.get(listId).get(listLvl) + 1);
				// формируем строку-номер
				String numstr = "";
				for (int lvl = 0; lvl <= listLvl; lvl++) numstr += listInfo.get(listId).get(lvl) + ".";
				paraText = paragraph.getText().trim();
				System.out.println("LIST ENTRY:" + listLvl + ", " + listId + ", " + numstr + "[" + paraText + "]");
				LinkedList<Node> item_subnodes = new LinkedList<Node>();
				item_subnodes.add(new org.luwrain.app.reader.doctree.Paragraph(new org.luwrain.app.reader.doctree.Run(paraText)));
				node.subnodes = item_subnodes.toArray(new Node[item_subnodes.size()]);
			} else
			{
				paraText = paragraph.getText().trim();
				System.out.println("PARAGRAPH:[" + paraText + "]");
				subnodes.add(new org.luwrain.app.reader.doctree.Paragraph(new org.luwrain.app.reader.doctree.Run(paraText)));
			}
		} else
		{
			System.out.println(className);
			subnodes.add(new org.luwrain.app.reader.doctree.Paragraph(new org.luwrain.app.reader.doctree.Run(paraText)));
		}
	}

	private void checkNodesNotNull(Node[] nodes)
	{
		if (nodes == null)
			throw new NullPointerException("nodes is null");
		for (int i = 0; i < nodes.length; ++i)
			if (nodes[i] == null)
				throw new NullPointerException("nodes[" + i + "] is null");
	}

}

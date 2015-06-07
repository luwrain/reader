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

package org.luwrain.app.reader.i18n;

public class Ru implements org.luwrain.app.reader.Strings
{
    @Override public String appName()
    {
	return "Просмотр документов";
    }

    @Override public String tableIntroduction(int rows, int cols, String text)
    {
	return "Таблица Строк " + rows + " Столбцов " + cols + " " + text;
    }

    @Override public String tableCellIntroduction(int row, int col, String text)
    {
	if (col == 1)
return "Строка " + row + " " + text; else
return "Столбец " + col + " " + text;


    }

    @Override public String orderedListItemIntroduction(int index, String text)
    {
	if (index == 0)
	    return "Начало нумерованного списка " + text;
	    return "элемент списка " + index + " " + text;
    }

    @Override public String unorderedListItemIntroduction(int index, String text)
    {
	if (index == 0)
	    return "Начало ненумерованного списка " + text;
	return "Ненумерованный элемент " + text;
    }

    @Override public String errorOpeningFile()
    {
	return "Произошла ошибка открытия документа";
    }

    @Override public String errorFetching()
    {
	return "Доставка запрошенной страницы невозможна";
    }
}

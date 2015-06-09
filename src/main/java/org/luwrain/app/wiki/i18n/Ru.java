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

package org.luwrain.app.wiki.i18n;

public class Ru implements org.luwrain.app.wiki.Strings
{
    @Override public String appName()
    {
	return "Поиск в Википедии";
    }

    @Override public String querySuccess(int count)
    {
	return "Найдено статей: " + count;
    }

    @Override public String searchEn()
    {
	return "Поиск в английском языковом разделе";
    }

    @Override public String searchRu()
    {
	return "Поиск в русском языковом разделе";
    }

    @Override public String errorSearching()
    {
	return "При обработке запроса произошла ошибка";
    }

    @Override public String nothingFound()
    {
	return "Ничего не найдено";
    }

    @Override public String queryResults()
    {
	return "Результаты поиска";
    }

    @Override public String queryPopupName()
    {
	return "Поиск в Википедии";
    }

    @Override public String queryPopupPrefix()
    {
	return "Выражения для поиска:";
    }
}

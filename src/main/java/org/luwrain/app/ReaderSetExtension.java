/*
   Copyright 2012-2015 Michael Pozhidaev <michael.pozhidaev@gmail.com>

   This file is part of the LUWRAIN.

   LUWRAIN is free software; you can redistribute it and/or
   modify it under the terms of the GNU General Public
   License as published by the Free Software Foundation; either
   version 3 of the License, or (at your option) any later version.

   LUWRAIN is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
   General Public License for more details.
*/

package org.luwrain.app;

import java.net.*;
import org.luwrain.core.*;
import org.luwrain.popups.Popups;

import org.luwrain.app.reader.ReaderApp;
import org.luwrain.app.reader.DocInfo;
import org.luwrain.app.wiki.WikiApp;
import org.luwrain.app.reader.FormatsList;

public class ReaderSetExtension extends org.luwrain.core.extensions.EmptyExtension
{
    @Override public Command[] getCommands(Luwrain luwrain)
    {
	return new Command[]{

	    new Command(){
		@Override public String getName()
		{
		    return "reader";
		}
		@Override public void onCommand(Luwrain luwrain)
		{
		    luwrain.launchApp("reader");
		}
	    },

	    new Command(){
		@Override public String getName()
		{
		    return "reader-open-url";
		}
		@Override public void onCommand(Luwrain luwrain)
		{
		    final String url = Popups.simple(luwrain, "Страница", "Введите адрес страницы:", "");//FIXME:
		    if (url != null && !url.trim().isEmpty())
			luwrain.launchApp("reader", new String[]{"--URL", url});
		}
	    },

	    new Command(){
		@Override public String getName()
		{
		    return "reader-search-google";
		}
		@Override public void onCommand(Luwrain luwrain)
		{
		    final String query = Popups.simple(luwrain, "Поиск в Google", "Введите поисковый запрос:", "");//FIXME:
		    if (query != null && !query.trim().isEmpty())
		    {
			final String url = "http://www.google.ru/search?q=" + URLEncoder.encode(query) + "&hl=ru&ie=utf-8";
			luwrain.launchApp("reader", new String[]{"--URL", url});
		    }
		}
	    },


	    new Command(){
		@Override public String getName()
		{
		    return "wiki";
		}
		@Override public void onCommand(Luwrain luwrain)
		{
		    luwrain.launchApp("wiki");
		}
	    }};
    }

    @Override public Shortcut[] getShortcuts(Luwrain luwrain)
    {
	final Shortcut reader = new Shortcut() {
		@Override public String getName()
		{
		    return "reader";
		}
		@Override public Application[] prepareApp(String[] args)
		{
		    if (args == null || args.length < 1 || args.length > 2)
			return new Application[]{new ReaderApp()};
		    if (args.length > 2)
			return null;
		    if (args.length == 1)
		    {
			if (args[0] == null)
			    return null;
			return new Application[]{new ReaderApp(DocInfo.LOCAL, args[0])};
		    }
		    if (args.length == 2)
		    {
			if (args[0] == null || args[1] == null)
			    return null;
			if (args[0].equals("--URL"))
			    return new ReaderApp[]{new ReaderApp(DocInfo.URL, args[1])};
			return new Application[]{new ReaderApp(DocInfo.LOCAL, args[0], args[1])};
		    }
		    return null;
		}
	    };

	final Shortcut wiki = new Shortcut() {
		@Override public String getName()
		{
		    return "wiki";
		}
		@Override public Application[] prepareApp(String[] args)
		{
		    return new Application[]{new WikiApp()};
		}
	    };
	return new Shortcut[]{reader, wiki};
    }

    @Override public SharedObject[] getSharedObjects(Luwrain luwrain)
    {
	return new SharedObject[]{

	    new SharedObject(){
		@Override public String getName()
		{
		    return "luwrain.reader.formats";
		}
		@Override public Object getSharedObject()
		{
		    return FormatsList.getSupportedFormatsList();
		    }
		},

		    };
	}
}

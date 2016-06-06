/*
   Copyright 2012-2016 Michael Pozhidaev <michael.pozhidaev@gmail.com>

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
import org.luwrain.cpanel.Factory;

import org.luwrain.app.reader.ReaderApp;
import org.luwrain.app.reader.DocInfo;
import org.luwrain.app.wiki.WikiApp;
import org.luwrain.app.opds.OpdsApp;
import org.luwrain.app.narrator.NarratorApp;

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
		    final String url = Popups.simple(luwrain, "Open URL", "Enter the URL to open:", "");//FIXME:
		    if (url != null && !url.trim().isEmpty())
			luwrain.launchApp("reader", new String[]{"--URL", url.indexOf("://") >= 0?url:("http://" + url)});
		}
	    },

	    new Command(){
		@Override public String getName()
		{
		    return "open-url";
		}
		@Override public void onCommand(Luwrain luwrain)
		{
		    final String url = Popups.simple(luwrain, "Open URL", "Enter the URL to open:", "");//FIXME:
		    if (url != null && !url.trim().isEmpty())
			luwrain.launchApp("reader", new String[]{"--URL", url.indexOf("://") >= 0?url:("http://" + url)});
		}
	    },


	    new Command(){
		@Override public String getName()
		{
		    return "reader-search-google";
		}
		@Override public void onCommand(Luwrain luwrain)
		{
		    final String query = Popups.simple(luwrain, "Search in Google", "Enter the expression to search in Google:", "");//FIXME:
		    if (query != null && !query.trim().isEmpty())
		    {
			final String url = "http://www.google.ru/search?q=" + URLEncoder.encode(query) + "&hl=en&ie=utf-8";
			luwrain.launchApp("reader", new String[]{"--URL", url});
		    }
		}
	    },

	    new Command(){
		@Override public String getName()
		{
		    return "reader-luwrain-homepage";
		}
		@Override public void onCommand(Luwrain luwrain)
		{
			luwrain.launchApp("reader", new String[]{"--URL", "http://luwrain.org/?mode=adapted&lang=en"});
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
	    },

	    new Command(){
		@Override public String getName()
		{
		    return "wiki-current-word";
		}
		@Override public void onCommand(Luwrain luwrain)
		{
		    final String word = luwrain.currentAreaWord(true);
		    if (word != null && !word.trim().isEmpty())
			luwrain.launchApp("wiki", new String[]{word});
		}
	    },

	    new Command(){
		@Override public String getName()
		{
		    return "opds";
		}
		@Override public void onCommand(Luwrain luwrain)
		{
		    luwrain.launchApp("opds");
		}
	    },

	    new Command(){
		@Override public String getName()
		{
		    return "narrator";
		}
		@Override public void onCommand(Luwrain luwrain)
		{
		    luwrain.launchApp("narrator");
		}
	    },

};
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
		    if (args == null || args.length < 1)
			return new Application[]{new ReaderApp()};
		    final DocInfo docInfo = new DocInfo();
		    if (docInfo.load(args))
			return new Application[]{new ReaderApp(docInfo)};
		    Log.warning("reader", "unable to parse command line argument for ReaderApp, starting in initial state");
			return new Application[]{new ReaderApp()};

		}
	    };

	final Shortcut wiki = new Shortcut() {
		@Override public String getName()
		{
		    return "wiki";
		}
		@Override public Application[] prepareApp(String[] args)
		{
		    if (args != null && args.length == 1 && args[0] != null)
		    return new Application[]{new WikiApp(args[0])};
		    return new Application[]{new WikiApp()};
		}
	    };

	final Shortcut opds = new Shortcut() {
		@Override public String getName()
		{
		    return "opds";
		}
		@Override public Application[] prepareApp(String[] args)
		{
		    return new Application[]{new OpdsApp()};
		}
	    };

	final Shortcut narrator = new Shortcut() {
		@Override public String getName()
		{
		    return "narrator";
		}
		@Override public Application[] prepareApp(String[] args)
		{
		    if (args == null || args.length != 2)
		    return new Application[]{new NarratorApp()};
		    if (args[0].equals("--TEXT"))
			return new Application[]{new NarratorApp(args[1] != null?args[1]:"")};
		    return new Application[]{new NarratorApp()};
		}
	    };

	return new Shortcut[]{reader, wiki, opds, narrator};
    }

    @Override public Factory[] getControlPanelFactories(Luwrain luwrain)
    {
	NullCheck.notNull(luwrain, "luwrain");
	return new Factory[]{new org.luwrain.app.narrator.ControlPanelFactory(luwrain)};
    }
}

/*
   Copyright 2012-2018 Michael Pozhidaev <michael.pozhidaev@gmail.com>

   This file is part of LUWRAIN.

   LUWRAIN is free software; you can redistribute it and/or
   modify it under the terms of the GNU General Public
   License as published by the Free Software Foundation; either
   version 3 of the License, or (at your option) any later version.

   LUWRAIN is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
   General Public License for more details.
*/

package org.luwrain.app.reader;

import java.net.*;
import java.util.*;

import org.luwrain.core.*;
import org.luwrain.popups.Popups;
import org.luwrain.cpanel.Factory;

import org.luwrain.app.wiki.WikiApp;

public final class Extension extends org.luwrain.core.extensions.EmptyExtension
{
    private final Set<String> queries = new TreeSet();
    
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
		    return "reader-open";
		}
		@Override public void onCommand(Luwrain luwrain)
		{
		    final Strings strings = (Strings)luwrain.i18n().getStrings(Strings.NAME);
		    if (strings == null)
			return;
		    final String url = Popups.simple(luwrain, strings.openUrlPopupName(), strings.openUrlPopupPrefix(), "http://");
		    if (url != null && !url.trim().isEmpty())
			luwrain.launchApp("reader", new String[]{url.indexOf("://") >= 0?url:("http://" + url)});
		}
	    },

	    //The same as reader-open
	    new Command(){
		@Override public String getName()
		{
		    return "open-url";
		}
		@Override public void onCommand(Luwrain luwrain)
		{
		    final Strings strings = (Strings)luwrain.i18n().getStrings(Strings.NAME);
		    if (strings == null)
			return;
		    final String url = Popups.simple(luwrain, strings.openUrlPopupName(), strings.openUrlPopupPrefix(), "http://");
		    if (url != null && !url.trim().isEmpty())
			luwrain.launchApp("reader", new String[]{url.indexOf("://") >= 0?url:("http://" + url)});
		}
	    },

	    new Command(){
		@Override public String getName()
		{
		    return "reader-search-google";
		}
		@Override public void onCommand(Luwrain luwrain)
		{
		    final Strings strings = (Strings)luwrain.i18n().getStrings(Strings.NAME);
		    if (strings == null)
			return;
		    final String query = Popups.simple(luwrain, strings.searchGooglePopupName(), strings.searchGooglePopupPrefix(), "");
		    if (query != null && !query.trim().isEmpty())
			luwrain.launchApp("reader", new String[]{constructGoogleUrl(luwrain.getRegistry(), query)});
		}
	    },

	    //The same as reader-search-google
	    new Command(){
		@Override public String getName()
		{
		    return "reader-search";
		}
		@Override public void onCommand(Luwrain luwrain)
		{
		    final Strings strings = (Strings)luwrain.i18n().getStrings(Strings.NAME);
		    if (strings == null)
			return;
		    final String query = Popups.simple(luwrain, strings.searchGooglePopupName(), strings.searchGooglePopupPrefix(), "");
		    if (query != null && !query.trim().isEmpty())
			luwrain.launchApp("reader", new String[]{constructGoogleUrl(luwrain.getRegistry(), query)});
		}
	    },

	    new Command(){
		@Override public String getName()
		{
		    return "web-open";
		}
		@Override public void onCommand(Luwrain luwrain)
		{
		    final Strings strings = (Strings)luwrain.i18n().getStrings(Strings.NAME);
		    if (strings == null)
			return;
		    final String query = Popups.editWithHistory(luwrain, strings.openAutodetectPopupName(), strings.openAutodetectPopupPrefix(), "", queries, Popups.DEFAULT_POPUP_FLAGS);
		    if (query == null || query.trim().isEmpty())
			return;
		    if (query.trim().toLowerCase().startsWith("http://") || query.trim().toLowerCase().startsWith("https://"))
			luwrain.launchApp("reader", new String[]{query}); else
			luwrain.launchApp("reader", new String[]{constructGoogleUrl(luwrain.getRegistry(), query)});
		}
	    },

	    new Command(){
		@Override public String getName()
		{
		    return "reader-luwrain-homepage";
		}
		@Override public void onCommand(Luwrain luwrain)
		{
		    luwrain.launchApp("reader", new String[]{"http://luwrain.org/?mode=adapted&lang=en"});
		}
	    },

	    new Command(){
		@Override public String getName()
		{
		    return "wiki";
		}
		@Override public void onCommand(Luwrain luwrain)
		{
		    final String word = luwrain.getActiveAreaText(Luwrain.AreaTextType.WORD, true);
		    if (word != null && !word.trim().isEmpty())
			luwrain.launchApp("wiki", new String[]{word.trim()}); else
			luwrain.launchApp("wiki");
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

	};
    }
    
    @Override public Shortcut[] getShortcuts(Luwrain luwrain)
    {
	final Shortcut reader = new Shortcut() {
		@Override public String getExtObjName()
		{
		    return "reader";
		}
		@Override public Application[] prepareApp(String[] args)
		{
		    if (args == null || args.length < 1)
			return new Application[]{new App()};
		    if (args.length == 1)
			return new Application[]{new App(args[0], "")};
		    if (args.length == 2)
			return new Application[]{new App(args[0], args[1])};
		    return new Application[]{new App()};

		}
	    };

	final Shortcut wiki = new Shortcut() {
		@Override public String getExtObjName()
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
		@Override public String getExtObjName()
		{
		    return "opds";
		}
		@Override public Application[] prepareApp(String[] args)
		{
		    return new Application[]{new org.luwrain.app.opds.App()};
		}
	    };

	return new Shortcut[]{reader, wiki, opds};
    }

    private String constructGoogleUrl(Registry registry, String query)
    {
	NullCheck.notNull(registry, "registry");
	NullCheck.notEmpty(query, "query");
	final Settings sett = createSettings(registry);
	return "http://www.google.ru/search?q=" + URLEncoder.encode(query) + "&hl=" + sett.getGoogleLang("en") + "&ie=utf-8";
    }

    interface Settings
    {
	String getGoogleLang(String defValue);
	void setGoogleLang(String value);
    }

    static Settings createSettings(Registry registry)
    {
	NullCheck.notNull(registry, "registry");
	return RegistryProxy.create(registry, "/org/luwrain/app/reader", Settings.class);
    }
}

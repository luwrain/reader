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

package org.luwrain.app;

import org.luwrain.core.*;

import org.luwrain.app.reader.ReaderApp;
import org.luwrain.app.wiki.WikiApp;

public class ReaderExtension extends org.luwrain.core.extensions.EmptyExtension
{
    @Override public Command[] getCommands(Luwrain luwrain)
    {
	Command reader = new Command(){
		@Override public String getName()
		{
		    return "reader";
		}
		@Override public void onCommand(Luwrain luwrain)
		{
		    luwrain.launchApp("reader");
		}
	    };
	Command wiki = new Command(){
		@Override public String getName()
		{
		    return "wiki";
		}
		@Override public void onCommand(Luwrain luwrain)
		{
		    luwrain.launchApp("wiki");
		}
	    };
	return new Command[]{reader, wiki};
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
		    if (args == null)
			throw new NullPointerException("args may not be null");
		    if (args.length < 1 || args.length > 2)
			return new Application[]{new ReaderApp()};
		    if (args.length == 1)
			return new Application[]{new ReaderApp(ReaderApp.LOCAL, args[0])};
		    if (args[0].equals("--URL"))
			return new Application[]{new ReaderApp(ReaderApp.URL, args[1])};
		    if (args[0].equals("--LOCAL"))
			return new Application[]{new ReaderApp(ReaderApp.LOCAL, args[1])};
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
		    return new Application[]{new WikiApp()};
		}
	    };
	return new Shortcut[]{reader, wiki};
    }

    @Override public void i18nExtension(Luwrain luwrain, I18nExtension i18nExt)
    {
	i18nExt.addCommandTitle("en", "reader", "Reader");
	i18nExt.addCommandTitle("ru", "reader", "Просмотр документов");

	i18nExt.addCommandTitle("en", "wiki", "Search in Wikipedia");
	i18nExt.addCommandTitle("ru", "wiki", "Поиск в Википедии");

	i18nExt.addStrings("ru", ReaderApp.STRINGS_NAME, new org.luwrain.app.reader.i18n.Ru());
	i18nExt.addStrings("ru", WikiApp.STRINGS_NAME, new org.luwrain.app.wiki.i18n.Ru());
    }
}

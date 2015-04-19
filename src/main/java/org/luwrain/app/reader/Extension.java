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

package org.luwrain.app.reader;

import org.luwrain.core.Application;
import org.luwrain.core.Shortcut;
import org.luwrain.core.Command;
import org.luwrain.core.CommandEnvironment;
import org.luwrain.core.I18nExtension;
import org.luwrain.core.Luwrain;
import org.luwrain.core.Registry;

public class Extension extends org.luwrain.core.EmptyExtension
{
    @Override public Command[] getCommands(CommandEnvironment env)
    {
	Command res = new Command(){
		@Override public String getName()
		{
		    return "reader";
		}
		@Override public void onCommand(CommandEnvironment env)
		{
		    env.launchApp("reader");
		}
	    };
	return new Command[]{res};
    }

    @Override public Shortcut[] getShortcuts()
    {
	Shortcut res = new Shortcut() {
		@Override public String getName()
		{
		    return "reader";
		}
		@Override public Application[] prepareApp(String[] args)
		{
		    return new Application[]{new ReaderApp()};
		}
	    };
	return new Shortcut[]{res};
    }

    @Override public void i18nExtension(I18nExtension i18nExt)
    {
	i18nExt.addCommandTitle("en", "reader", "Reader");
	i18nExt.addCommandTitle("ru", "reader", "Просмотр документов");
	i18nExt.addStrings("ru", ReaderApp.STRINGS_NAME, new org.luwrain.app.reader.i18n.Ru());
    }
}

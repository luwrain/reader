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

package org.luwrain.app.opds;

import org.luwrain.core.*;

final class RemoteLibrary implements Comparable
{
    final String title;
    final String url;

    RemoteLibrary(Registry registry, String regPath)
    {
	NullCheck.notNull(registry, "registry");
	NullCheck.notEmpty(regPath, "regPath");
	final Settings sett = RegistryProxy.create(registry, regPath, Settings.class);
	title = sett.getTitle("");
	url = sett.getUrl("");
    }

    @Override public String toString()
    {
	return !title.trim().isEmpty()?title:url;
    }

    @Override public int compareTo(Object o)
    {
	if (o == null || !(o instanceof RemoteLibrary))
	    return 0;
	return title.compareTo(((RemoteLibrary)o).title);
    }

    interface Settings
    {
	String getTitle(String defValue);
	void setTitle(String value);
	String getUrl(String defValue);
	void setUrl(String value);
    }
    }

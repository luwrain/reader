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

package org.luwrain.app.opds;

import org.luwrain.core.*;

class RemoteLibrary implements Comparable
{
interface RegistryParams 
{
    String getTitle();
    void setTitle(String value);
    String getUrl();
    void setUrl(String value);
}

    String title;
    String url;

    boolean init(Registry registry, String regPath)
    {
	NullCheck.notNull(registry, "registry");
	NullCheck.notNull(regPath, "regPath");
	try {
	    final RegistryParams params = RegistryProxy.create(registry, regPath, RegistryParams.class);
	    title = params.getTitle();
	    url = params.getUrl();
	    return title != null && url != null && !url.trim().isEmpty();
	}
	catch(Exception e)
	{
	    e.printStackTrace();
	    return false;
	}
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
}

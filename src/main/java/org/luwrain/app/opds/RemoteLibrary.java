/*
   Copyright 2012-2021 Michael Pozhidaev <msp@luwrain.org>

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

import com.google.gson.annotations.*;

import org.luwrain.core.*;

final class RemoteLibrary implements Comparable
{
    @SerializedName("title")
    String title = null;

    @SerializedName("url")
    String url = null;

    @Override public String toString()
    {
	if (title != null && !title.isEmpty())
	    return title;
	if (url != null && !url.isEmpty())
	    return url;
	return "-";
	    }

    @Override public int compareTo(Object o)
    {
	if (o == null || !(o instanceof RemoteLibrary))
	    return 0;
	if (title == null || ((RemoteLibrary)o).title == null)
	    return 0;
	return title.compareTo(((RemoteLibrary)o).title);
    }
    }

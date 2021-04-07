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

import java.net.*;

import org.luwrain.core.*;

final class HistoryItem
{
    final URL url;
    final Opds.Entry[] entries;
    Opds.Entry selected = null;

    HistoryItem(URL url, Opds.Entry[] entries)
    {
	NullCheck.notNull(url, "url");
	NullCheck.notNullItems(entries, "entries");
	this.url = url;
	this.entries = entries;
    }
}

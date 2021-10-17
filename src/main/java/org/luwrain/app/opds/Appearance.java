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

import java.util.*;

import org.luwrain.core.*;
import org.luwrain.controls.*;
import org.luwrain.app.opds.Opds.Entry;

import static org.luwrain.core.DefaultEventResponse.*;

final class Appearance implements ListArea.Appearance<Opds.Entry>
{
    private final Luwrain luwrain;
    private final Strings strings;

    Appearance(Luwrain luwrain, Strings strings)
    {
	NullCheck.notNull(luwrain, "luwrain");
	NullCheck.notNull(strings, "strings");
	this.luwrain = luwrain;
	this.strings = strings;
    }

    @Override public void announceItem(Opds.Entry entry, Set<Flags> flags)
    {
	NullCheck.notNull(entry, "entry");
	NullCheck.notNull(flags, "flags");
	luwrain.setEventResponse(listItem(getString(entry), Suggestions.CLICKABLE_LIST_ITEM));
    }

    @Override public String getScreenAppearance(Opds.Entry entry, Set<Flags> flags)
    {
	NullCheck.notNull(entry, "entry");
	NullCheck.notNull(flags, "flags");
	if (Utils.isCatalogOnly(entry))
	    return "[" + getString(entry) + "]";
	return " " + getString(entry) + " ";
    }

    @Override public int getObservableLeftBound(Opds.Entry entry)
    {
	return entry != null?1:0;
    }

    @Override public int getObservableRightBound(Opds.Entry entry)
    {
	if (entry == null)
	    return 0;
	return getString(entry).length() + 1;
    }

    static private String getString(Entry entry)
    {
	NullCheck.notNull(entry, "entry");
	if (entry.authors == null || entry.authors.length == 0)
	    return entry.title;
	return entry.authors[0].name + " - " + entry.title;
    }
}

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

import java.util.*;

import org.luwrain.core.*;
import org.luwrain.controls.*;
import org.luwrain.app.opds.Opds.Entry;

class Appearance implements ListArea.Appearance
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

    @Override public void announceItem(Object item, Set<Flags> flags)
    {
	NullCheck.notNull(item, "item");
	NullCheck.notNull(flags, "flags");
	luwrain.playSound(Sounds.LIST_ITEM);
	if (item instanceof Opds.Entry)
	{
	    final Opds.Entry entry = (Opds.Entry)item;
	    if (Base.isCatalogOnly(entry) && flags.contains(Flags.BRIEF))
		luwrain.say(getString(entry) + " " + strings.catalog()); else
		luwrain.say(getString(entry));
	    return;
	}
		luwrain.say(item.toString());
    }

    @Override public String getScreenAppearance(Object item, Set<Flags> flags)
    {
	NullCheck.notNull(item, "item");
	NullCheck.notNull(flags, "flags");
	if (item instanceof Opds.Entry)
	{
	    final Opds.Entry entry = (Opds.Entry)item;
	    if (Base.isCatalogOnly(entry))
		return "[" + getString(entry) + "]";
	    return " " + getString(entry) + " ";
	}
	return " " + item.toString() + " ";
    }

    @Override public int getObservableLeftBound(Object item)
    {
	return item != null?1:0;
    }

    @Override public int getObservableRightBound(Object item)
    {
	if (item == null)
	    return 0;
	if (item instanceof Entry)
	    return getString((Entry)item).length() + 1;
	return item.toString().length() + 1;    
}

    static private String getString(Entry entry)
    {
	NullCheck.notNull(entry, "entry");
	if (entry.authors == null || entry.authors.length == 0)
	    return entry.title;
	return entry.authors[0].name + " - " + entry.title;
    }
}

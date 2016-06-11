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

package org.luwrain.app.opds;

import java.util.*;

import org.luwrain.util.Opds;
import org.luwrain.core.*;
import org.luwrain.controls.*;

class Appearance implements ListArea.Appearance
{
    private Luwrain luwrain;
    private Strings strings;

Appearance(Luwrain luwrain, Strings strings)
    {
	this.luwrain = luwrain;
	this.strings = strings;
	NullCheck.notNull(luwrain, "luwrain");
	NullCheck.notNull(strings, "strings");
    }

    @Override public void announceItem(Object item, Set<Flags> flags)
    {
	NullCheck.notNull(item, "item");
	NullCheck.notNull(flags, "flags");
	luwrain.playSound(Sounds.LIST_ITEM);
	if (item instanceof Opds.Entry)
	{
	    final Opds.Entry entry = (Opds.Entry)item;
	    if (entry.isCatalogOnly() && flags.contains(Flags.BRIEF))
		luwrain.say(entry.toString() + " " + strings.catalog()); else
		luwrain.say(entry.toString());
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
	    if (entry.isCatalogOnly())
		return "[" + entry.toString() + "]";
		return " " + entry.toString() + " ";
	}
	return " " + item.toString() + " ";
    }

    @Override public int getObservableLeftBound(Object item)
    {
	return item != null?1:0;
    }

    @Override public int getObservableRightBound(Object item)
    {
	return item != null?item.toString().length() + 1:0;
    }
}

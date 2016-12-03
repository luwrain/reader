
package org.luwrain.app.opds;

import java.util.*;

import org.luwrain.core.*;
import org.luwrain.controls.*;
import org.luwrain.util.Opds;
import org.luwrain.util.Opds.Entry;

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

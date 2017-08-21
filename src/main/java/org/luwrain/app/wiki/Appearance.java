
package org.luwrain.app.wiki;

import java.util.*;

import org.luwrain.core.*;
import org.luwrain.controls.*;

class Appearance implements ListArea.Appearance
{
    private Luwrain luwrain;
    private Strings strings;

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
	if (flags.contains(Flags.BRIEF) && item instanceof Page)
	{
	    final Page page = (Page)item ;
	    luwrain.say(page.title);
	    return;
	}
	luwrain.say(item.toString());
    }

    @Override public String getScreenAppearance(Object item, Set<Flags> flags)
    {
	NullCheck.notNull(item, "item");
	NullCheck.notNull(flags, "flags");
	return item.toString();
    }

    @Override public int getObservableLeftBound(Object item)
    {
	return 0;
    }

    @Override public int getObservableRightBound(Object item)
    {
	return getScreenAppearance(item, EnumSet.noneOf(Flags.class)).length();
    }
}

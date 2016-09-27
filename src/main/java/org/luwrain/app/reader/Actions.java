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

package org.luwrain.app.reader;

import java.util.*;

import org.luwrain.core.*;
import org.luwrain.core.events.*;
import org.luwrain.doctree.*;
import org.luwrain.doctree.loading.*;

interface Actions
{
    static Action[] getReaderAreaActions(Strings strings, boolean hasDocument)
    {
	NullCheck.notNull(strings, "strings");
	final LinkedList<Action> res = new LinkedList<Action>();
	res.add(new Action("open-file", strings.actionOpenFile(), new KeyboardEvent(KeyboardEvent.Special.F5)));
	res.add(new Action("open-url", strings.actionOpenUrl(), new KeyboardEvent(KeyboardEvent.Special.F6)));
	if (hasDocument)
	{
	    res.add(new Action("open-in-narrator", strings.actionOpenInNarrator(), new KeyboardEvent(KeyboardEvent.Special.F8)));
	    res.add(new Action("change-format", strings.actionChangeFormat(), new KeyboardEvent(KeyboardEvent.Special.F9)));
	    res.add(new Action("change-charset", strings.actionChangeCharset(), new KeyboardEvent(KeyboardEvent.Special.F10)));
	    res.add(new Action("book-mode", strings.actionBookMode()));
	    res.add(new Action("doc-mode", strings.actionDocMode()));
	    res.add(new Action("play-audio", strings.actionPlayAudio(), new KeyboardEvent(KeyboardEvent.Special.F7)));
	    res.add(new Action("info", strings.actionInfo(), new KeyboardEvent(KeyboardEvent.Special.F8)));
	}
	return res.toArray(new Action[res.size()]);
    }

    static Action[] getTreeAreaActions(Strings strings, boolean hasDocument)
    {
	NullCheck.notNull(strings, "strings");
	return getReaderAreaActions(strings, hasDocument);
    }

    static Action[] getNotesAreaActions(Strings strings, boolean hasDocument)
    {
	NullCheck.notNull(strings, "strings");
	final LinkedList<Action> res = new LinkedList<Action>();
	res.add(new Action("add-note", strings.actionAddNote(), new KeyboardEvent(KeyboardEvent.Special.INSERT)));
	for(Action a: getReaderAreaActions(strings, hasDocument))
	    res.add(a);
	return res.toArray(new Action[res.size()]);
    }

    static boolean onPlayAudio(Base base, DoctreeArea area)
    {
	NullCheck.notNull(base, "base");
	NullCheck.notNull(area, "area");
	if (!base.isInBookMode())
	    return false;
	final String[] ids = area.getHtmlIds();
	if (ids == null || ids.length < 1)
	    return false;
	return base.playAudio(area, ids);
    }
}

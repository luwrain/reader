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

package org.luwrain.app.reader;

import java.util.*;

import org.luwrain.core.*;
import org.luwrain.core.events.*;

final class ActionLists
{
    private final Luwrain luwrain;
    private final Strings strings;
    private final Base base;

    ActionLists(Luwrain luwrain, Base base, Strings strings)
    {
	NullCheck.notNull(luwrain, "luwrain");
	NullCheck.notNull(base, "base");
	NullCheck.notNull(strings, "strings");
	this.luwrain = luwrain;
	this.base = base;
	this.strings = strings;
    }

    Action[] getReaderActions()
    {
	final List<Action> res = new LinkedList();
	if (base.hasDocument())
	{
	    res.add(new Action("save-bookmark", strings.actionSaveBookmark(), new KeyboardEvent(KeyboardEvent.Special.F2)));
	    res.add(new Action("restore-bookmark", strings.actionRestoreBookmark(), new KeyboardEvent(KeyboardEvent.Special.F2, EnumSet.of(KeyboardEvent.Modifiers.SHIFT))));
	    if (!base.isInBookMode() && base.getContentType().equals(ContentTypes.TEXT_PLAIN_DEFAULT))
	    {
		res.add(new Action("change-charset", strings.actionChangeCharset(), new KeyboardEvent(KeyboardEvent.Special.F10)));
		res.add(new Action("change-text-para-style", strings.actionChangeTextParaStyle()));
	    }
	    if (base.isInBookMode())
	    {
		res.add(new Action("show-sections-tree", strings.actionShowSectionsTree(), new KeyboardEvent(KeyboardEvent.Special.F5)));
		res.add(new Action("show-notes", strings.actionShowNotes(), new KeyboardEvent(KeyboardEvent.Special.F6)));
	    }
	}
	res.add(new Action("open-file", strings.actionOpenFile(), new KeyboardEvent(KeyboardEvent.Special.F3, EnumSet.of(KeyboardEvent.Modifiers.SHIFT))));
	res.add(new Action("open-url", strings.actionOpenUrl(), new KeyboardEvent(KeyboardEvent.Special.F4, EnumSet.of(KeyboardEvent.Modifiers.SHIFT))));
	return res.toArray(new Action[res.size()]);
    }

        Action[] getTreeAreaActions(boolean hasDocument)
    {
	return new Action[0];
    }

Action[] getNotesAreaActions(boolean hasDocument)
    {
	final LinkedList<Action> res = new LinkedList<Action>();
	res.add(new Action("add-note", strings.actionAddNote(), new KeyboardEvent(KeyboardEvent.Special.INSERT)));
	res.add(new Action("delete-note", strings.actionDeleteNote(), new KeyboardEvent(KeyboardEvent.Special.DELETE)));
	return res.toArray(new Action[res.size()]);
    }

}

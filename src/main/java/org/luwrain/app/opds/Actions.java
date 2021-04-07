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
import java.util.*;

import org.luwrain.core.*;
import org.luwrain.controls.*;

final class Actions
{
    private final Luwrain luwrain;
    private final App app;
    private final Base base;
    private final Strings strings;

    Actions(Luwrain luwrain, App app, Base base, Strings strings)
    {
	NullCheck.notNull(luwrain, "luwrain");
	NullCheck.notNull(app, "app");
	NullCheck.notNull(base, "base");
	NullCheck.notNull(strings, "strings");
	this.luwrain = luwrain;
	this.app = app;
	this.base = base;
	this.strings = strings;
    }


    boolean onListClick(ListArea listArea, Object obj)
    {
	NullCheck.notNull(listArea, "listArea");
	if (obj == null || !(obj instanceof Opds.Entry))
	    return false;
	final Opds.Entry entry = (Opds.Entry)obj;
try {
		base.onEntry(app, entry);
	    listArea.refresh();
	    return true;
	}
	catch (MalformedURLException e)
	{
	    luwrain.message(strings.badUrl(e.getMessage()), Luwrain.MessageType.ERROR);
	    return true;
	}
    }

    boolean onListProperties(ListArea detailsArea, Object obj)
    {
	NullCheck.notNull(detailsArea, "detailsArea");
	if (obj == null || !(obj instanceof Opds.Entry))
	    return false;
	final Opds.Entry entry = (Opds.Entry)obj;
	final List<PropertiesItem> items = new LinkedList();
	for(Opds.Link l: entry.links)
	{
	    final URL url = base.prepareUrl(l.url);
	    if (url != null)
		items.add(new PropertiesItem(url.toString(), l.type));
	}
	final ListUtils.FixedModel model = (ListUtils.FixedModel)detailsArea.getListModel();
	model.setItems(items.toArray(new PropertiesItem[items.size()]));
	    luwrain.setActiveArea(detailsArea);
	return true;
    }

    boolean onLinkClick(Object obj)
    {
	if (obj == null || !(obj instanceof PropertiesItem))
	    return false;
	final PropertiesItem item = (PropertiesItem)obj;
	base.launchReader(item.url, item.contentType);
	return true;
    }
}

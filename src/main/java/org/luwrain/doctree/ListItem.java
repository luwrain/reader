/*
   Copyright 2012-2016 Michael Pozhidaev <michael.pozhidaev@gmail.com>
   Copyright 2015-2016 Roman Volovodov <gr.rPman@gmail.com>

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

package org.luwrain.reader;

public class ListItem extends Node
{
    ListItem()
    {
	super(Node.Type.LIST_ITEM);
    }

    public int getListItemIndex()
    {
	return getIndexInParentSubnodes();
    }

    public boolean isListOrdered()
    {
	return parentNode.type == Node.Type.ORDERED_LIST;
    }

    public int getListTotalItemCount()
    {
	if (parentNode == null || parentNode.subnodes == null)
	    return 0;
	return parentNode.subnodes.length;
    }
}

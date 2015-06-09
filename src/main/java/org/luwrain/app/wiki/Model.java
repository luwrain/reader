/*
   Copyright 2012-2015 Michael Pozhidaev <msp@altlinux.org>

   This file is part of the Luwrain.

   Luwrain is free software; you can redistribute it and/or
   modify it under the terms of the GNU General Public
   License as published by the Free Software Foundation; either
   version 3 of the License, or (at your option) any later version.

   Luwrain is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
   General Public License for more details.
*/

package org.luwrain.app.wiki;

import org.luwrain.controls.*;

class Model implements ListModel
{
    private Object[] objs = new Object[0];

    public void setObjects(Object[] objs)
    {
	if (objs == null)
	    throw new NullPointerException("objs may not be null");
	this.objs = objs;
    }

    @Override public int getItemCount()
    {
	return objs != null?objs.length:0;
    }

    @Override public Object getItem(int index)
    {
	return objs != null?objs[index]:null;
    }

    @Override public void refresh()
    {
    }

    @Override public boolean toggleMark(int index)
    {
	return false;
    }
}

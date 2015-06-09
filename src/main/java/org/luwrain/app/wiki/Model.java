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
    private Page[] pages = new Page[0];

    public void setPages(Page[] pages)
    {
	if (pages == null)
	    throw new NullPointerException("pages may not be null");
	this.pages = pages;
    }

    @Override public int getItemCount()
    {
	return pages != null?pages.length:0;
    }

    @Override public Object getItem(int index)
    {
	return pages != null?pages[index]:null;
    }

    @Override public void refresh()
    {
    }

    @Override public boolean toggleMark(int index)
    {
	return false;
    }
}

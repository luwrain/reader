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

package org.luwrain.app.wiki;

import org.luwrain.core.*;

class Page
{
    final String lang;
    final String title;
    final String comment;

    Page(String lang, String title, String comment)
    {
	NullCheck.notNull(lang, "lang");
	NullCheck.notNull(title, "title");
	NullCheck.notNull(comment, "comment");
	this.lang = lang;
	this.title = title;
	this.comment = comment;
    }

    @Override public String toString()
    {
	if (comment.trim().isEmpty())
	    return title;
	return title + ", " + comment;
    }
}

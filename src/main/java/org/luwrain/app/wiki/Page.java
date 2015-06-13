/*
   Copyright 2012-2015 Michael Pozhidaev <michael.pozhidaev@gmail.com>

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

class Page
{
    private String lang;
    private String title = "";
    private String comment = "";

    public Page(String lang,
		String title,
		String comment)
    {
	this.lang = lang;
	this.title = title;
	this.comment = comment;
	if (lang == null)
	    throw new NullPointerException("lang may not be null");
	if (title == null)
	    throw new NullPointerException("title may not be null");
	if (comment == null)
	    throw new NullPointerException("comment may not be null");
    }

    public String title()
    {
	return title;
    }

    public String comment()
    {
	return comment;
    }

    @Override public String toString()
    {
	if (comment.trim().isEmpty())
	    return title;
	return title + ", " + comment;
    }

    public String lang()
    {
	return lang;
    }
}

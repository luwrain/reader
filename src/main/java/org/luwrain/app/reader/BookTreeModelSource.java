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
import org.luwrain.controls.*;
import org.luwrain.doctree.*;
import org.luwrain.app.reader.books.*;

class BookTreeModelSource implements CachedTreeModelSource
{
    private String root;
    private Book.Section[] sections;

    BookTreeModelSource(String root, Book.Section[] sections)
    {
	NullCheck.notNull(root, "root");
	NullCheck.notNullItems(sections, "sections");
	this.root = root;
	this.sections = sections;
    }

    void setSections(Book.Section[] sections)
    {
	NullCheck.notNullItems(sections, "sections");
	this.sections = sections;
    }

    @Override public Object getRoot()
    {
	return root;
    }

    @Override public Object[] getChildObjs(Object obj)
    {
	final LinkedList res = new LinkedList();
	if (obj == root)
	{
	    for(Book.Section s: sections)
		if (s.level == 1)
		    res.add(s);
	} else
	{
	    int i = 0;
	    for(i = 0;i < sections.length;++i)
		if (sections[i] == obj)
		    break;
	    if (i < sections.length)
	    {
		final Book.Section sect = sections[i];
		for(int k = i + 1;k < sections.length;++k)
		{
		    if (sections[k].level <= sect.level)
			break;
		    if (sections[k].level == sect.level + 1)
			res.add(sections[k]);
		}
	    }
	}
	return res.toArray(new Object[res.size()]);
    }
}

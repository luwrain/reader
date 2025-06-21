/*
   Copyright 2012-2021 Michael Pozhidaev <msp@luwrain.org>
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

package org.luwrain.app.reader;

import java.util.*;

import com.google.gson.*;
import com.google.gson.annotations.*;

import org.luwrain.core.*;
import org.luwrain.io.api.books.v1.*;

final class Attributes
{
    private final Gson gson = new Gson();
    private final Settings sett;
    private final Books books;

    Attributes(Settings sett)
    {
	NullCheck.notNull(sett, "sett");
	this.sett = sett;
	Books b = gson.fromJson(sett.getAttributes(""), Books.class);
	if (b == null)
	    b = new Books();
	this.books = b;
	if (books.attrs == null)
	    books.attrs = new HashMap();
    }

    List<Note> getBookNotes(String bookId)
    {
	NullCheck.notEmpty(bookId, "bookId");
	if (!books.attrs.containsKey(bookId))
	{
	    final Attrs attrs = new Attrs();
	    attrs.notes = new ArrayList();
	    books.attrs.put(bookId, attrs);
	    save();
	    return attrs.notes;
	}
	final Attrs a = books.attrs.get(bookId);
	final List<Note> n = a.notes;
	a.notes = new ArrayList();
	if (n != null)
	    a.notes.addAll(n);
	return a.notes;
    }

    void save()
    {
	sett.setAttributes(gson.toJson(this.books));
    }

    static private final class Attrs
    {
	@SerializedName("notes")
	List<Note> notes;
    }

    static private final class Books
    {
	@SerializedName("attrs")
	Map<String, Attrs> attrs = null;
    }
}

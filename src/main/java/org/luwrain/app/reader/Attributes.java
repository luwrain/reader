/*
   Copyright 2012-2020 Michael Pozhidaev <msp@luwrain.org>
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

final class Attributes
{
    private final Registry registry;
    private final Gson gson = new Gson();

    Attributes(Registry registry)
    {
	NullCheck.notNull(registry, "registry");
	this.registry = registry;
	this.registry.addDirectory(Settings.ATTRIBUTES_PATH);
    }

    BookAttr load(String id)
    {
	NullCheck.notEmpty(id, "id");
	final String path = Registry.join(Settings.ATTRIBUTES_PATH, id);
	if (registry.getTypeOf(path) != Registry.STRING)
	    return null;
	final String value = registry.getString(path);
	if (value.isEmpty())
	    return null;
	return gson.fromJson(value, BookAttr.class);
    }

    void save(String id, BookAttr bookAttr)
    {
	NullCheck.notEmpty(id, "id");
	NullCheck.notNull(bookAttr, "bookAttr");
	final String path = Registry.join(Settings.ATTRIBUTES_PATH, id);
	registry.setString(path, gson.toJson(bookAttr));
    }

    static final class Note
    {
	@SerializedName("title")
	private String title = null;
	Note(String title)
	{
	    NullCheck.notEmpty(title, "title");
	    this.title = title;
	}
	@Override public String toString()
	{
	    return this.title != null?title:"";
	}
    }

    static final class Bookmark
    {
	@SerializedName("pos")
	private int pos = 0;
    }

    static final class BookAttr
    {
	@SerializedName("notes")
	private List<Note> notes = null;
	@SerializedName("bookmark")
	private Bookmark bookmark = null;
	@SerializedName("charset")
	private String charset = null;
	@SerializedName("paragraphType")
	private String paragraphType = null;
	List<Note> getNotes()
	{
	    if (this.notes == null)
		this.notes = new ArrayList();
	    return this.notes;
	}
    }
}

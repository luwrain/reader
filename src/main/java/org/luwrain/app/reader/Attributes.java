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
    private final App app;

    Attributes(App app)
    {
	NullCheck.notNull(app, "app");
	this.app = app;
    }

    void addNote(int pos, String text)
    {
    }

    void addNotes(int pos, List<Note> notes)
    {
    }

    void removeNote(int pos)
    {
    }

    int getNoteCount()
    {
	return 0;
    }

    Note getNote(int index)
    {
	return null;
    }

    static final class BookAttr
    {
	@SerializedName("charset")
	private String charset = null;
	@SerializedName("paragraphType")
	private String paragraphType = null;
    }
}

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
import org.luwrain.controls.*;
import org.luwrain.io.api.books.v1.*;

final class Attributes implements EditableListArea.Model
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

    @Override public boolean clearModel()
    {
	return false;
    }

    @Override public boolean addToModel(int pos, java.util.function.Supplier supplier)
    {
	NullCheck.notNull(supplier, "supplier");
	if (pos < 0 || pos > getNoteCount())
	    throw new IllegalArgumentException("pos (" + String.valueOf(pos) + ") must be non-negative and not greater than " + String.valueOf(getNoteCount()));
	final Object supplied = supplier.get();
	if (supplied == null)
	    return false;
	final Object[] newNotes;
	if (supplied instanceof Object[])
	    newNotes = (Object[])supplied; else
	    newNotes = new Object[]{supplied};
	for(Object o: newNotes)
	    if (!(o instanceof Note))
		return false;
	addNotes(pos, Arrays.asList(Arrays.copyOf(newNotes, newNotes.length, Note[].class)));
	return true;
    }

    @Override public boolean removeFromModel(int pos)
    {
	if (pos < 0 || pos >= getNoteCount())
	    throw new IllegalArgumentException("pos (" + String.valueOf(pos) + ") must be non-negative and less than " + String.valueOf(getNoteCount()));
	removeNote(pos);
	return true;
    }

    @Override public Object getItem(int index)
    {
	return getNote(index);
    }

    @Override public int getItemCount()
    {
	return getNoteCount();
    }

    @Override public void refresh()
    {
    }

    static final class BookAttr
    {
	@SerializedName("charset")
	private String charset = null;
	@SerializedName("paragraphType")
	private String paragraphType = null;
    }
}

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
import java.util.concurrent.*;

import com.google.gson.*;
import com.google.gson.annotations.*;

import org.luwrain.core.*;
import org.luwrain.controls.*;
import org.luwrain.io.api.books.v1.*;

final class Notes extends ArrayList<Note> implements EditableListArea.Model
{
        private final App app;
    private final Attributes attrs;
    private final List<Note> notes;
    private FutureTask task = null;

    Notes(App app, String bookId)
    {
	NullCheck.notNull(app, "app");
	NullCheck.notEmpty(bookId, "bookId");
	this.app = app;
	this.attrs = app.getAttributes();
	this.notes = this.attrs.getBookNotes(bookId);
    }

    boolean addNote(int pos, String text)
    {
	NullCheck.notNull(text, "text");
	if (pos < 0)
	    throw new IllegalArgumentException("pos can't be negative");
	final Note note = new Note();
	note.setPos(String.valueOf(pos));
	note.setText(text);
	this.notes.add(note);
	this.attrs.save();
	return true;
    }

    boolean addNotes(int pos, List<Note> notes)
    {
	if (isBusy())
	    return false;
	return true;
    }

    boolean removeNotes(int posFrom, int posTo)
    {
	if (isBusy())
	return false;
	return true;
    }

    int ggetNoteCount()
    {
	return this.notes.size();
    }

    Note getNote(int index)
    {
	return this.notes.get(index);
    }

    @Override public boolean addToModel(int pos, java.util.function.Supplier supplier)
    {
	NullCheck.notNull(supplier, "supplier");
	if (pos < 0 || pos > size())
	    throw new IllegalArgumentException("pos (" + String.valueOf(pos) + ") must be non-negative and not greater than " + String.valueOf(size()));
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
	return addNotes(pos, Arrays.asList(Arrays.copyOf(newNotes, newNotes.length, Note[].class)));
    }

    @Override public boolean removeFromModel(int posFrom, int posTo)
    {
	if (posFrom < 0 || posFrom>= size())
	    throw new IllegalArgumentException("pos (" + String.valueOf(posFrom) + ") must be non-negative and less than " + String.valueOf(size()));
		if (posTo < 0 || posTo >= size())
	    throw new IllegalArgumentException("pos (" + String.valueOf(posTo) + ") must be non-negative and less or equal than " + String.valueOf(size()));
	return removeNotes(posFrom, posTo);
    }

    @Override public Object getItem(int index)
    {
	return get(index);
    }

    @Override public int getItemCount()
    {
	return size();
    }

    @Override public void refresh()
    {
    }

    private boolean isBusy()
    {
	return task != null && !task.isDone();
    }
}

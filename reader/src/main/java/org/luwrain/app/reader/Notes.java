// SPDX-License-Identifier: BUSL-1.1
// Copyright 2012-2026 Michael Pozhidaev <msp@luwrain.org>

package org.luwrain.app.reader;

import java.util.*;
import java.util.concurrent.*;

import com.google.gson.*;
import com.google.gson.annotations.*;

import org.luwrain.core.*;
import org.luwrain.controls.*;
import org.luwrain.io.api.books.v1.*;

import static java.util.Objects.*;

final class Notes implements EditableListArea.Model
{
    private final App app;
    private final Attributes attrs;
    private final List<Note> notes;
    private FutureTask task = null;

    Notes(App app, String bookId)
    {
	requireNonNull(app, "app can't be null");
	NullCheck.notEmpty(bookId, "bookId");
	this.app = app;
	this.attrs = app.getAttributes();
	this.notes = this.attrs.getBookNotes(bookId);
    }

        boolean setBookmark(int pos)
    {
	/*
	if (pos < 0)
	    throw new IllegalArgumentException("pos can't be negative");
	final Note note = new Note();
	note.setType(Note.BOOKMARK);
	note.setPos(String.valueOf(pos));
	for(int i = 0;i < this.notes.size();i++)
	    if (this.notes.get(i).getType() != null && this.notes.get(i).getType().equals(Note.BOOKMARK))
	    {
		this.notes.set(i, note);
			this.attrs.save();
			return true;
	    }
	this.notes.add(note);
	this.attrs.save();
	*/
	return true;
    }

    Note getBookmark()
    {
	/*
	for(Note n: notes)
	    if (n.getType() != null && n.getType().equals(Note.BOOKMARK))
		return n;
	*/
	return null;
    }

    boolean addNote(int pos, String text)
    {
	/*
	requireNonNull(text, "text can't be null");
	if (pos < 0)
	    throw new IllegalArgumentException("pos can't be negative");
	final Note note = new Note();
	note.setType(Note.NOTE);
	note.setPos(String.valueOf(pos));
	//	note.setText(text);
	this.notes.add(0, note);
	this.attrs.save();
	*/
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

    @Override public int getItemCount()
    {
	return this.notes.size();
    }

    @Override public Note getItem(int index)
    {
	return this.notes.get(index);
    }

    @Override public boolean addToModel(int pos, java.util.function.Supplier supplier)
    {
	requireNonNull(supplier, "supplier can't be null");
	if (pos < 0 || pos > getItemCount())
	    throw new IllegalArgumentException("pos (" + String.valueOf(pos) + ") must be non-negative and not greater than " + String.valueOf(getItemCount()));
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
	if (posFrom < 0 || posFrom>= getItemCount())
	    throw new IllegalArgumentException("pos (" + String.valueOf(posFrom) + ") must be non-negative and less than " + String.valueOf(getItemCount()));
		if (posTo < 0 || posTo >= getItemCount())
	    throw new IllegalArgumentException("pos (" + String.valueOf(posTo) + ") must be non-negative and less or equal than " + String.valueOf(getItemCount()));
	return removeNotes(posFrom, posTo);
    }

    @Override public void refresh()
    {
    }

    private boolean isBusy()
    {
	return task != null && !task.isDone();
    }
}

// SPDX-License-Identifier: BUSL-1.1
// Copyright 2012-2026 Michael Pozhidaev <msp@luwrain.org>

package org.luwrain.app.reader;

import java.util.*;
import java.io.*;

import com.google.gson.annotations.*;
import com.google.gson.*;

import org.luwrain.core.*;

import static java.util.Objects.*;

final class Attributes
{
    static private final String
	LOG_COMPONENT = App.LOG_COMPONENT;

    final App app;
    private Books books;

    Attributes(App app)
    {
	this.app = requireNonNull(app, "app can't be null");
	this.books = load();
    }

    List<Note> getBookNotes(String bookId)
    {
	NullCheck.notEmpty(bookId, "bookId");
	if (books == null || books.attrs == null)
	    return new ArrayList<>();
	Attrs a = books.attrs.get(bookId);
	if (a == null)
	    return new ArrayList<>();
	return a.notes != null ? a.notes : new ArrayList<>();
    }

    void setBookNotes(String bookId, List<Note> notes)
    {
	NullCheck.notEmpty(bookId, "bookId");
	requireNonNull(notes, "notes can't be null");
	if (books == null)
	    books = new Books();
	if (books.attrs == null)
	    books.attrs = new HashMap<>();
	Attrs a = books.attrs.get(bookId);
	if (a == null)
	{
	    a = new Attrs();
	    books.attrs.put(bookId, a);
	}
	a.notes = notes;
	save();
    }

    void save()
    {
	final Settings settings = Settings.create(app.getLuwrain());
	if (settings == null)
	    return;
	try {
	    final Gson gson = new GsonBuilder().setPrettyPrinting().create();
	    final String json = books != null ? gson.toJson(books) : "{}";
	    settings.setAttributes(json);
	}
	catch (Exception e)
	{
	    Log.error(LOG_COMPONENT, "unable to save attributes: " + e.getClass().getName() + ": " + e.getMessage());
	}
    }

    private Books load()
    {
	final Settings settings = Settings.create(app.getLuwrain());
	if (settings == null)
	    return new Books();
	try {
	    final String json = settings.getAttributes("{}");
	    final Gson gson = new Gson();
	    final Books b = gson.fromJson(json, Books.class);
	    return b != null ? b : new Books();
	}
	catch (Exception e)
	{
	    Log.error(LOG_COMPONENT, "unable to load attributes: " + e.getClass().getName() + ": " + e.getMessage());
	    return new Books();
	}
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

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
import java.io.*;

import com.google.gson.*;
import com.google.gson.annotations.*;

import org.luwrain.core.*;
import org.luwrain.io.api.books.v1.*;

final class LocalRepoMetadata
{
    private final Gson gson = new Gson();
    private final Settings sett;
    private List<Book> books = null;

    LocalRepoMetadata(Settings sett)
    {
	NullCheck.notNull(sett, "sett");
	this.sett = sett;
    }

    List<Book> getBooks()
    {
	if (this.books != null)
	    return this.books;
	Data data = gson.fromJson(sett.getLocalRepoMetadata(""), Data.class);
	if (data == null)
	    data = new Data();
	if (data.books == null)
	    data.books = new ArrayList();
	this.books = new ArrayList(data.books);
	return this.books;
    }

    void save()
    {
	if (this.books == null)
	    return;
	final Data data = new Data();
	data.books = this.books;
	sett.setLocalRepoMetadata(gson.toJson(data));
    }

    Book findBook(String id)
    {
	NullCheck.notEmpty(id, "id");
	for(Book b: getBooks())
	    if (b.getId().equals(id))
		return b;
	return null;
    }

    void addBook(Book book)
    {
	NullCheck.notNull(book, "book");
	if (book.getId() == null || book.getId().isEmpty())
	    throw new IllegalArgumentException("The book doesn't have an ID");
	if (this.books == null)
	    getBooks();
	for(Book b: books)
	    if (b.getId().equals(book.getId()))
		return;
	books.add(book);
	save();
    }

    static private final class  Data
    {
	@SerializedName("books")
	List<Book> books = null;
    }
}

// SPDX-License-Identifier: BUSL-1.1
// Copyright 2012-2026 Michael Pozhidaev <msp@luwrain.org>

package org.luwrain.app.reader;

import java.util.*;
import java.io.*;

import com.google.gson.*;
import com.google.gson.annotations.*;

import org.luwrain.core.*;
import org.luwrain.io.api.books.v1.*;

import static java.util.Objects.*;

final class LocalRepoMetadata
{
    private final Gson gson = new Gson();
    private final Settings sett;
    private List<Book> books = null;

    LocalRepoMetadata(Settings sett)
    {
	requireNonNull(sett, "sett can't be null");
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
	requireNonNull(book, "book can't be null");
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

    boolean removeBook(Book book)
    {
	requireNonNull(book, "book can't be null");
	if (book.getId() == null || book.getId().isEmpty())
	    throw new IllegalArgumentException("The book doesn't have an ID");
	if (this.books == null)
	    getBooks();
	for(int i = 0;i < books.size();i++)
	    if (books.get(i).equals(book))
	    {
		books.remove(i);
		save();
		return true;
	    }
	return false;
    }


    static private final class  Data
    {
	@SerializedName("books")
	List<Book> books = null;
    }
}

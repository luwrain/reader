// SPDX-License-Identifier: BUSL-1.1
// Copyright 2012-2026 Michael Pozhidaev <msp@luwrain.org>

package org.luwrain.app.reader;

import java.util.*;
import java.util.zip.*;
import java.io.*;
import java.net.*;
import org.apache.commons.io.*;

import org.luwrain.core.*;
import org.luwrain.util.*;
//import org.luwrain.io.api.books.v1.*;

import static java.nio.file.Files.*;

import static java.util.Objects.*;

final class LocalRepo
{
    private File repoDir;
    private final App app;

    LocalRepo(App app, File repoDir)
    {
		requireNonNull(app, "app can't be null");
	requireNonNull(repoDir, "repoDir can't be null");
	this.app = app;
	this.repoDir = repoDir;
    }

        Book findBook(String id)
    {
	/*
	NullCheck.notEmpty(id, "id");
	for(Book b: getBooks())
	    if (b.getId().equals(id))
		return b;
	*/
	return null;
    }

    void addBook(Book book)
    {
	requireNonNull(book, "book can't be null");
	/*
	if (book.getId() == null || book.getId().isEmpty())
	    throw new IllegalArgumentException("The book doesn't have an ID");
	if (this.books == null)
	    getBooks();
	for(Book b: books)
	    if (b.getId().equals(book.getId()))
		return;
	books.add(book);
	save();
	*/
    }

    boolean removeBook(Book book)
    {
	requireNonNull(book, "book can't be null");
	/*
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
	*/
	return false;
    }


    void addDaisy(Book book, File zipFile) throws IOException
    {
	requireNonNull(book, "book can't be null");
	requireNonNull(zipFile, "zipFile can't be null");
	/*&
	final String id = book.getId();
	if (id == null || id.isEmpty())
	    throw new IllegalArgumentException("The book diesn't have an ID");
	final File bookDir = new File(repoDir, id);
	createDirectories(bookDir.toPath());
        try (final BufferedInputStream is = new BufferedInputStream(new FileInputStream(zipFile))) {
	    final ZipInputStream stream = new ZipInputStream(is);
	    {
		ZipEntry entry = null;
		while ((entry = stream.getNextEntry()) != null) {
		    if (entry.isDirectory())
			continue;
		    final File destFile = new File(bookDir, entry.getName());
		    createDirectories(destFile.getParentFile().toPath());
		    try (final BufferedOutputStream os = new BufferedOutputStream(new FileOutputStream(destFile))){
			IOUtils.copy(stream, os);
			os.flush();
		    }
		    stream.closeEntry();
		}
	    }
	}
	metadata.addBook(book);
	*/
    }

    boolean remove(Book book)
    {
	requireNonNull(book, "book can't be null");
	/*
	if (!metadata.removeBook(book))
	    return false;
	deleteDir(new File(repoDir, book.getId()));
	*/
	return true;
    }

    private void deleteDir(File file)
    {
	requireNonNull(file, "file can't be null");
	if (!file.exists())
	    return;
	if (!file.isDirectory())
	{
	    file.delete();
	    return;
	}
	final File[] files = file.listFiles();
	if (files != null)
	{
	    for(File f: files)
		if (f != null)
		    deleteDir(f);
	}
	file.delete();
    }

    File findDaisyMainFile(Book book)
    {
	/*
	requireNonNull(book, "book can't be null");
	NullCheck.notEmpty(book.getId(), "book.getId()");
	return findNcc(new File(repoDir, book.getId()));
	*/
	return null;
    }

    private File findNcc(File file)
    {
	requireNonNull(file, "file can't be null");
	if (!file.isDirectory())
	{
	    final String name = file.getName().toLowerCase();
	    return (name.equals("ncc.html") || name.equals("ncc.htm"))?file:null;
	}
	final File[] files = file.listFiles();
	if (files == null)
	    return null;
	for(File f: files)
	    if (f != null)
	    {
		final File res = findNcc(f);
		if (res != null)
		    return res;
	    }
	return null;
    }

    Book[] getBooks()
    {
	/*
	final List<Book> books = metadata.getBooks();
	return books.toArray(new Book[books.size()]);
	*/
	return null;
    }

    boolean hasBook(Book book)
    {
	/*
	requireNonNull(book, "book can't be null");
	return metadata.findBook(book.getId()) != null;
	*/
	return false;
    }
}

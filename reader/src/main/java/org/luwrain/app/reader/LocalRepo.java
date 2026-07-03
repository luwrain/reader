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

import com.google.gson.*;
import com.google.gson.reflect.*;

import static java.nio.file.Files.*;

import static java.util.Objects.*;

final class LocalRepo
{
    private File repoDir;
    private final App app;
    private List<Book> books = null;

    LocalRepo(App app, File repoDir)
    {
	requireNonNull(app, "app can't be null");
	requireNonNull(repoDir, "repoDir can't be null");
	this.app = app;
	this.repoDir = repoDir;
    }

    Book[] getBooks()
    {
	load();
	return books != null ? books.toArray(new Book[books.size()]) : new Book[0];
    }

    Book findBook(String id)
    {
	NullCheck.notEmpty(id, "id");
	load();
	if (books == null)
	    return null;
	for(Book b: books)
	    if (b.getId() != null && b.getId().equals(id))
		return b;
	return null;
    }

    boolean hasBook(Book book)
    {
	requireNonNull(book, "book can't be null");
	if (book.getId() == null || book.getId().isEmpty())
	    return false;
	return findBook(book.getId()) != null;
    }

    void addBook(Book book)
    {
	requireNonNull(book, "book can't be null");
	if (book.getId() == null || book.getId().isEmpty())
	    throw new IllegalArgumentException("The book doesn't have an ID");
	load();
	if (books == null)
	    books = new ArrayList<>();
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
	load();
	if (books == null)
	    return false;
	for(int i = 0;i < books.size();i++)
	    if (books.get(i).getId().equals(book.getId()))
	    {
		books.remove(i);
		save();
		return true;
	    }
	return false;
    }

    boolean remove(Book book)
    {
	requireNonNull(book, "book can't be null");
	if (!removeBook(book))
	    return false;
	final File bookDir = new File(repoDir, book.getId());
	deleteDir(bookDir);
	return true;
    }

    void addDaisy(Book book, File zipFile) throws IOException
    {
	requireNonNull(book, "book can't be null");
	requireNonNull(zipFile, "zipFile can't be null");
	final String id = book.getId();
	if (id == null || id.isEmpty())
	    throw new IllegalArgumentException("The book doesn't have an ID");
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
	addBook(book);
    }

    File findDaisyMainFile(Book book)
    {
	requireNonNull(book, "book can't be null");
	if (book.getId() == null || book.getId().isEmpty())
	    return null;
	return findNcc(new File(repoDir, book.getId()));
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

    private void load()
    {
	if (books != null)
	    return;
	try {
	    createDirectories(repoDir.toPath());
	    final File metaFile = new File(repoDir, "repo.json");
	    if (!metaFile.exists())
	    {
		books = new ArrayList<>();
		return;
	    }
	    final Gson gson = new Gson();
	    try (final Reader reader = new InputStreamReader(new FileInputStream(metaFile), "UTF-8")) {
		final Book[] arr = gson.fromJson(reader, Book[].class);
		books = arr != null ? new ArrayList<>(Arrays.asList(arr)) : new ArrayList<>();
	    }
	}
	catch(IOException e)
	{
	    Log.error(App.LOG_COMPONENT, "unable to load local repo: " + e.getClass().getName() + ": " + e.getMessage());
	    books = new ArrayList<>();
	}
    }

    private void save()
    {
	if (books == null)
	    return;
	try {
	    createDirectories(repoDir.toPath());
	    final Gson gson = new GsonBuilder().setPrettyPrinting().create();
	    final File metaFile = new File(repoDir, "repo.json");
	    try (final Writer writer = new OutputStreamWriter(new FileOutputStream(metaFile), "UTF-8")) {
		gson.toJson(books, writer);
	    }
	}
	catch(IOException e)
	{
	    Log.error(App.LOG_COMPONENT, "unable to save local repo: " + e.getClass().getName() + ": " + e.getMessage());
	}
    }
}

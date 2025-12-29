
package org.luwrain.app.reader;

import java.util.*;
import java.util.zip.*;
import java.io.*;
import java.net.*;
import org.apache.commons.io.*;

import org.luwrain.core.*;
import org.luwrain.util.*;
import org.luwrain.io.api.books.v1.*;

import static java.nio.file.Files.*;

final class LocalRepo
{
    private File repoDir;
    private final LocalRepoMetadata metadata;

    LocalRepo(LocalRepoMetadata metadata, File repoDir)
    {
	NullCheck.notNull(metadata, "metadata");
	NullCheck.notNull(repoDir, "repoDir");
	this.metadata = metadata;
	this.repoDir = repoDir;
    }

    void addDaisy(Book book, File zipFile) throws IOException
    {
	NullCheck.notNull(book, "book");
	NullCheck.notNull(zipFile, "zipFile");
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
    }

    boolean remove(Book book)
    {
	NullCheck.notNull(book, "book");
	if (!metadata.removeBook(book))
	    return false;
	deleteDir(new File(repoDir, book.getId()));
	return true;
    }

    private void deleteDir(File file)
    {
	NullCheck.notNull(file, "file");
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
	NullCheck.notNull(book, "book");
	NullCheck.notEmpty(book.getId(), "book.getId()");
	return findNcc(new File(repoDir, book.getId()));
    }

    private File findNcc(File file)
    {
	NullCheck.notNull(file, "file");
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
	final List<Book> books = metadata.getBooks();
	return books.toArray(new Book[books.size()]);
    }

    boolean hasBook(Book book)
    {
	NullCheck.notNull(book, "book");
	return metadata.findBook(book.getId()) != null;
    }
}

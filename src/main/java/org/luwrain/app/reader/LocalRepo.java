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
import java.util.zip.*;
import java.io.*;
import java.net.*;

import org.luwrain.core.*;
import org.luwrain.util.*;
import org.luwrain.io.api.books.v1.*;

final class LocalRepo
{
    private File repoDir = new File("/tmp/repo");
    private final LocalRepoMetadata metadata;

    LocalRepo(LocalRepoMetadata metadata)
    {
	NullCheck.notNull(metadata, "metadata");
	this.metadata = metadata;
    }

    void addDaisy(Book book, File zipFile) throws IOException
    {
	NullCheck.notNull(book, "book");
	NullCheck.notNull(zipFile, "zipFile");
	final String id = book.getId();
	if (id == null || id.isEmpty())
	    throw new IllegalArgumentException("The book diesn't have an ID");
	final File bookDir = new File(repoDir, id);
	FileUtils.createSubdirs(bookDir);
        try (final BufferedInputStream is = new BufferedInputStream(new FileInputStream(zipFile))) {
	    final ZipInputStream stream = new ZipInputStream(is);
	    {
		ZipEntry entry = null;
		while ((entry = stream.getNextEntry()) != null) {
		    if (entry.isDirectory())
			continue;
		    final File destFile = new File(bookDir, entry.getName());
		    FileUtils.createSubdirs(destFile.getParentFile());
		    try (final BufferedOutputStream os = new BufferedOutputStream(new FileOutputStream(destFile))){
			StreamUtils.copyAllBytes(stream, os);
			os.flush();
		    }
		    stream.closeEntry();
		}
	    }
	}
	metadata.addBook(book);
    }
}

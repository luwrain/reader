
package org.luwrain.app.reader.books;

import java.net.*;
import java.io.*;
import java.nio.file.*;

import org.luwrain.core.*;
import org.luwrain.reader.*;
import org.luwrain.util.*;
import org.luwrain.app.reader.*;

public final class BookFactory
{
    static public Book initDaisy2(Luwrain luwrain, Document nccDoc)
    {
	NullCheck.notNull(luwrain, "luwrain");
	NullCheck.notNull(nccDoc, "nccDoc");
	final Daisy2 book = new Daisy2(luwrain);
	book.init(nccDoc);
	return book;
    }

    public Book newBook(Luwrain luwrain, String url) throws IOException
    {
	final UrlLoader loader = new UrlLoader(luwrain, new URL(url));
	final UrlLoader.Result res = loader.load();
	return new SingleFileBook(luwrain, res.doc);
    }
}

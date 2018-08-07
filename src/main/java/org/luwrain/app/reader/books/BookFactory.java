
package org.luwrain.app.reader.books;

import java.net.*;
import java.io.*;
import java.nio.file.*;

import org.luwrain.core.NullCheck;
import org.luwrain.core.Log;
import org.luwrain.doctree.*;
import org.luwrain.util.*;
import org.luwrain.app.reader.loading.*;

public class BookFactory
{
    static public Book initDaisy2(Document nccDoc, UrlLoaderFactory urlLoaderFactory)
    {
	NullCheck.notNull(nccDoc, "nccDoc");
	NullCheck.notNull(urlLoaderFactory, "urlLoaderFactory");
	final Daisy2 book = new Daisy2(urlLoaderFactory);
	book.init(nccDoc);
	return book;
    }
}

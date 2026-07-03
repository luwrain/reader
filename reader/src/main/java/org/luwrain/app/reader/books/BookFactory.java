package org.luwrain.app.reader.books;

import java.net.*;
import java.io.*;
import java.nio.file.*;

import org.luwrain.core.*;
import org.luwrain.io.bookdoc.*;
import org.luwrain.util.*;

public final class BookFactory
{
    static final String LOG_COMPONENT = "reader";

    private Book initDaisy2(Luwrain luwrain, Doc nccDoc)
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
	final Doc doc = res.doc;
	final URL docUrl;
	try {
	    docUrl = new URL(doc.getProperty("url"));
	}
	catch(MalformedURLException e)
	{
	    Log.warning(LOG_COMPONENT, "unable to extract the URL of the loaded document: " + e.getClass().getName() + ":" + e.getMessage());
	    return new SingleFileBook(luwrain, doc);
	}
	if (docUrl.getFile().toLowerCase().endsWith("/ncc.html"))
	{
	    Log.debug(LOG_COMPONENT, "opening the book as DAISY v2.2");
	    return initDaisy2(luwrain, doc);
	}
	return new SingleFileBook(luwrain, res.doc);
    }
}

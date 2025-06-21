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

package org.luwrain.app.reader.books;

import java.net.*;
import java.io.*;
import java.nio.file.*;

import org.luwrain.core.*;
import org.luwrain.reader.*;
import org.luwrain.util.*;
import org.luwrain.reader.*;
import org.luwrain.app.reader.*;

public final class BookFactory
{
    static final String LOG_COMPONENT = "reader";
    
    private Book initDaisy2(Luwrain luwrain, Document nccDoc)
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
	final Document doc = res.doc;
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

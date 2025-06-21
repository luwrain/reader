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

import java.util.*;
import java.io.*;
import java.net.*;

import org.luwrain.core.*;
import org.luwrain.reader.*;
import org.luwrain.app.reader.*;

public final class SingleFileBook implements Book
{
    private final Luwrain luwrain;
    private Document doc = null;

    SingleFileBook(Luwrain luwrain, Document doc)
    {
	NullCheck.notNull(luwrain, "luwrain");
	NullCheck.notNull(doc, "doc");
	this.luwrain = luwrain;
	this.doc = doc;
    }

    @Override public String getBookId()
    {
	return "FIXME";
    }

    @Override public Set<Flags> getBookFlags()
    {
	return EnumSet.noneOf(Flags.class);
    }

        @Override public Document getDocument(String href) throws IOException
    {
	NullCheck.notEmpty(href, "href");
		final UrlLoader loader = new UrlLoader(luwrain, new URL(href));
	final UrlLoader.Result res = loader.load();
	return res.doc;
    }

    @Override public Document getDefaultDocument()
    {
	return this.doc;
    }

    @Override public AudioFragment findAudioForId(String ids)
    {
	return null;
    }

    @Override public String findTextForAudio(String audioFileUrl, long msec)
    {
	return null;
    }

    @Override public Section[] getBookSections()
    {
	return null;
    }
}

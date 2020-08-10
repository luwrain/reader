/*
   Copyright 2012-2020 Michael Pozhidaev <msp@luwrain.org>
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
import java.util.*;
import java.io.*;
import java.nio.file.*;

import org.luwrain.core.*;
import org.luwrain.reader.*;
import org.luwrain.app.reader.*;
import org.luwrain.util.*;

final class Javadoc implements Book
{
    private final Luwrain luwrain;
    private URL baseUrl;

    Javadoc(Luwrain luwrain, URL baseUrl)
    {
	NullCheck.notNull(luwrain, "luwrain");
	NullCheck.notNull(baseUrl, "baseUrl");
	this.luwrain = luwrain;
	this.baseUrl = baseUrl;
    }

    @Override public String getBookId()
    {
	return "FIXME";
    }

    @Override public Set<Flags> getBookFlags()
    {
	return EnumSet.of(Flags.OPEN_IN_SECTION_TREE);
    }

    @Override public Document getDefaultDocument()
    {
	return null;
    }

    @Override public Document getDocument(String href)
    {
	NullCheck.notNull(href, "href");
	return null;
    }

    @Override public AudioFragment findAudioForId(String id)
    {
	return null;
    }

    @Override public     String findTextForAudio(String audioFileUrl, long msec)
    {
	return null;
    }

    @Override public Book.Section[] getBookSections()
    {
	return null;
    }
}

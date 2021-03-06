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
import java.net.*;

import org.luwrain.core.*;
import org.luwrain.reader.*;

public interface Book
{
    public enum Flags {OPEN_IN_SECTION_TREE};

    String getBookId();
    Set<Flags> getBookFlags();
    Section[] getBookSections();
    Document getDocument(String href) throws java.io.IOException;
    Document getDefaultDocument();
    AudioFragment findAudioForId(String ids);
    String findTextForAudio(String audioFileUrl, long msec);

    static public final class Section
    {
	public final int level;
	public final String title;
	public final String href;
	public Section(int level, String title, String href)
	{
	    NullCheck.notNull(title, "title");
	    NullCheck.notNull(href, "href");
	    if (level < 0)
		throw new IllegalArgumentException("level (" + String.valueOf(level) + ") can't be negative");
	    this.level = level;
	    this.title = title;
	    this.href = href;
	}
	@Override public String toString()
	{
	    return title;
	}
    }

}

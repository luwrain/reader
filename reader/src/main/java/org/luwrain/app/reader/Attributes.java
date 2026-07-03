// SPDX-License-Identifier: BUSL-1.1
// Copyright 2012-2026 Michael Pozhidaev <msp@luwrain.org>

package org.luwrain.app.reader;

import java.util.*;

import com.google.gson.annotations.*;

import org.luwrain.core.*;

import static java.util.Objects.*;

final class Attributes
{
    final App app;
    private final Books books;

    Attributes(App app)
    {
	this.app = requireNonNull(app, "app can't be null");
	books = null;
    }

    List<Note> getBookNotes(String bookId)
    {
	return null;
	    }

    void save()
    {
	    }

    static private final class Attrs
    {
	@SerializedName("notes")
	List<Note> notes;
    }

    static private final class Books
    {
	@SerializedName("attrs")
	Map<String, Attrs> attrs = null;
    }
}

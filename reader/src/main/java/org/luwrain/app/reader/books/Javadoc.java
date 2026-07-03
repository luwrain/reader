// SPDX-License-Identifier: BUSL-1.1
// Copyright 2012-2026 Michael Pozhidaev <msp@luwrain.org>

package org.luwrain.app.reader.books;

import java.net.*;
import java.util.*;
import java.io.*;
import java.nio.file.*;

import org.luwrain.core.*;
import org.luwrain.io.bookdoc.*;
import org.luwrain.app.reader.*;
import org.luwrain.util.*;

import static java.util.Objects.*;

final class Javadoc implements Book
{
    private final Luwrain luwrain;
    private URL baseUrl;

    Javadoc(Luwrain luwrain, URL baseUrl)
    {
	requireNonNull(luwrain, "luwrain can't be null");
	requireNonNull(baseUrl, "baseUrl can't be null");
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

    @Override public Doc getDefaultDocument()
    {
	return null;
    }

    @Override public Doc getDocument(String href)
    {
	requireNonNull(href, "href can't be null");
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

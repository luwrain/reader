// SPDX-License-Identifier: BUSL-1.1
// Copyright 2012-2026 Michael Pozhidaev <msp@luwrain.org>

package org.luwrain.app.reader.books;

import java.util.*;
import java.io.*;
import java.net.*;

import org.luwrain.core.*;
import org.luwrain.io.bookdoc.*;

import static java.util.Objects.*;

public final class SingleFileBook implements Book
{
    private final Luwrain luwrain;
    private Doc doc = null;

    SingleFileBook(Luwrain luwrain, Doc doc)
    {
	requireNonNull(luwrain, "luwrain can't be null");
	requireNonNull(doc, "doc can't be null");
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

    @Override public Doc getDocument(String href) throws IOException
    {
	NullCheck.notEmpty(href, "href");
	final UrlLoader loader = new UrlLoader(luwrain, new URL(href));
	final UrlLoader.Result res = loader.load();
	return res.doc;
    }

    @Override public Doc getDefaultDocument()
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

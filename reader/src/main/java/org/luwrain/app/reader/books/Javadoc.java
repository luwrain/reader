package org.luwrain.app.reader.books;

import java.net.*;
import java.util.*;
import java.io.*;
import java.nio.file.*;

import org.luwrain.core.*;
import org.luwrain.io.bookdoc.*;
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

    @Override public Doc getDefaultDocument()
    {
	return null;
    }

    @Override public Doc getDocument(String href)
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

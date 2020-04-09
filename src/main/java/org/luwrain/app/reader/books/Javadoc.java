
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

    @Override public Document getStartingDocument()
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

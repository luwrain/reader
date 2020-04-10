
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

        @Override public Document getDocument(String href) throws IOException
    {
	NullCheck.notEmpty(href, "href");
		final UrlLoader loader = new UrlLoader(luwrain, new URL(href));
	final UrlLoader.Result res = loader.load();
	return res.doc;
    }

    @Override public Document getStartingDocument()
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

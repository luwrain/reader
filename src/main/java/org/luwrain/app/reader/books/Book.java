
package org.luwrain.app.reader.books;

import java.util.*;
import java.net.*;

import org.luwrain.core.*;
import org.luwrain.doctree.*;

public interface Book
{
    static public class Section
    {
	public final int level;
	public final String title;
	public final String href;

	public Section(int level,
		       String title, String href)
	{
	    NullCheck.notNull(title, "title");
	    NullCheck.notNull(href, "href");
	    this.level = level;
	    this.title = title;
	    this.href = href;
	}

	@Override public String toString()
	{
	    return title;
	}
    }

    Document[] getDocuments();
    Map<URL, Document> getDocumentsWithUrls();
    Document getStartingDocument();
    Document openHref(String href);
    AudioInfo findAudioForId(String ids);
    String findTextForAudio(String audioFileUrl, long msec);
    //Expecting that href is absolute
    Document getDocument(String href);
    Section[] getBookSections();
}

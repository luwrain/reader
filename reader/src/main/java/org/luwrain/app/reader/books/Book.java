package org.luwrain.app.reader.books;

import java.util.*;

import org.luwrain.core.*;
import org.luwrain.io.bookdoc.*;

public interface Book
{
    enum Flags {OPEN_IN_SECTION_TREE};

    String getBookId();
    Set<Flags> getBookFlags();
    Section[] getBookSections();
    Doc getDocument(String href) throws java.io.IOException;
    Doc getDefaultDocument();
    AudioFragment findAudioForId(String ids);
    String findTextForAudio(String audioFileUrl, long msec);

    final class Section
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

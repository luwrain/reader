// SPDX-License-Identifier: BUSL-1.1
// Copyright 2012-2026 Michael Pozhidaev <msp@luwrain.org>

package org.luwrain.app.reader.books;

import java.util.*;

import org.luwrain.core.*;
import org.luwrain.io.bookdoc.*;
import static java.util.Objects.*;

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
	    requireNonNull(title, "title can't be null");
	    requireNonNull(href, "href can't be null");
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

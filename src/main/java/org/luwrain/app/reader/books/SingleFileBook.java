
package org.luwrain.app.reader.books;

import java.util.*;
import java.net.*;

import org.luwrain.core.*;
import org.luwrain.reader.*;

public final class SingleFileBook implements Book
{
        //Expecting that href is absolute
        @Override public Document getDocument(String href)
    {
	return null;
    }

    @Override public Document getStartingDocument()
    {
	return null;
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

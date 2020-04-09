
package org.luwrain.app.reader;

import java.util.*;
import java.io.*;
import java.net.*;

import org.luwrain.core.*;
import org.luwrain.reader.*;
import org.luwrain.app.reader.books.*;
import org.luwrain.player.*;
import org.luwrain.template.*;

/*
	if (savedPosition > 0)
	    res.doc.setProperty(Document.DEFAULT_ITERATOR_INDEX_PROPERTY, "" + savedPosition);
*/

final class BookContainer
{
    private final App2 app;
    private final Book book;
    private Document doc = null;
        private final LinkedList<HistoryItem> history = new LinkedList();
        private Book.Section[] sections = new Book.Section[0];

    BookContainer(App2 app, Book book)
    {
	NullCheck.notNull(app, "app");
	NullCheck.notNull(book, "book");
	this.app = app;
	this.book = book;
    }

    boolean jump(String href, int lastPos, int newDesiredPos, Runnable onSuccess)
    {
	NullCheck.notEmpty(href, "href");
	NullCheck.notNull(onSuccess, "onSuccess");
	final App2.TaskId taskId = app.newTaskId();
	return app.runTask(()->{	
	final Document doc = book.getDocument(href);
	if (doc == null)
	    return;
	if (doc != doc)
	{
	    if (lastPos >= 0 && !history.isEmpty())
		history.getLast().lastRowIndex = lastPos;
	    history.add(new HistoryItem(doc));
	}
	this.doc = doc;
	if (newDesiredPos >= 0)
	    doc.setProperty(Document.DEFAULT_ITERATOR_INDEX_PROPERTY, "" + newDesiredPos);
	    });
	    }


        boolean changeCharset(String newCharset)
    {
	NullCheck.notNull(newCharset, "newCharset");
	/*
	final UrlLoader urlLoader;
	try {
	    urlLoader = new UrlLoader(luwrain, res.doc.getUrl());
	}
	catch(MalformedURLException e)
	{
	    luwrain.crash(e);
	    return false;
	}
		if (storedProps == null)
	    storedProps = new StoredProperties(luwrain.getRegistry(), res.doc.getUrl().toString());
		storedProps.setCharset(newCharset);
	urlLoader.setCharset(newCharset);
	final ParaStyle paraStyle = translateParaStyle(storedProps.getParaStyle());
	if (paraStyle != null)
	    urlLoader.setTxtParaStyle(paraStyle);
	task = createTask(urlLoader);
	luwrain.executeBkg(task);
	*/
	return true;
    }

    boolean onPrevDoc()
    {
	if (history.isEmpty())
	    return false;
	final HistoryItem item = history.pollLast();
	/*
	    res.doc = item.doc;
successNotification.run();
	return true;
	*/
	return true;
    }

    /*
    boolean fillDocProperties(MutableLines lines)
    {
	NullCheck.notNull(lines, "lines");
	if (history.isEmpty())
	    return false;
	final HistoryItem item = history.getLast();
	lines.beginLinesTrans();
	lines.addLine(strings.propertiesAreaUrl(item.url));
	lines.addLine(strings.propertiesAreaContentType(item.contentType));
	lines.addLine(strings.propertiesAreaFormat(item.format));
	lines.addLine(strings.propertiesAreaCharset(item.charset));
	lines.addLine("");
	lines.endLinesTrans();
	return true;
    }
    */

    /*
    boolean playAudio(ReaderArea area, String[] ids)
    {
	NullCheck.notNull(area, "area");
	NullCheck.notNullItems(ids, "ids");
	if (!isInBookMode())
	    return false;
	if (audioPlaying == null)
	return false;
	return audioPlaying.playAudio(res.book, res.doc, area, ids);
    }
    */

    /*
    boolean stopAudio()
    {
	if (audioPlaying == null)
	    return false;
	return audioPlaying.stop();
    }
    */

    /*
    private URL getNotesUrl()
    {
	if (isInBookMode())
	    return res.book.getStartingDocument().getUrl();
	return res.doc.getUrl();
    }
    */



    /*
    String getDocHash()
    {
	if (!hasDocument())
	    return "";
	final String res = getDocument().getProperty("hash");
	return res != null?res:"";
    }
    */

    /*
    String getContentType()
    {
	if (!hasDocument())
	    return "";
	final String r = res.doc.getProperty("contenttype");
	return r != null?r:"";
    }
    */

    Book.Section[] getSections()
    {
	return new Book.Section[0];
    }

}

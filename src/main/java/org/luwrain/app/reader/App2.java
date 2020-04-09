
package org.luwrain.app.reader;

import java.util.*;
import java.util.concurrent.*;
import java.util.function.*;
import java.net.*;
import java.io.*;
import java.nio.file.*;

import org.luwrain.core.*;
import org.luwrain.controls.*;
import org.luwrain.popups.Popups;
import org.luwrain.reader.*;
import org.luwrain.controls.reader.*;
import org.luwrain.app.reader.books.*;
import org.luwrain.player.*;
import org.luwrain.template.*;

final class App2 extends AppBase<Strings>
{
    static final String LOG_COMPONENT = "reader";
    static private final String DEFAULT_ENCODING = "UTF-8";

enum ParaStyle {
	EMPTY_LINES,
	EACH_LINE,
	INDENT};


    private BookContainer bookContainer = null;
        private AudioPlaying audioPlaying = null;
    
        private final LinkedList<HistoryItem> history = new LinkedList();
    private UrlLoader.Result res = null;
        private StoredProperties storedProps = null;
        private Book.Section[] sections = new Book.Section[0];
    private final ListUtils.FixedModel notesModel = new ListUtils.FixedModel();

    App2()
    {
	super(Strings.NAME, Strings.class);
    }

    @Override public boolean onAppInit()
    {
	/*
	final AudioPlaying a = new AudioPlaying(luwrain);
		this.audioPlaying = a.isLoaded()?a:null;
	*/
	return false;
    }

    boolean openInitial(URL url, String contentType)
    {
	/*
	NullCheck.notNull(url, "url");
	NullCheck.notNull(contentType, "contentType");
	if (isInBookMode())
	{
	    luwrain.launchApp("reader", new String[]{url.toString()});
	    return true;
	}
	if (isBusy())
	    return false;
	final UrlLoader urlLoader;
	try {
	    urlLoader = new UrlLoader(luwrain, url);
	}
	catch(MalformedURLException e)
	{
	    luwrain.crash(e);
	    return false;
	    	}
	if (StoredProperties.hasProperties(luwrain.getRegistry(), url.toString()))
	{
	    final StoredProperties props = new StoredProperties(luwrain.getRegistry(), url.toString());
	    if (!props.getCharset().isEmpty())
		urlLoader.setCharset(props.getCharset());
	    	final ParaStyle paraStyle = translateParaStyle(props.getParaStyle());
		if (paraStyle != null)
		    urlLoader.setTxtParaStyle(paraStyle);
	}
	if (!contentType.isEmpty())
	    urlLoader.setContentType(contentType);
	task = createTask(urlLoader);
	luwrain.executeBkg(task);
	*/
	return true;
    }

        boolean changeCharset(String newCharset)
    {
	NullCheck.notNull(newCharset, "newCharset");
	/*
	if (isInBookMode() || isBusy() || !hasDocument())
	    return false;
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

    boolean changeTextParaStyle(ParaStyle newParaStyle)
    {
	NullCheck.notNull(newParaStyle, "newParaStyle");
	/*
	if (isInBookMode() || isBusy() || !hasDocument())
	    return false;
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
	storedProps.setParaStyle(newParaStyle.toString());
	urlLoader.setTxtParaStyle(newParaStyle);
	if (!storedProps.getCharset().isEmpty())
	    urlLoader.setCharset(storedProps.getCharset());
	task = createTask(urlLoader);
	luwrain.executeBkg(task);
	*/
	return true;
    }



    boolean jumpByHrefInNonBook(String href, int currentRowIndex)
    {
	NullCheck.notEmpty(href, "href");
	/*
	if (isInBookMode())
	    throw new RuntimeException("May not be in book mode");
	NullCheck.notNull(res.doc, "res.doc");
	    if (isBusy())
	    return false;
		final URL url;
	try {
	    url = new URL(href);
	}
	catch(MalformedURLException e)
	{
	    luwrain.message(strings.badUrl() + href, Luwrain.MessageType.ERROR);
	    return true;
	}
	history.add(new HistoryItem(res.doc));
	if (!openInitial(url, ""/*, currentRowIndex))
	    return false;
	luwrain.message(strings.fetching() + " " + href, Luwrain.MessageType.NONE);
	    */
	return true;
    }

    boolean onPrevDoc()
    {
	/*
	if (isBusy())
	    return false;
	if (history.isEmpty())
	    return false;
	final HistoryItem item = history.pollLast();
	    res.doc = item.doc;
successNotification.run();
	return true;
	*/
	return true;
    }

    Document jumpByHrefInBook(String href, int lastPos, int newDesiredPos)
    {
	NullCheck.notEmpty(href, "href");
	/*
	if (!isInBookMode() || isBusy())
	    return null;
	final Document doc = res.book.getDocument(href);
	if (doc == null)
	    return null;
	if (doc != res.doc)
	{
	    if (lastPos >= 0 && !history.isEmpty())
		history.getLast().lastRowIndex = lastPos;
	    history.add(new HistoryItem(doc));
	}
	res.doc = doc;
	if (newDesiredPos >= 0)
	    res.doc.setProperty(Document.DEFAULT_ITERATOR_INDEX_PROPERTY, "" + newDesiredPos);
	return doc;
	*/
	return null;
    }

    /*
    private FutureTask createTask(UrlLoader urlLoader)
    {
	NullCheck.notNull(urlLoader, "urlLoader");
	return new FutureTask(()->{
		try {
		    final UrlLoader.Result r = urlLoader.load();
		    if (r != null)
			luwrain.runUiSafely(()->{
				task = null; //the Strong mark that the work is done
				    onNewLoadingRes(r);
				    successNotification.run();
			    });
		}
		catch(Throwable e)
		{
		    Log.error("reader", "unable to fetch:" + e.getClass().getName() + ":" + e.getMessage());
		    final Properties props = new Properties();
		    props.setProperty("url", urlLoader.requestedUrl.toString());
		    props.setProperty("contentType", urlLoader.getContentType());
		    props.setProperty("charset", urlLoader.getCharset());
		    luwrain.runUiSafely(()->errorHandler.accept(props, e));
		}
	}, null);
    }
    */

    /*
        private void onNewLoadingRes(UrlLoader.Result newRes)
    {
	NullCheck.notNull(newRes, "newRes");
	if (isInBookMode() && res.book != null)
	    throw new RuntimeException("Cannot open the new book being in book mode");
	this.res = newRes;
	if (res.book != null)
	{
	    this.sections = res.book.getBookSections();
	    res.doc = res.book.getStartingDocument();
	    history.clear();
	    newBookNotification.run();
	}
	NullCheck.notNull(res.doc, "res.doc");
	if (res.doc.getProperty("url").matches("http://www\\.google\\.ru/search.*"))
	    Visitor.walk(res.doc.getRoot(), new org.luwrain.app.reader.filters.GDotCom());
	if (StoredProperties.hasProperties(luwrain.getRegistry(), res.doc.getUrl().toString ()))
	{
	    this.storedProps = new StoredProperties(luwrain.getRegistry(), res.doc.getUrl().toString());
	    final int savedPosition = storedProps.getBookmarkPos();
	if (savedPosition > 0)
	    res.doc.setProperty(Document.DEFAULT_ITERATOR_INDEX_PROPERTY, "" + savedPosition);
	}
	res.doc.commit();
    }
    */


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


    BookContainer getBookContainer()
    {
	return this.bookContainer;
    }


    @Override public AreaLayout getDefaultAreaLayout()
    {
	return null;
    }
}

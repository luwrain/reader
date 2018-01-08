
package org.luwrain.app.opds;

import java.util.*;
import java.net.*;
import java.util.concurrent.*;
import javax.activation.*;

import org.luwrain.core.*;
import org.luwrain.controls.*;
import org.luwrain.util.Opds;
import org.luwrain.util.Opds.Link;
import org.luwrain.util.Opds.Entry;

class Base
{
    static private final String CONTENT_TYPE_FB2_ZIP = org.luwrain.doctree.loading.UrlLoader.CONTENT_TYPE_FB2_ZIP;

    static final String PROFILE_CATALOG = "opds-catalog";
    static final String BASE_TYPE_CATALOG = "application/atom+xml";
    static final String PRIMARY_TYPE_IMAGE = "image";

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private Luwrain luwrain;
    private Strings strings;
    private FutureTask task;
    private RemoteLibrary[] libraries;
    private final ListUtils.FixedModel librariesModel = new ListUtils.FixedModel();
    private final ListUtils.FixedModel model = new ListUtils.FixedModel();
    private final LinkedList<HistoryItem> history = new LinkedList<HistoryItem>();

    boolean init(Luwrain luwrain, Strings strings)
    {
	NullCheck.notNull(luwrain, "luwrain");
	NullCheck.notNull(strings, "strings");
	this.luwrain = luwrain;
	this.strings = strings;
	loadLibraries();
	model.setItems(libraries);
	return true;
    }

    boolean start(OpdsApp app, URL url)
    {
	NullCheck.notNull(app, "app");
	NullCheck.notNull(url, "url");
	if (task != null && !task.isDone())
	    return false;
	task = new FutureTask<Opds.Result>(()->{
		final Opds.Result res = Opds.fetch(url);
		luwrain.runInMainThread(()->onFetchResult(url, app, res));
	    }, null);
	Log.debug("opds", "starting fetching:" + url.toString());
	executor.execute(task);
	//	history.add(url);
	model.clear();
	app.updateAreas();
	return true;
    }

    boolean returnBack(OpdsApp app)
    {
	NullCheck.notNull(app, "app");
	if (history.isEmpty() ||
	    (task != null && !task.isDone()))
	    return false;
	if (history.size() == 1)
	{
	    history.clear();
	    model.setItems(libraries);
	    return true ;
	}
	history.pollLast();
	//	task = constructTask(app, history.getLast().url);
	executor.execute(task);
	model.clear();
	return true;
    }

    private void onFetchResult(URL url, OpdsApp app, Opds.Result res)
    {
	NullCheck.notNull(url, "url");
	NullCheck.notNull(app, "app");
	NullCheck.notNull(res, "res");
	Log.debug("opds", "fetching result:" + res.getError().toString());
	task = null;
	app.updateAreas();
	if (res.getError() == Opds.Result.Errors.FETCHING_PROBLEM)
	{
	    luwrain.message("Невозможно подключиться к серверу или данные по указанному адресу не являются правильным OPDS-каталогом", Luwrain.MessageType.ERROR);
	    return;
	}
	if(res.hasEntries())
	{
	    Log.debug("opds", "" + res.getEntries().length + " entries");
	    model.setItems(res.getEntries());
	    history.add(new HistoryItem(url));
	    luwrain.playSound(Sounds.INTRO_REGULAR);
	}
    }

    private void loadLibraries()
    {
	libraries = new RemoteLibrary[0];
	final Registry registry = luwrain.getRegistry();
	registry.addDirectory(Settings.LIBRARIES_PATH);
	final LinkedList<RemoteLibrary> res = new LinkedList<RemoteLibrary>();
	for(String s: registry.getDirectories(Settings.LIBRARIES_PATH))
	{
	    final RemoteLibrary l = new RemoteLibrary(registry, Registry.join(Settings.LIBRARIES_PATH, s));
	    if (!l.url.isEmpty())
		res.add(l);
	}
	libraries = res.toArray(new RemoteLibrary[res.size()]);
	Arrays.sort(libraries);
	librariesModel.setItems(libraries);
    }

    boolean isFetchingInProgress()
    {
	return task != null && !task.isDone();
    }

    URL getCurrentUrl()
    {
	Log.debug("opds", "history has " + history.size() + " items");
	return !history.isEmpty()?history.getLast().url:null;
    }

    void onEntry(OpdsApp app, Opds.Entry entry) throws MalformedURLException
    {
	NullCheck.notNull(app, "app");
	NullCheck.notNull(entry, "entry");
	if (openBook(entry))
	    return;
	final Opds.Link catalogLink = getCatalogLink(entry);
	if (catalogLink == null)
	    return;
	start(app, new URL(getCurrentUrl(), catalogLink.url));
    }

    private boolean openBook(Entry entry)
    {
	NullCheck.notNull(entry, "entry");
	for(Link link:entry.links)
	{
	    if (link.type.toLowerCase().equals(CONTENT_TYPE_FB2_ZIP))
	    {
		launchReader(prepareUrl(link.url).toString(), link.type);
		return true;
	    }
	}
	return false;
	/*
	final LinkedList<Opds.Link> s = new LinkedList<Opds.Link>();
	for(Opds.Link link: entry.links)
	{
	    if (isCatalog(link) || isImage(link))
		continue;
	    s.add(link);
	}
	final Opds.Link[] suitable = s.toArray(new Opds.Link[s.size()]);
	if (suitable.length == 1)
	{
	    openReader(new URL(getCurrentUrl(), suitable[0].url.toString()), suitable[0].type);
	    return;
	}
	for(Opds.Link link: suitable)
	    if (link.type.equals("application/fb2+zip") ||
		link.type.equals("application/fb2"))
	    {
		openReader(new URL(getCurrentUrl(), link.url.toString()), link.type);
		return;
	    }
	luwrain.message(strings.noSuitableLinksInEntry(), Luwrain.MESSAGE_ERROR);
	*/
    }

    void launchReader(String url, String contentType)
    {
	NullCheck.notEmpty(url, "url");
	NullCheck.notNull(contentType, "contentType");
	luwrain.launchApp("reader", new String[]{url, contentType});
    }

    ListArea.Model getLibrariesModel()
    {
	return librariesModel;
    }

    URL prepareUrl(String href)
    {
	final URL currentUrl = getCurrentUrl();
	NullCheck.notNull(currentUrl, "currentUrl");
	try {
	    return new URL(currentUrl, href);
	}
	catch(MalformedURLException e)
	{
	    return null;
	}
    }

    ListArea.Model getModel()
    {
	return model;
    }

    static Link getCatalogLink(Entry entry)
    {
	NullCheck.notNull(entry, "entry");
	for(Link link: entry.links)
	    if (isCatalog(link))
		return link;
	return null;
    }

    static boolean isCatalogOnly(Entry entry)
    {
	NullCheck.notNull(entry, "entry");
	for(Link link: entry.links)
	    if (!isCatalog(link))
		return false;
	return true;
    }

    static boolean hasCatalogLinks(Entry entry)
    {
	NullCheck.notNull(entry, "entry");
	for(Link link: entry.links)
	    if (isCatalog(link))
		return true;
	return false;
    }

    static boolean hasBooks(Entry entry)
    {
	NullCheck.notNull(entry, "entry");
	for(Link link: entry.links)
	    if (!isCatalog(link) && !isImage(link))
		return true;
	return false;
    }

    static boolean isCatalog(Link link)
    {
	NullCheck.notNull(link, "link");
	if (getTypeProfile(link).toLowerCase().equals(PROFILE_CATALOG))
	    return true;
	return getBaseType(link).equals(BASE_TYPE_CATALOG);
    }

    static boolean isImage(Link link)
    {
	NullCheck.notNull(link, "link");
	return getPrimaryType(link).toLowerCase().trim().equals(PRIMARY_TYPE_IMAGE);
    }

    //Never returns null
    static String getBaseType(Link link)
    {
	NullCheck.notNull(link, "link");
	if (link.type == null)
	    return "";
	try {
	    final MimeType mime = new MimeType(link.type);
	    final String value = mime.getBaseType();
	    return value != null?value:"";
	}
	catch(MimeTypeParseException e)
	{
	    e.printStackTrace();
	    return "";
	}
    }

    //Never returns null
    static String getPrimaryType(Link link)
    {
	NullCheck.notNull(link, "link");
	if (link.type == null)
	    return "";
	try {
	    final MimeType mime = new MimeType(link.type);
	    final String value = mime.getPrimaryType();
	    return value != null?value:"";
	}
	catch(MimeTypeParseException e)
	{
	    e.printStackTrace();
	    return "";
	}
    }

    //Never returns null
    static String getSubType(Link link)
    {
	NullCheck.notNull(link, "link");
	if (link.type == null)
	    return "";
	try {
	    final MimeType mime = new MimeType(link.type);
	    final String value = mime.getSubType();
	    return value != null?value:"";
	}
	catch(MimeTypeParseException e)
	{
	    e.printStackTrace();
	    return "";
	}
    }

    //Never returns null
    static String getTypeProfile(Link link)
    {
	NullCheck.notNull(link, "link");
	if (link.type == null)
	    return "";
	try {
	    final MimeType mime = new MimeType(link.type);
	    final String value = mime.getParameter("profile");
	    return value != null?value:"";
	}
	catch(MimeTypeParseException e)
	{
	    e.printStackTrace();
	    return "";
	}
    }
}

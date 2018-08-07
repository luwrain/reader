
package org.luwrain.app.reader.books;

import java.net.*;
import java.util.*;
import java.io.*;
import java.nio.file.*;

import org.luwrain.core.NullCheck;
import org.luwrain.core.Log;
import org.luwrain.doctree.*;
import org.luwrain.app.reader.loading.*;
import org.luwrain.util.*;

class Daisy2 implements Book
{
    protected final UrlLoaderFactory urlLoaderFactory;
    protected final HashMap<URL, Document> docs = new HashMap<URL, Document>();
    protected final HashMap<URL, Smil.Entry> smils = new HashMap<URL, Smil.Entry>();
    protected Document nccDoc;
    protected URL nccDocUrl;
    protected Book.Section[] bookSections = new Book.Section[0];

    Daisy2(UrlLoaderFactory urlLoaderFactory)
    {
	NullCheck.notNull(urlLoaderFactory, "urlLoaderFactory");
	this.urlLoaderFactory = urlLoaderFactory;
    }

    @Override public Document[] getDocuments()
    {
	final LinkedList<Document> res = new LinkedList<Document>();
	for(Map.Entry<URL, Document> e: docs.entrySet())
	    res.add(e.getValue());
	return res.toArray(new Document[res.size()]);
    }

    @Override public Map<URL, Document> getDocumentsWithUrls()
    {
	return docs;
    }

    @Override public Document getStartingDocument()
    {
	return nccDoc;
    }

    /*
    @Override public Note createNote(Document doc, int rowIndex)
    {
	NullCheck.notNull(doc, "doc");
	final String localPath = doc.getProperty("daisy.localpath");
	if (localPath != null && !localPath.isEmpty())
	return new Note(localPath, rowIndex);
	return null;
    }
    */

    /*
@Override public     String getHrefOfNoteDoc(Note note)
    {
	NullCheck.notNull(note, "note");
	final String id = note.docId();
	final String nccLocalPath = nccDoc.getProperty("daisy.localpath");
	if (nccLocalPath != null && id.equals(nccLocalPath))
	    return nccDoc.getProperty("url").toString();
	for(Map.Entry<URL, Document> e: docs.entrySet())
	{
	    final String url = e.getKey().toString();
	    final String localPath = e.getValue().getProperty("daisy.localpath");
	    if (localPath != null && id.equals(localPath))
		return url;
	}
	return "";
    }
    */

    @Override public Document getDocument(String href)
    {
	NullCheck.notNull(href, "href");
	final URL url;
	final URL noRefUrl;
	try {
	    url = new URL(href);
	    noRefUrl = new URL(url.getProtocol(), url.getHost(), url.getPort(), url.getFile());
	}
	catch(MalformedURLException e)
	{
	    e.printStackTrace();
	    return null;
	}
	if (smils.containsKey(noRefUrl))
	{
	    final Smil.Entry entry = smils.get(noRefUrl);
	    final Smil.Entry requested = entry.findById(url.getRef());
	    if (requested != null)
	    {
		Log.debug("doctree", "requested entry type is " + requested.type());
		if (requested.type() == Smil.Entry.Type.PAR || requested.type() == Smil.Entry.Type.SEQ)
		{
		    final LinkedList<String> links = new LinkedList<String>();
		    collectTextStartingAtEntry(requested, links);
		    Log.debug("doctree", "collected " + links.size() + " entries");
		    if (!links.isEmpty())
		    {
			final String link = links.getFirst();
			Log.debug("doctree", "using link " + link);
			return getDocument(link);
		    }
		    Log.debug("doctree", "nothing found in SMILs");
		    return null;
		} else
		if (requested.type() == Smil.Entry.Type.TEXT)
		    return getDocument(requested.src()); else
		{
		    Log.warning("doctree-daisy", "URL " + href + " points to a SMIL entry, but its type is " + requested.type());
		    return null;
		}
	    }
	} //smils;
	if (docs.containsKey(noRefUrl))
	{
	    final Document res = docs.get(noRefUrl);
	    if (res != null && url.getRef() != null)
		res.setProperty("startingref", url.getRef()); else
		res.setProperty("startingref", "");
	    return res;
	}
	if (nccDoc.getUrl().equals(url))
	{
	    if (url.getRef() != null)
		nccDoc.setProperty("startingref", url.getRef()); else
		nccDoc.setProperty("startingref", "");
	    return nccDoc;
	}
	Log.warning("doctree", "unable to find a document in Daisy2 book for URL:" + url.toString());
	return null;
    }

    @Override public Document openHref(String href)
    {
	return null;
    }

    @Override public AudioFragment findAudioForId(String id)
    {
	NullCheck.notNull(id, "id");
	Log.debug("doctree-daisy", "searching audio for " + id);
	for(Map.Entry<URL, Smil.Entry> e: smils.entrySet())
	{
	    final Smil.Entry entry = findSmilEntryWithText(e.getValue(), id);
	    if (entry != null)
	    {
		final LinkedList<AudioFragment> infos = new LinkedList();
		collectAudioStartingAtEntry(entry, infos);
		if (infos.size() > 0)
		    return infos.getFirst();
	    }
	}
	return null;
    }

    @Override public     String findTextForAudio(String audioFileUrl, long msec)
    {
	NullCheck.notNull(audioFileUrl, "audioFileUrl");
	Log.debug("doctree-daisy", "text for " + audioFileUrl + " at " + msec);
	for(Map.Entry<URL, Smil.Entry> e: smils.entrySet())
	{
	    final Smil.Entry entry = findSmilEntryWithAudio(e.getValue(), audioFileUrl, msec);
	    if (entry != null)
	    {
		final LinkedList<String> links = new LinkedList<String>();
		collectTextStartingAtEntry(entry, links);
		if (links.size() > 0)
		    return links.getFirst();
	    }
	}
	return null;
    }

    void init(Document nccDoc)
    {
	NullCheck.notNull(nccDoc, "nccDoc");
	nccDoc.setProperty("daisy.localpath", nccDoc.getUrl().getFile());//FIXME:Leave only base file name
	final String[] allHrefs = nccDoc.getHrefs();
	final LinkedList<String> textSrcs = new LinkedList<String>();
	for(String h: allHrefs)
	    try {
		URL url = new URL(nccDoc.getUrl(), h);
		url = new URL(url.getProtocol(), url.getHost(), url.getPort(), url.getFile());
		if (url.getFile().toLowerCase().endsWith(".smil"))
		    loadSmil(url, textSrcs); else
		    textSrcs.add(h);
	    }
	    catch(MalformedURLException e)
	    {
		e.printStackTrace();
	    }
	Log.debug("doctree-daisy", "" + smils.size() + " SMIL(s) loaded");
	for(String s: textSrcs)
	    try {
		URL url = new URL(nccDoc.getUrl(), s);
		url = new URL(url.getProtocol(), url.getHost(), url.getPort(), url.getFile());
		loadDoc(s, url);
	    }
	    catch(MalformedURLException e)
	    {
		e.printStackTrace();
	    }
	Log.debug("doctree-daisy", "" + docs.size() + " documents loaded");
	this.nccDoc = nccDoc;
	this.nccDocUrl = this.nccDoc.getUrl();
	final SectionsVisitor visitor = new SectionsVisitor();
	Visitor.walk(nccDoc.getRoot(), visitor);
	final Book.Section[] sections = visitor.getBookSections();
	for(int i = 0;i < sections.length;++i)
	{
	    try {
		final URL url = new URL(sections[i].href);
		final URL fileUrl = new URL(url.getProtocol(), url.getHost(), url.getPort(), url.getFile());
		if (fileUrl.getFile().toLowerCase().endsWith(".smil") && !url.getRef().isEmpty())
		{
		    final String text = smilEntryToText(fileUrl, url.getRef());
		    if (text != null)
			sections[i] = new Book.Section(sections[i].level, sections[i].title, text);
		}
	    }
	    catch(MalformedURLException e)
	    {
		e.printStackTrace();
	    }
	}
	this.bookSections = sections;
	/*
	try {
	    this.bookPath = Paths.get(nccDoc.getUrl().toURI()).getParent().resolve("luwrain.book");
	}
	catch(URISyntaxException e)
	{
	    e.printStackTrace();
	    bookPath = null;
	}
	if (bookPath != null)
	    Log.debug("doctree-daisy", "book path set to " + bookPath.toString()); else
	    Log.debug("doctree-daisy", "book path isn\'t set");
	*/
    }

    @Override public Book.Section[] getBookSections()
    {
	return bookSections;
    }

    private void loadSmil(URL url, LinkedList<String> textSrcs)
    {
	NullCheck.notNull(url, "url");
	if (smils.containsKey(url))
	    return;
	final Smil.Entry smil = Smil.fromUrl(url);
	if (smil == null)
	{
	    Log.error("doctree-daisy", "unable to read SMIL " + url.toString());
	}
	smils.put(url, smil);
	smil.saveTextSrc(textSrcs);
	try {
	    smil.allSrcToUrls(url); 
	}
	catch(MalformedURLException e)
	{
	    e.printStackTrace();
	}
    }

    private void loadDoc(String localPath, URL url)
    {
	if (docs.containsKey(url))
	    return;
	UrlLoader.Result res;
	try {
res = loadDoc(url);
	}
	catch(Exception e)
	{
	    Log.error("doctree-daisy", "unable to read a document from URL " + url.toString());
	    e.printStackTrace();
	    return;
	}
	if (res.type() != UrlLoader.Result.Type.OK)
	{
	    Log.warning("doctree-daisy", "unable to load a document by URL " + url + ":" + res.toString());
	    return;
	}
	if (res.book() != null)
	{
	    Log.debug("doctree-daisy", "the URL " + url + "references a book, not including to current one");
	    return;
	}
	res.doc().setProperty("daisy.localpath", localPath);
	docs.put(url, res.doc());
    }

    static private Smil.Entry findSmilEntryWithText(Smil.Entry entry, String src)
    {
	NullCheck.notNull(entry, "entry");
	NullCheck.notNull(src, "src");
	switch(entry.type() )
	{
	case TEXT:
	    return (entry.src() != null && entry.src().equals(src))?entry:null;
	case AUDIO:
	    return null;
	case FILE:
	case SEQ:
	    if (entry.entries() == null)
		return null;
	    for (int i = 0;i < entry.entries().length;++i)
	    {
		final Smil.Entry res = findSmilEntryWithText(entry.entries()[i], src);
		if (res == null)
		    continue;
		if (res != entry.entries()[i])
		    return res;
		if (i == 0)
		    return entry;
		return entry.entries()[i];
	    }
	    return null;
	case PAR:
	    if (entry.entries() == null)
		return null;
	    for(Smil.Entry e: entry.entries())
	    {
		final Smil.Entry res = findSmilEntryWithText(e, src);
		if (res != null)
		    return entry;
	    }
	    return null;
	default:
	    Log.warning("doctree-daisy", "unknown SMIL entry type:" + entry.type());
	    return null;
	}
    }

private Smil.Entry findSmilEntryWithAudio(Smil.Entry entry, String audioFileUrl, long msec)
    {
	NullCheck.notNull(entry, "entry");
	NullCheck.notNull(audioFileUrl, "audioFileUrl");
	switch(entry.type() )
	{
	case AUDIO:
	    return entry.getAudioFragment().covers(audioFileUrl, msec, nccDocUrl)?entry:null;
	case TEXT:
	    return null;
	case FILE:
	case SEQ:
	    if (entry.entries() == null)
		return null;
	    for (int i = 0;i < entry.entries().length;++i)
	    {
		final Smil.Entry res = findSmilEntryWithAudio(entry.entries()[i], audioFileUrl, msec);
		if (res == null)
		    continue;
		if (res != entry.entries()[i])
		    return res;
		if (i == 0)
		    return entry;
		return entry.entries()[i];
	    }
	    return null;
	case PAR:
	    if (entry.entries() == null)
		return null;
	    for(Smil.Entry e: entry.entries())
	    {
		final Smil.Entry res = findSmilEntryWithAudio(e, audioFileUrl, msec);
		if (res != null)
		    return entry;
	    }
	    return null;
	default:
	    Log.warning("doctree-daisy", "unknown SMIL entry type:" + entry.type());
	    return null;
	}
    }

    static private void collectAudioStartingAtEntry(Smil.Entry entry, LinkedList<AudioFragment> audioInfos)
    {
	NullCheck.notNull(entry, "entry");
	NullCheck.notNull(audioInfos, "audioInfos");
	switch(entry.type())
	{
	case AUDIO:
	    audioInfos.add(entry.getAudioFragment());
	    return;
	case TEXT:
	    return;
	case PAR:
	    if (entry.entries() != null)
		for(Smil.Entry e: entry.entries())
		    collectAudioStartingAtEntry(e, audioInfos);
	    return;
	case FILE:
	case SEQ:
	    if (entry.entries() != null &&
		 entry.entries().length >= 1)
		collectAudioStartingAtEntry(entry.entries()[0], audioInfos);
	    return;
	default:
	    Log.warning("doctree-daisy", "unknown SMIL entry type:" + entry.type());
	}
    }

    static private void collectTextStartingAtEntry(Smil.Entry entry, LinkedList<String> links)
    {
	NullCheck.notNull(entry, "entry");
	NullCheck.notNull(links, "links");
	switch(entry.type())
	{
	case AUDIO:
	    return;
	case TEXT:
	    links.add(entry.src());
	    return;
	case PAR:
	    if (entry.entries() != null)
		for(Smil.Entry e: entry.entries())
		    collectTextStartingAtEntry(e, links);
	    return;
	case FILE:
	case SEQ:
	    if (entry.entries() != null &&
		 entry.entries().length >= 1)
		collectTextStartingAtEntry(entry.entries()[0], links);
	    return;
	default:
	    Log.warning("doctree-daisy", "unknown SMIL entry type:" + entry.type());
	}
    }

    private String smilEntryToText(URL url, String id)
    {
		    if (!smils.containsKey(url))
			return null;
			final Smil.Entry entry = smils.get(url).findById(id);
			if (entry == null)
			    return null;
			final LinkedList<String> links = new LinkedList<String>();
			collectTextStartingAtEntry(entry, links);
			return !links.isEmpty()?links.getFirst():null;
		    }

    private UrlLoader.Result loadDoc(URL url) throws MalformedURLException, IOException
    {
final UrlLoader loader = urlLoaderFactory.newUrlLoader(url);
return loader.load();
    }
}

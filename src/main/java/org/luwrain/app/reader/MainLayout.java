
package org.luwrain.app.reader;

import java.util.*;
import java.net.*;

import org.luwrain.core.*;
import org.luwrain.core.events.*;
import org.luwrain.core.queries.*;
import org.luwrain.controls.*;
import org.luwrain.reader.*;
import org.luwrain.controls.reader.*;
import org.luwrain.template.*;

final class MainLayout extends LayoutBase
{
        private final TreeArea treeArea;
    private final ReaderArea readerArea;
    private final ListArea notesArea;
    
    MainLayout(App app)
    {
	this.treeArea = new TreeArea(createTreeParams()) {
		@Override public boolean onInputEvent(KeyboardEvent event)
		{
		    NullCheck.notNull(event, "event");
		    return super.onInputEvent(event);
		}
		@Override public boolean onSystemEvent(EnvironmentEvent event)
		{
		    NullCheck.notNull(event, "event");
			return super.onSystemEvent(event);
		}
		@Override public Action[] getAreaActions()
		{
		    return null;
		}
	    };
	this.readerArea = new ReaderArea(createReaderParams()){
		@Override public boolean onInputEvent(KeyboardEvent event)
		{
		    NullCheck.notNull(event, "event");
		    return super.onInputEvent(event);
		}
		@Override public boolean onSystemEvent(EnvironmentEvent event)
		{
		    NullCheck.notNull(event, "event");
			return super.onSystemEvent(event);
		}
		@Override public boolean onAreaQuery(AreaQuery query)
		{
		    NullCheck.notNull(query, "query");
			return super.onAreaQuery(query);
		}
		@Override public Action[] getAreaActions()
		{
		    return new Action[0];
		    		}
		@Override public String getAreaName()
		{
		    /*
		    if (!base.hasDocument())
			return strings.appName();
		    */
		    //		    return base.getDocument().getTitle();
		    return "";
		}
		    @Override public String getDocUniRef()
    {
	/*
	final String addr = getDocUrl();
	if (addr.isEmpty())
	    return "";
	return UniRefUtils.makeUniRef("reader", addr);
	*/
	return null;
    }
		@Override protected String noContentStr()
		{
		    /*
		    return base.isBusy()?strings.noContentFetching():strings.noContent();
		    */
		    return "";
		}
	    };
	this.notesArea = new ListArea(createNotesParams()) {
		@Override public boolean onInputEvent(KeyboardEvent event)
		{
		    NullCheck.notNull(event, "event");
		    return super.onInputEvent(event);
		}
		@Override public boolean onSystemEvent(EnvironmentEvent event)
		{
		    NullCheck.notNull(event, "event");
			return super.onSystemEvent(event);
		}
		@Override public Action[] getAreaActions()
		{
		    return new Action[0];
		}
	    };
    }

    private TreeArea.Params createTreeParams()
    {
	return null;
    }

    final ReaderArea.Params createReaderParams()
    {
    	final ReaderArea.Params params = new ReaderArea.Params();
	/*
	readerParams.context = new DefaultControlContext(luwrain);
	readerParams.clickHandler = (area,run)->{
	    NullCheck.notNull(area, "area");
	    NullCheck.notNull(run, "run");
	    if (!run.href().isEmpty())
		return jumpByHref(run.href(), luwrain.getAreaVisibleWidth(area));
	    return actions.onPlayAudio(area);
	};
	*/
	return params;
    }

    private int getSuitableWidth()
    {
	/*
	final int areaWidth = luwrain.getAreaVisibleWidth(readerArea);
	final int screenWidth = luwrain.getScreenWidth();
	int width = areaWidth;
	if (width < 80)
	    width = screenWidth;
	if (width < 80)
	    width = 80;
	return width;
	*/
	return -1;
    }

    private boolean addNote()
    {
	/*
	if (!base.hasDocument())
	    return false;
	if (!base.addNote(readerArea.getCurrentRowIndex()))
	    return true;
	notesArea.refresh();
	*/
	return true;
    }

    private boolean onDeleteNote()
    {
	/*
	if (notesArea.selected() == null || !(notesArea.selected() instanceof Note))
	    return false;
	final Note note = (Note)notesArea.selected();
	if (!Popups.confirmDefaultNo(luwrain, "Удаление закладки", "Вы действительно хотите удалить закладку \"" + note.comment + "\"?"))
	    return true;
	base.deleteNote(note);
	notesArea.refresh();
	*/
	return true;
    }

    private ListArea.Params createNotesParams()
    {
	return null;
    }

    /*
    boolean addNote(int pos)
    {
	NullCheck.notNull(res.doc, "res.doc");
	final String text = Popups.simple(luwrain, strings.addNotePopupName(), strings.addNotePopupPrefix(), "");
	if (text == null)
	    return false;
	Settings.addNote(luwrain.getRegistry(), getNotesUrl().toString(), res.doc.getUrl().toString(), pos, text, "");
	updateNotesModel();
	return true;
    }

    void deleteNote(Note note)
    {
	NullCheck.notNull(note, "note");
	Settings.deleteNote(luwrain.getRegistry(), getNotesUrl().toString(), note.num);
	updateNotesModel();
    }

    */

    /*
    TreeArea.Params createTreeParams(TreeArea.ClickHandler clickHandler)
    {
	NullCheck.notNull(clickHandler, "clickHandler");

	final TreeArea.Params params = new TreeArea.Params();
	params.context = new DefaultControlContext(luwrain);
	params.model = new CachedTreeModel(new BookTreeModelSource(strings.bookTreeRoot()));
	params.name = strings.treeAreaName();
	params.clickHandler = clickHandler;
	return params;
    }

    ListArea.Params createNotesListParams(ListArea.ClickHandler clickHandler)
    {
	NullCheck.notNull(clickHandler, "clickHandler");
	final ListArea.Params params = new ListArea.Params();
	params.context = new DefaultControlContext(luwrain);
	params.model = notesModel;
	params.appearance = new ListUtils.DefaultAppearance(params.context, Suggestions.LIST_ITEM);
	params.clickHandler = clickHandler;
	params.name = strings.notesAreaName();
	return params;
    }

    */

    /*
    private final class BookTreeModelSource implements CachedTreeModelSource
{
    private final String root;
    BookTreeModelSource(String root)
    {
	NullCheck.notNull(root, "root");
	this.root = root;
    }
    @Override public Object getRoot()
    {
	return root;
    }
    @Override public Object[] getChildObjs(Object obj)
    {
	final List res = new LinkedList();
	if (obj == root)
	{
	    for(Book.Section s: sections)
		if (s.level == 1)
		    res.add(s);
	} else
	{
	    int i = 0;
	    for(i = 0;i < sections.length;++i)
		if (sections[i] == obj)
		    break;
	    if (i < sections.length)
	    {
		final Book.Section sect = sections[i];
		for(int k = i + 1;k < sections.length;++k)
		{
		    if (sections[k].level <= sect.level)
			break;
		    if (sections[k].level == sect.level + 1)
			res.add(sections[k]);
		}
	    }
	}
	return res.toArray(new Object[res.size()]);
    }
}

    */
}

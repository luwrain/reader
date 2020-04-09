
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

}

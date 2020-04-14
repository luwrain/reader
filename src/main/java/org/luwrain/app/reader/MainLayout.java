/*
   Copyright 2012-2020 Michael Pozhidaev <msp@luwrain.org>
   Copyright 2015-2016 Roman Volovodov <gr.rPman@gmail.com>

   This file is part of LUWRAIN.

   LUWRAIN is free software; you can redistribute it and/or
   modify it under the terms of the GNU General Public
   License as published by the Free Software Foundation; either
   version 3 of the License, or (at your option) any later version.

   LUWRAIN is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
   General Public License for more details.
*/

package org.luwrain.app.reader;

import java.util.*;
import java.net.*;

import org.luwrain.core.*;
import org.luwrain.core.events.*;
import org.luwrain.core.queries.*;
import org.luwrain.controls.*;
import org.luwrain.reader.*;
import org.luwrain.controls.reader.*;
import org.luwrain.app.reader.books.*;
import org.luwrain.template.*;

final class MainLayout extends LayoutBase implements TreeArea.ClickHandler, ReaderArea.ClickHandler
{
    private App app;
    private final TreeArea treeArea;
    private final ReaderArea readerArea;
    private final ListArea notesArea;

    MainLayout(App app)
    {
	NullCheck.notNull(app, "app");
	this.app = app;
	this.treeArea = new TreeArea(createTreeParams()) {
		@Override public boolean onInputEvent(KeyboardEvent event)
		{
		    NullCheck.notNull(event, "event");
		    if (app.onInputEvent(this, event))
			return true;
		    return super.onInputEvent(event);
		}
		@Override public boolean onSystemEvent(EnvironmentEvent event)
		{
		    NullCheck.notNull(event, "event");
		    if (app.onSystemEvent(this, event))
			return true;
		    return super.onSystemEvent(event);
		}
				@Override public boolean onAreaQuery(AreaQuery query)
		{
		    NullCheck.notNull(query, "query");
		    if (app.onAreaQuery(this, query))
			return true;
		    return super.onAreaQuery(query);
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
		    if (app.onInputEvent(this, event))
			return true;
		    return super.onInputEvent(event);
		}
		@Override public boolean onSystemEvent(EnvironmentEvent event)
		{
		    NullCheck.notNull(event, "event");
		    if (app.onSystemEvent(this, event))
			return true;
		    return super.onSystemEvent(event);
		}
		@Override public boolean onAreaQuery(AreaQuery query)
		{
		    NullCheck.notNull(query, "query");
		    if (app.onAreaQuery(this, query))
			return true;
		    return super.onAreaQuery(query);
		}
		@Override public Action[] getAreaActions()
		{
		    return new Action[0];
		}
		@Override public String getAreaName()
		{
		    final Document doc = getDocument();
		    if (doc == null)
			return app.getStrings().appName();
		    return doc.getTitle();
		}
		@Override public String getDocUniRef()
		{
		    final String addr = getDocUrl();
		    if (addr.isEmpty())
			return "";
		    return UniRefUtils.makeUniRef("reader", addr);
		}
		@Override protected String noContentStr()
		{
		    return app.isBusy()?app.getStrings().noContentFetching():app.getStrings().noContent();
		}
	    };
	this.notesArea = new ListArea(createNotesParams()) {
		@Override public boolean onInputEvent(KeyboardEvent event)
		{
		    NullCheck.notNull(event, "event");
		    if (app.onInputEvent(this, event))
			return true;
		    return super.onInputEvent(event);
		}
		@Override public boolean onSystemEvent(EnvironmentEvent event)
		{
		    NullCheck.notNull(event, "event");
		    if (app.onSystemEvent(this, event))
			return true;
		    return super.onSystemEvent(event);
		}
		@Override public boolean onAreaQuery(AreaQuery query)
		{
		    NullCheck.notNull(query, "query");
		    if (app.onAreaQuery(this, query))
			return true;
		    return super.onAreaQuery(query);
		}
		@Override public Action[] getAreaActions()
		{
		    return new Action[0];
		}
	    };
    }

    void update()
    {
		this.readerArea.setDocument(app.getBookContainer().getDocument(), app.getLuwrain().getScreenWidth() - 3);//FIXME:proper width
		app.getLuwrain().setActiveArea(this.readerArea);
    }

    @Override public boolean onTreeClick(TreeArea treeArea, Object obj)
    {
	NullCheck.notNull(treeArea, "treeArea");
	NullCheck.notNull(obj, "obj");
		if (!(obj instanceof Book.Section))
	    return false;
	final Book.Section sect = (Book.Section)obj;
return app.getBookContainer().jump(sect.href, 0, 0, ()->{
		    //FIXME:
    });
    }

    @Override public boolean onReaderClick(ReaderArea area, Run run)
    {
	    NullCheck.notNull(area, "area");
	    NullCheck.notNull(run, "run");
	    final String href = run.href();
	    if (!href.isEmpty())
		return app.getBookContainer().jump(href, readerArea.getCurrentRowIndex(), 0, ()->update());
			    //	    return actions.onPlayAudio(area);
	    return false;
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

    private TreeArea.Params createTreeParams()
    {
	final TreeArea.Params params = new TreeArea.Params();
	params.context = new DefaultControlContext(app.getLuwrain());
	params.model = new CachedTreeModel(new BookTreeModelSource());
	params.name = app.getStrings().treeAreaName();
	params.clickHandler = this;
	return params;
    }

        final ReaderArea.Params createReaderParams()
    {
    	final ReaderArea.Params params = new ReaderArea.Params();
	params.context = new DefaultControlContext(app.getLuwrain());
	params.clickHandler = this;
		return params;
    }

    private ListArea.Params createNotesParams()
    {
	final ListArea.Params params = new ListArea.Params();
	params.context = new DefaultControlContext(app.getLuwrain());
	params.model = new ListUtils.FixedModel();
	params.appearance = new ListUtils.DefaultAppearance(params.context, Suggestions.LIST_ITEM);
	//params.clickHandler = clickHandler;
	params.name = app.getStrings().notesAreaName();
	return params;
    }

    AreaLayout getLayout()
    {
	return new AreaLayout(AreaLayout.LEFT_RIGHT, treeArea, readerArea);
    }

    private final class BookTreeModelSource implements CachedTreeModelSource
    {
	private final String root = app.getStrings().bookTreeRoot();
	@Override public Object getRoot()
	{
	    return this.root;
	}
	@Override public Object[] getChildObjs(Object obj)
	{
	    NullCheck.notNull(obj, "obj");
	    final List res = new LinkedList();
	    if (obj == root)
	    {
		for(Book.Section s: app.getBookContainer().getSections())
		    if (s.level == 1)
			res.add(s);
	    } else
	    {
		final Book.Section[] sections = app.getBookContainer().getSections();
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
}

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
import java.io.*;
import java.net.*;

import org.luwrain.core.*;
import org.luwrain.core.events.*;
import org.luwrain.core.queries.*;
import org.luwrain.controls.*;
import org.luwrain.reader.*;
import org.luwrain.controls.reader.*;
import org.luwrain.app.reader.books.*;
import org.luwrain.app.base.*;

final class MainLayout extends LayoutBase implements TreeArea.ClickHandler, ReaderArea.ClickHandler
{
    private final App app;
    private final BookContainer bookContainer;
    private final TreeArea treeArea;
    private final ReaderArea readerArea;
    private final EditableListArea notesArea;

    private boolean sectionsTreeShown = true;
        private boolean notesShown = false;

    MainLayout(App app)
    {
	NullCheck.notNull(app, "app");
	this.app = app;
	this.bookContainer = app.getBookContainer();
	this.sectionsTreeShown = bookContainer.getBookFlags().contains(Book.Flags.OPEN_IN_SECTION_TREE);

	final ActionInfo openFile = action("open-file", app.getStrings().actionOpenFile(), new InputEvent(InputEvent.Special.F3, EnumSet.of(InputEvent.Modifiers.SHIFT)), MainLayout.this::actOpenFile);
		final ActionInfo openUrl = action("open-url", app.getStrings().actionOpenUrl(), new InputEvent(InputEvent.Special.F4, EnumSet.of(InputEvent.Modifiers.SHIFT)), MainLayout.this::actOpenUrl);
				final ActionInfo showSectionsTree = action("show-sections-tree", app.getStrings().actionShowSectionsTree(), new InputEvent(InputEvent.Special.F5), MainLayout.this::actShowSectionsTree);
								final ActionInfo showNotes = action("show-notes", app.getStrings().actionShowNotes(), new InputEvent(InputEvent.Special.F6), MainLayout.this::actShowNotes);

	this.treeArea = new TreeArea(createTreeParams()) {
		final Actions actions = actions(
						openFile, openUrl,
						action("hide-sections-tree", app.getStrings().actionHideSectionsTree(), new InputEvent(InputEvent.Special.F5), MainLayout.this::actHideSectionsTree),
						showNotes
								);
		@Override public boolean onInputEvent(InputEvent event)
		{
		    NullCheck.notNull(event, "event");
		    if (app.onInputEvent(this, event))
			return true;
		    return super.onInputEvent(event);
		}
		@Override public boolean onSystemEvent(SystemEvent event)
		{
		    NullCheck.notNull(event, "event");
		    if (app.onSystemEvent(this, event, actions))
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
		    return actions.getAreaActions();
		}
	    };

	this.readerArea = new ReaderArea(createReaderParams()){
		final Actions actions = actions(
						openFile, openUrl, showSectionsTree, showNotes
						);
		@Override public boolean onInputEvent(InputEvent event)
		{
		    NullCheck.notNull(event, "event");
		    if (app.onInputEvent(this, event))
			return true;
		    return super.onInputEvent(event);
		}
		@Override public boolean onSystemEvent(SystemEvent event)
		{
		    NullCheck.notNull(event, "event");
		    if (app.onSystemEvent(this, event, actions))
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
		    return actions.getAreaActions();
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

	this.notesArea = new EditableListArea(createNotesParams()) {
		final Actions actions = actions(
						action("add-note", app.getStrings().actionAddNote(), new InputEvent(InputEvent.Special.INSERT), MainLayout.this::actAddNote),
						openFile, openUrl, showSectionsTree,
												action("hide-notes", app.getStrings().actionHideNotes(), new InputEvent(InputEvent.Special.F6), MainLayout.this::actHideNotes)
												);
		@Override public boolean onInputEvent(InputEvent event)
		{
		    NullCheck.notNull(event, "event");
		    if (app.onInputEvent(this, event))
			return true;
		    return super.onInputEvent(event);
		}
		@Override public boolean onSystemEvent(SystemEvent event)
		{
		    NullCheck.notNull(event, "event");
		    if (app.onSystemEvent(this, event, actions))
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
		    return actions.getAreaActions();
		}
	    };
    }

    void updateInitial()
    {
	this.readerArea.setDocument(bookContainer.getDocument(), app.getLuwrain().getScreenWidth() - 3);//FIXME:proper width
	if (sectionsTreeShown)
	    	app.getLuwrain().setActiveArea(this.treeArea); else
	app.getLuwrain().setActiveArea(this.readerArea);
    }

    void updateAfterJump()
    {
	this.readerArea.setDocument(bookContainer.getDocument(), app.getLuwrain().getScreenWidth() - 3);//FIXME:proper width
	app.getLuwrain().setActiveArea(this.readerArea);
    }

    private boolean actShowSectionsTree()
    {
	this.sectionsTreeShown = true;
	app.layout(getLayout());
	app.getLuwrain().setActiveArea(treeArea);
	return true;
    }

    private boolean actHideSectionsTree()
    {
	if (!this.sectionsTreeShown)
	    return false;
	this.sectionsTreeShown = false;
	app.layout(getLayout());
		app.getLuwrain().setActiveArea(readerArea);
	return true;
    }

        private boolean actShowNotes()
    {
	this.notesShown = true;
	app.layout(getLayout());
	app.getLuwrain().setActiveArea(notesArea);
	return true;
    }

    private boolean actHideNotes()
    {
	if (!this.notesShown)
	    return false;
	this.notesShown = false;
	app.layout(getLayout());
	app.getLuwrain().setActiveArea(readerArea);
	return true;
    }


    @Override public boolean onTreeClick(TreeArea treeArea, Object obj)
    {
	NullCheck.notNull(treeArea, "treeArea");
	NullCheck.notNull(obj, "obj");
	if (!(obj instanceof Book.Section))
	    return false;
	final Book.Section sect = (Book.Section)obj;
	return bookContainer.jump(sect.href, readerArea, 0, ()->updateAfterJump());
    }

    @Override public boolean onReaderClick(ReaderArea area, Run run)
    {
	NullCheck.notNull(area, "area");
	NullCheck.notNull(run, "run");
	final String href = run.href();
	if (!href.isEmpty())
	    return app.getBookContainer().jump(href, readerArea, 0, ()->updateAfterJump());
	final String[] ids = readerArea.getHtmlIds();
	if (ids == null || ids.length == 0)
	    return false;
	return app.getBookContainer().playAudio(readerArea, ids);
    }

    private boolean actOpenFile()
    {
	final File file = app.conv().fileToOpen();
	if (file == null)
	    return false;
	return true;
    }

    private boolean actOpenUrl()
    {
	final URL url = app.conv().urlToOpen(readerArea.getDocUrl());
	if (url == null)
	    return false;
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

    private boolean actAddNote()
    {
	final String text = app.conv().newNote();
	if (text == null)
	    return false;
	final int selected = notesArea.selectedIndex();
	if (selected >= 0)
	    app.getBookContainer().getAttr().getNotes().add(selected, new Attributes.Note(text)); else
	    app.getBookContainer().getAttr().getNotes().add(new Attributes.Note(text));
	notesArea.refresh();
	return true;
    }

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

    private EditableListArea.Params createNotesParams()
    {
	final EditableListArea.Params params = new EditableListArea.Params();
	params.context = new DefaultControlContext(app.getLuwrain());
	params.model = new NotesModel();
	params.appearance = new ListUtils.DefaultAppearance(params.context, Suggestions.LIST_ITEM);
	params.name = app.getStrings().notesAreaName();
	params.clipboardSaver = (area, model, appearance, fromIndex, toIndex, clipboard)->{
	    final List<Attributes.Note> n = new LinkedList();
	    final List<String> s = new LinkedList();
	    for(int i = fromIndex;i < toIndex;i++)
	    {
		final Attributes.Note note = (Attributes.Note)model.getItem(i);
		n.add(note);
		s.add(note.toString());
	    }
	    clipboard.set(n.toArray(new Attributes.Note[n.size()]), s.toArray(new String[s.size()]));
	    return true;
	};
	return params;
    }

    AreaLayout getLayout()
    {
	if (sectionsTreeShown && notesShown)
	return new AreaLayout(AreaLayout.LEFT_TOP_BOTTOM, treeArea, readerArea, notesArea);
	if (sectionsTreeShown)
	    	return new AreaLayout(AreaLayout.LEFT_RIGHT, treeArea, readerArea);
	if (notesShown)
		return new AreaLayout(AreaLayout.TOP_BOTTOM, readerArea, notesArea);
			return new AreaLayout(readerArea);
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

    private final class NotesModel implements EditableListArea.Model
    {
	@Override public boolean clearModel()
	{
	    return false;
	}
	@Override public boolean addToModel(int pos, java.util.function.Supplier supplier)
	{
	    NullCheck.notNull(supplier, "supplier");
	    final List<Attributes.Note> notes = bookContainer.getAttr().getNotes();
	    if (pos < 0 || pos > notes.size())
		throw new IllegalArgumentException("pos (" + String.valueOf(pos) + ") must be non-negative and not greater than " + String.valueOf(notes.size()));
	    final Object supplied = supplier.get();
	    if (supplied == null)
		return false;
	    final Object[] newNotes;
	    if (supplied instanceof Object[])
		newNotes = (Object[])supplied; else
		newNotes = new Object[]{supplied};
	    for(Object o: newNotes)
		if (!(o instanceof Attributes.Note))
		    return false;
	    notes.addAll(pos, Arrays.asList(Arrays.copyOf(newNotes, newNotes.length, Attributes.Note[].class)));
	    return true;
	}
	@Override public boolean removeFromModel(int pos)
	{
	    final List<Attributes.Note> notes = bookContainer.getAttr().getNotes();
	    	    if (pos < 0 || pos >= notes.size())
		throw new IllegalArgumentException("pos (" + String.valueOf(pos) + ") must be non-negative and less than " + String.valueOf(notes.size()));
		    notes.remove(pos);
	    return true;
	}
	@Override public Object getItem(int index)
	{
	    return bookContainer.getAttr().getNotes().get(index);
	}
	@Override public int getItemCount()
	{
	    return bookContainer.getAttr().getNotes().size();
	}
	@Override public void refresh()
	{
	}
    }
}

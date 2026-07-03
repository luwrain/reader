// SPDX-License-Identifier: BUSL-1.1
// Copyright 2012-2026 Michael Pozhidaev <msp@luwrain.org>

package org.luwrain.app.reader;

import java.util.*;
import java.io.*;
import java.net.*;

import org.luwrain.core.*;
import org.luwrain.core.events.*;
import org.luwrain.core.queries.*;
import org.luwrain.controls.*;
import org.luwrain.io.bookdoc.*;
import org.luwrain.controls.reader.*;
import org.luwrain.app.reader.books.*;
import org.luwrain.app.base.*;
//import org.luwrain.io.api.books.v1.Note;
import org.luwrain.app.reader.books.Book;

import static org.luwrain.core.DefaultEventResponse.*;
import static java.util.Objects.*;

final class MainLayout extends LayoutBase implements TreeArea.ClickHandler, ReaderArea.ClickHandler
{
    private final App app;

    final TreeArea treeArea;
    final ReaderArea readerArea;
    private final EditableListArea<Note> notesArea;
    private final Actions treeActions, readerActions, notesActions;

    private final BookContainer bookContainer;
    private boolean sectionsTreeShown = true, notesShown = false;

    MainLayout(App app)
    {
	super(app);
	this.app = app;
	this.bookContainer = app.getBookContainer();
	this.sectionsTreeShown = bookContainer.getBookFlags().contains(Book.Flags.OPEN_IN_SECTION_TREE);

	final ActionInfo openFile = action("open-file", app.getStrings().actionOpenFile(), new InputEvent(InputEvent.Special.F3, EnumSet.of(InputEvent.Modifiers.SHIFT)), MainLayout.this::actOpenFile);
	final ActionInfo openUrl = action("open-url", app.getStrings().actionOpenUrl(), new InputEvent(InputEvent.Special.F4, EnumSet.of(InputEvent.Modifiers.SHIFT)), MainLayout.this::actOpenUrl);
	final ActionInfo showSectionsTree = action("show-sections-tree", app.getStrings().actionShowSectionsTree(), new InputEvent(InputEvent.Special.F5), MainLayout.this::actShowSectionsTree);
	final ActionInfo showNotes = action("show-notes", app.getStrings().actionShowNotes(), new InputEvent(InputEvent.Special.F6), MainLayout.this::actShowNotes);

	{
	    final TreeArea.Params params = new TreeArea.Params();
	    params.context = getControlContext();
	    params.model = new CachedTreeModel(new BookTreeModelSource());
	    params.name = app.getStrings().treeAreaName();
	    params.clickHandler = this;
	    this.treeArea = new TreeArea(params);
	}
	this.treeActions = actions(
				   action("hide-sections-tree", app.getStrings().actionHideSectionsTree(), new InputEvent(InputEvent.Special.F5), MainLayout.this::actHideSectionsTree),
				   showNotes,
				   openFile, openUrl);

	{
	    final ReaderArea.Params params = new ReaderArea.Params();
	    params.context = getControlContext();
	    params.clickHandler = this;
	    this.readerArea = new ReaderArea(params){
		    @Override public boolean onInputEvent(InputEvent event)
		    {
			requireNonNull(event, "event can't be null");
			if (event.isSpecial() && event.getSpecial() == InputEvent.Special.ESCAPE && !event.isModified() &&
			    app.stopAudio())
			    return true;
			return super.onInputEvent(event);
		    }
		    @Override public boolean onSystemEvent(SystemEvent event)
		    {
			requireNonNull(event, "event can't be null");
			if (event.getType() != SystemEvent.Type.REGULAR)
			    return super.onSystemEvent(event);
			switch(event.getCode())
			{
			case SAVE:
			    return actSaveBookmark();
			case PROPERTIES:
			    return onProps();
			default:
			    return super.onSystemEvent(event);
			}
		    }
		    @Override public String getAreaName()
		    {
			final Doc doc = getDocument();
			if (doc == null)
			    return app.getStrings().appName();
			final String title = doc.getProperty(Doc.PROP_TITLE);
			return title != null?title:app.getStrings().appName();
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
	}
	this.readerActions = actions(
				     new ActionInfo("back", "Вернуться", new InputEvent(InputEvent.Special.BACKSPACE), this::actBack),//FIXME:
				     new ActionInfo("save-bookmark", "Поставить закладку", new InputEvent(InputEvent.Special.F2), this::actSaveBookmark),//FIXME:
				     showSectionsTree, showNotes,
				     openFile, openUrl
				     );

	this.notesArea = new EditableListArea<>(createNotesParams()) ;
	this.notesActions = actions(
				    action("add-note", app.getStrings().actionAddNote(), new InputEvent(InputEvent.Special.INSERT), MainLayout.this::actAddNote),
				    showSectionsTree,
				    action("hide-notes", app.getStrings().actionHideNotes(), new InputEvent(InputEvent.Special.F6), MainLayout.this::actHideNotes),
				    openFile, openUrl);
	updateLayout();
    }

    void updateInitial()
    {
	this.readerArea.setDocument(bookContainer.getDocument(), app.getLuwrain().getScreenWidth() - 3);//FIXME:proper width
	if (sectionsTreeShown)
	    setActiveArea(this.treeArea); else
	    setActiveArea(this.readerArea);
    }

    private void updateAfterJump()
    {
	this.readerArea.setDocument(bookContainer.getDocument(), app.getLuwrain().getScreenWidth() - 3);//FIXME:proper width
	setActiveArea(this.readerArea);
    }

    private boolean actBack()
    {
	return this.bookContainer.onPrevDoc(()->updateAfterJump());
    }

    private boolean actSaveBookmark()
    {
	if (!this.bookContainer.notes.setBookmark(readerArea.getCurrentRowIndex()))
	    return false;
	app.setEventResponse(text(Sounds.OK, "Закладка установлена"));//FIXME:
	return true;
    }

    private boolean actShowSectionsTree()
    {
	this.sectionsTreeShown = true;
	updateLayout();
	app.setAreaLayout(this);
	setActiveArea(treeArea);
	return true;
    }

    private boolean actHideSectionsTree()
    {
	if (!this.sectionsTreeShown)
	    return false;
	this.sectionsTreeShown = false;
	updateLayout();
	app.setAreaLayout(this);
	setActiveArea(readerArea);
	return true;
    }

    private boolean actShowNotes()
    {
	this.notesShown = true;
	updateLayout();
	app.setAreaLayout(this);
	setActiveArea(notesArea);
	return true;
    }

    private boolean actHideNotes()
    {
	if (!this.notesShown)
	    return false;
	this.notesShown = false;
	updateLayout();
	app.setAreaLayout(this);
	setActiveArea(readerArea);
	return true;
    }

    @Override public boolean onTreeClick(TreeArea treeArea, Object obj)
    {
	requireNonNull(treeArea, "treeArea can't be null");
	requireNonNull(obj, "obj can't be null");
	if (!(obj instanceof Book.Section))
	    return false;
	final Book.Section sect = (Book.Section)obj;
	return bookContainer.jump(sect.href, readerArea, 0, ()->updateAfterJump());
    }

    @Override public boolean onReaderClick(ReaderArea area, Run run)
    {
	requireNonNull(area, "area can't be null");
	requireNonNull(run, "run can't be null");
	final String href = run.getHref();
	if (href != null && !href.isEmpty())
	    return bookContainer.jump(href, readerArea, 0, ()->updateAfterJump());
	final String[] ids = readerArea.getHtmlIds();
	if (ids == null || ids.length == 0)
	    return false;
	return app.getBookContainer().playAudio(readerArea, ids);
    }

    private boolean actOpenFile()
    {
	final File file = app.getConv().fileToOpen();
	if (file == null)
	    return false;
	return true;
    }

    private boolean actOpenUrl()
    {
	final URL url = app.getConv().urlToOpen(readerArea.getDocUrl());
	if (url == null)
	    return false;
	return false;
    }

    private int getSuitableWidth()
    {
	return -1;
    }

    private boolean actAddNote()
    {
	final String text = app.getConv().newNote();
	if (text == null)
	    return false;
	final int selected = notesArea.selectedIndex();
	if (selected >= 0)
	    app.getBookContainer().notes.addNote(selected, text); else
	    app.getBookContainer().notes.addNote(0, text);
	notesArea.refresh();
	return true;
    }

    private boolean onProps()
    {
	final PropertiesLayout props = new PropertiesLayout(app, ()->{
		app.setAreaLayout(this);
		setActiveArea(readerArea);
		return true;
	    });
	app.setAreaLayout(props);
	app.getLuwrain().announceActiveArea();
	return true;
    }

    private EditableListArea.Params<Note> createNotesParams()
    {
	final EditableListArea.Params<Note> params = new EditableListArea.Params();
	params.context = getControlContext();
	params.model = app.getBookContainer().notes;
	params.appearance = new NotesAppearance();
	params.name = app.getStrings().notesAreaName();
	params.clipboardSaver = (area, model, appearance, fromIndex, toIndex, clipboard)->{
	    return true;
	};
	return params;
    }

    void updateLayout()
    {
	if (sectionsTreeShown && notesShown)
	{
	    setAreaLayout(AreaLayout.LEFT_TOP_BOTTOM, treeArea, treeActions, readerArea, readerActions, notesArea, notesActions);
	    return;
	}
	if (sectionsTreeShown)
	{
	    setAreaLayout(AreaLayout.LEFT_RIGHT, treeArea, treeActions, readerArea, readerActions);
	    return;
	}
	if (notesShown)
	{
	    setAreaLayout(AreaLayout.TOP_BOTTOM, readerArea, readerActions, notesArea, notesActions);
	    return;
	}
	setAreaLayout(readerArea, readerActions);
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
	    requireNonNull(obj, "obj can't be null");
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

    private final class NotesAppearance extends ListUtils.AbstractAppearance<Note>
    {
	@Override public void announceItem(Note note, Set<Flags> flags)
	{
	    requireNonNull(note, "note can't be null");
	    requireNonNull(flags, "flags can't be null");
	    final String text = getScreenAppearance(note, flags);
	    if (note.getType() != null && note.getType().equals(Note.BOOKMARK))
		app.setEventResponse(listItem(Sounds.SELECTED, text, Suggestions.LIST_ITEM)); else
		app.setEventResponse(listItem(text, Suggestions.LIST_ITEM));
	}
	@Override public String getScreenAppearance(Note note, Set<Flags> flags)
	{
	    requireNonNull(note, "note can't be null");
	    requireNonNull(flags, "flags can't be null");
	    if (note.getType() != null && note.getType().equals(Note.BOOKMARK))
		return "Закладка по умолчанию";//FIXME:
	    return " без комментария";
	}
    }
}

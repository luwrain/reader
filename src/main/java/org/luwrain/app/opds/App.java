/*
   Copyright 2012-2018 Michael Pozhidaev <michael.pozhidaev@gmail.com>

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

package org.luwrain.app.opds;

import java.net.*;
import java.util.*;
import java.io.*;

import org.luwrain.core.*;
import org.luwrain.core.events.*;
import org.luwrain.core.queries.*;
import org.luwrain.controls.*;

public final class App implements Application
{
    private Luwrain luwrain = null;
    private Strings strings= null;
    private Base base = null;
    private Actions actions = null;
    private ListArea librariesArea = null;
    private ListArea listArea = null;
    private ListArea detailsArea = null;

    @Override public InitResult onLaunchApp(Luwrain luwrain)
    {
	NullCheck.notNull(luwrain, "luwrain");
	final Object o = luwrain.i18n().getStrings(Strings.NAME);
	if (o == null || !(o instanceof Strings))
	    return new InitResult(InitResult.Type.NO_STRINGS_OBJ, Strings.NAME);
	this.strings = (Strings)o;
	this.luwrain = luwrain;
	this.base = new Base(luwrain, strings);
	this.actions = new Actions(luwrain, this, strings);
	createAreas();
	return new InitResult();
    }

    private void createAreas()
    {
	final ListArea.Params librariesParams = new ListArea.Params();
	librariesParams.context = new DefaultControlEnvironment(luwrain);
	librariesParams.model = base.getLibrariesModel();
	librariesParams.appearance = new Appearance(luwrain, strings);
	librariesParams.clickHandler = (area, index, obj)->actions.onLibraryClick(base, listArea, obj);
	librariesParams.name = strings.librariesAreaName();
	this.librariesArea = new ListArea(librariesParams){
		@Override public boolean onInputEvent(KeyboardEvent event)
		{
		    NullCheck.notNull(event, "event");
		    if (event.isSpecial() && !event.isModified())
			switch(event.getSpecial())
			{
			case TAB:
			    luwrain.setActiveArea(listArea);
			    return true;
			}
		    return super.onInputEvent(event);
		}
		@Override public boolean onSystemEvent(EnvironmentEvent event)
		{
		    NullCheck.notNull(event, "event");
		    if (event.getType() != EnvironmentEvent.Type.REGULAR)
			return super.onSystemEvent(event);
		    switch(event.getCode())
		    {
		    case CLOSE:
			closeApp();
			return true;
		    default:
			return super.onSystemEvent(event);
		    }
		}
	    };

	final ListArea.Params params = new ListArea.Params();
	params.context = new DefaultControlEnvironment(luwrain);
	params.model = base.getModel();
	params.appearance = new Appearance(luwrain, strings);
	params.clickHandler = (area, index, obj)->actions.onListClick(base, listArea, obj);
	params.name = strings.itemsAreaName();
	this.listArea = new ListArea(params){
		@Override public boolean onInputEvent(KeyboardEvent event)
		{
		    NullCheck.notNull(event, "event");
		    if (event.isSpecial() && !event.isModified())
			switch(event.getSpecial())
			{
			case TAB:
			    luwrain.setActiveArea(detailsArea);
			    return true;
			case BACKSPACE:
			    return onReturnBack();
			}
		    return super.onInputEvent(event);
		}
		@Override public boolean onSystemEvent(EnvironmentEvent event)
		{
		    NullCheck.notNull(event, "event");
		    if (event.getType() != EnvironmentEvent.Type.REGULAR)
			return super.onSystemEvent(event);
		    switch(event.getCode())
		    {
		    case PROPERTIES:
			return actions.onListProperties(base, detailsArea, selected());
		    case CLOSE:
			closeApp();
			return true;
		    default:
			return super.onSystemEvent(event);
		    }
		}
		@Override public boolean onAreaQuery(AreaQuery query)
		{
		    NullCheck.notNull(query, "query");
		    switch(query.getQueryCode())
		    {
		    case AreaQuery.BACKGROUND_SOUND:
			if (base.isFetchingInProgress())
			{
			    ((BackgroundSoundQuery)query).answer(new BackgroundSoundQuery.Answer(BkgSounds.FETCHING));
			    return true;
			}
			return false;
		    default:
			return super.onAreaQuery(query);
		    }
		}
		@Override protected String noContentStr()
		{
		    if (base.isFetchingInProgress())
			return "Идёт загрузка. Пожалуйста, подождите.";//FIXME:
		    return super.noContentStr();
		}
	    };

	final ListArea.Params detailsParams = new ListArea.Params();
	detailsParams.context = new DefaultControlEnvironment(luwrain);
	detailsParams.model = new ListUtils.FixedModel();
	detailsParams.appearance = new ListUtils.DefaultAppearance(detailsParams.context, Suggestions.CLICKABLE_LIST_ITEM);
	//	params.clickHandler = (area, index, obj)->onClick(obj);
detailsParams.name = strings.detailsAreaName();
this.detailsArea = new ListArea(detailsParams){
	@Override public boolean onInputEvent(KeyboardEvent event)
	{
	    NullCheck.notNull(event, "event");
	    if (event.isSpecial() && !event.isModified())
		switch(event.getSpecial())
		{
		case TAB:
		    luwrain.setActiveArea(librariesArea);
		    return true;		    
		}
	    return super.onInputEvent(event);
	}
	@Override public boolean onSystemEvent(EnvironmentEvent event)
	{
	    NullCheck.notNull(event, "event");
	    if (event.getType() != EnvironmentEvent.Type.REGULAR)
		return super.onSystemEvent(event);
	    switch(event.getCode())
	    {
	    case CLOSE:
		closeApp();
		return true;
	    default:
		return super.onSystemEvent(event);
	    }
	}
    };

detailsArea.setListClickHandler((area, index, obj)->actions.onLinkClick(base, obj));
    }

    void updateAreas()
    {
	Log.debug("opds", "refreshing areas");
	listArea.refresh();
	listArea.resetHotPoint(false);
	luwrain.onAreaNewBackgroundSound(listArea);
    }

    private boolean onReturnBack()
    {
	/*
	if (!base.returnBack(listArea))
	    return false;
	listArea.refresh();
	*/
	    return true;
    }


    @Override public String getAppName()
    {
	return strings.appName();
    }

    @Override public AreaLayout getAreaLayout()
    {
	return new AreaLayout(AreaLayout.LEFT_TOP_BOTTOM, librariesArea, listArea, detailsArea);
    }

    @Override public void closeApp()
    {
	luwrain.closeApp();
    }
}

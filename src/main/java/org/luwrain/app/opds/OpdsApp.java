/*
   Copyright 2012-2016 Michael Pozhidaev <michael.pozhidaev@gmail.com>

   This file is part of the LUWRAIN.

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
import org.luwrain.util.Opds;
//import org.luwrain.popups.Popups;

public class OpdsApp implements Application
{
    private final Base base = new Base();
    private Actions actions;
    private Luwrain luwrain;
    private Strings strings;
    private ListArea librariesArea;
    private ListArea listArea;
    private SimpleArea detailsArea;
    private SimpleArea propertiesArea;
    private AreaLayoutSwitch layouts;

    @Override public boolean onLaunch(Luwrain luwrain)
    {
	NullCheck.notNull(luwrain, "luwrain");
	Object o = luwrain.i18n().getStrings(Strings.NAME);
	if (o == null || !(o instanceof Strings))
	    return false;
	strings = (Strings)o;
	this.luwrain = luwrain;
	if (!base.init(luwrain, strings))
	    return false;
	actions = new Actions(luwrain, this, strings);
	createAreas();
	layouts = new AreaLayoutSwitch(luwrain);
	layouts.add(new AreaLayout(AreaLayout.LEFT_TOP_BOTTOM, librariesArea, listArea, detailsArea));
	layouts.add(new AreaLayout(propertiesArea));
	return true;
    }

    private void createAreas()
    {
	final ListArea.Params librariesParams = new ListArea.Params();
	librariesParams.environment = new DefaultControlEnvironment(luwrain);
	librariesParams.model = base.getLibrariesModel();
	librariesParams.appearance = new Appearance(luwrain, strings);
	//	params.clickHandler = (area, index, obj)->onClick(obj);
	librariesParams.name = strings.librariesAreaName();

	librariesArea = new ListArea(librariesParams){

		@Override public boolean onKeyboardEvent(KeyboardEvent event)
		{
		    NullCheck.notNull(event, "event");
		    if (event.isSpecial() && !event.isModified())
			switch(event.getSpecial())
		    {
		    case TAB:
			goToList();
			return true;
			/*
		    case BACKSPACE:
			return onReturnBack();
			*/
		    }
		    return super.onKeyboardEvent(event);
		}

		@Override public boolean onEnvironmentEvent(EnvironmentEvent event)
		{
		    NullCheck.notNull(event, "event");
		    switch(event.getCode())
		    {
		    case PROPERTIES:
			if (selected() == null)
			    return false;
			return showEntryProperties(selected());
		    case CLOSE:
			closeApp();
			return true;
		    default:
			return super.onEnvironmentEvent(event);
		    }
		}

		/*
		@Override protected String noContentStr()
		{
		    if (base.isFetchingInProgress())
			return "Идёт загрузка. Пожалуйста, подождите.";
		    return super.noContentStr();
		}
		*/
	    };

	final ListArea.Params params = new ListArea.Params();
	params.environment = new DefaultControlEnvironment(luwrain);
	params.model = base.getModel();
	params.appearance = new Appearance(luwrain, strings);
	//	params.clickHandler = (area, index, obj)->onClick(obj);
	params.name = strings.itemsAreaName();

	listArea = new ListArea(params){

		@Override public boolean onKeyboardEvent(KeyboardEvent event)
		{
		    NullCheck.notNull(event, "event");
		    if (event.isSpecial() && !event.isModified())
			switch(event.getSpecial())
		    {
		    case TAB:
			goToDetails();
			return true;
		    case BACKSPACE:
			return onReturnBack();
		    }
		    return super.onKeyboardEvent(event);
		}

		@Override public boolean onEnvironmentEvent(EnvironmentEvent event)
		{
		    NullCheck.notNull(event, "event");
		    if (event.getType() != EnvironmentEvent.Type.REGULAR)
			return super.onEnvironmentEvent(event);
		    switch(event.getCode())
		    {
		    case PROPERTIES:
			if (selected() == null)
			    return false;
			return showEntryProperties(selected());
		    case CLOSE:
			closeApp();
			return true;
		    default:
			return super.onEnvironmentEvent(event);
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
			return "Идёт загрузка. Пожалуйста, подождите.";
		    return super.noContentStr();
		}
	    };

	librariesArea.setClickHandler((area, index, obj)->actions.onLibraryClick(base, listArea, obj));

	detailsArea = new SimpleArea(new DefaultControlEnvironment(luwrain), strings.detailsAreaName()){

		@Override public boolean onKeyboardEvent(KeyboardEvent event)
		{
		    NullCheck.notNull(event, "event");
		    if (event.isSpecial() && !event.isModified())
			switch(event.getSpecial())
		    {
		    case TAB:
			goToLibraries();
			return true;		    
}
		    return super.onKeyboardEvent(event);
		}

		@Override public boolean onEnvironmentEvent(EnvironmentEvent event)
		{
		    NullCheck.notNull(event, "event");
		    if (event.getType() != EnvironmentEvent.Type.REGULAR)
			return super.onEnvironmentEvent(event);
		    switch(event.getCode())
		    {
		    case CLOSE:
			closeApp();
			return true;
		    default:
			return super.onEnvironmentEvent(event);
		    }
		}

	    };

	propertiesArea = new SimpleArea(new DefaultControlEnvironment(luwrain), "Просмотр информации"){
		@Override public boolean onKeyboardEvent(KeyboardEvent event)
		{
		    NullCheck.notNull(event, "event");
		    if (event.isSpecial() && !event.isModified())
			    switch(event.getSpecial())
			{
		    case ESCAPE:
			closePropertiesArea();
			return true;
			}
		    return super.onKeyboardEvent(event);
		}
		@Override public boolean onEnvironmentEvent(EnvironmentEvent event)
		{
		    NullCheck.notNull(event, "event");
		    switch(event.getCode())
		    {
		    case CANCEL:
			closePropertiesArea();
			return true;
		    case CLOSE:
closeApp();
			return true;
		    default:
			return super.onEnvironmentEvent(event);
		    }
		}
	    };
    }

    void updateAreas()
    {
	listArea.refresh();
	listArea.resetHotPoint(false);
	luwrain.onAreaNewBackgroundSound(listArea);
    }

private boolean showEntryProperties(Object obj)
    {
	NullCheck.notNull(obj, "obj");
	if (!(obj instanceof Opds.Entry))
	    return false;
	final Opds.Entry entry = (Opds.Entry)obj;
	propertiesArea.clear();
	base.fillEntryProperties(entry, propertiesArea);
	layouts.show(1);
	luwrain.announceActiveArea();
	return true;
    }

 private void closePropertiesArea()
    {
	layouts.show(0);
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

    @Override public AreaLayout getAreasToShow()
    {
	return layouts.getCurrentLayout();
    }

    private void goToLibraries()
    {
	luwrain.setActiveArea(librariesArea);
    }

    private void goToList()
    {
	luwrain.setActiveArea(listArea);
    }

    private void goToDetails()
    {
	luwrain.setActiveArea(detailsArea);
    }

private boolean closeApp()
    {
	/*
	if (thread != null && !thread.done())
	    return false;
	*/
	luwrain.closeApp();
	return true;
    }
}

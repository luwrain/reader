
package org.luwrain.app.opds;

import java.net.*;
import java.util.*;
import java.io.*;

import org.luwrain.core.*;
import org.luwrain.core.events.*;
import org.luwrain.core.queries.*;
import org.luwrain.controls.*;
import org.luwrain.util.Opds;

public class OpdsApp implements Application
{
    private final Base base = new Base();
    private Actions actions;
    private Luwrain luwrain;
    private Strings strings;
    private ListArea librariesArea;
    private ListArea listArea;
    private ListArea detailsArea;
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
			return actions.onListProperties(base, detailsArea, selected());
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
			Log.debug("opds", "fetching in progress:" + base.isFetchingInProgress());
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

	final ListArea.Params detailsParams = new ListArea.Params();
detailsParams.environment = new DefaultControlEnvironment(luwrain);
detailsParams.model = new FixedListModel();
detailsParams.appearance = new DefaultListItemAppearance(detailsParams.environment);
	//	params.clickHandler = (area, index, obj)->onClick(obj);
detailsParams.name = strings.detailsAreaName();


	detailsArea = new ListArea(detailsParams){

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

	librariesArea.setClickHandler((area, index, obj)->actions.onLibraryClick(base, listArea, obj));
	listArea.setClickHandler((area, index, obj)->actions.onListClick(base, listArea, obj));
detailsArea.setClickHandler((area, index, obj)->actions.onLinkClick(base, obj));
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

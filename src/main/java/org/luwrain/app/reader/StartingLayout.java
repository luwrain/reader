
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

final class StartingLayout extends LayoutBase
{
    private App2 app;
    private final ListArea listArea;

    StartingLayout(App2 app)
    {
	NullCheck.notNull(app, "app");
	this.app = app;
	this.listArea = new ListArea(createListParams()) {
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

    private ListArea.Params createListParams()
    {
	final ListArea.Params params = new ListArea.Params();
	params.context = new DefaultControlContext(app.getLuwrain());
	params.model = new ListUtils.FixedModel();
	params.appearance = new ListUtils.DefaultAppearance(params.context, Suggestions.LIST_ITEM);
	//params.clickHandler = clickHandler;
	params.name = app.getStrings().appName();
	return params;
    }

    AreaLayout getLayout()
    {
	return new AreaLayout(listArea);
    }
    }

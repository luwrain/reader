
package org.luwrain.app.opds;

import java.net.*;
import java.util.*;
import java.io.*;

import org.luwrain.core.*;
import org.luwrain.core.events.*;
import org.luwrain.core.queries.*;
import org.luwrain.controls.*;
import org.luwrain.app.opds.Opds.Link;
import org.luwrain.app.opds.Opds.Entry;
import org.luwrain.app.base.*;

final class MainLayout extends LayoutBase
{
final ListArea librariesArea;
final ListArea listArea;
final ListArea detailsArea;

MainLayout(App app)
{
super(app);
{
	final ListArea.Params librariesParams = new ListArea.Params();
	librariesParams.context = getControlContext();
	librariesParams.model = new ListUtils.FixedModel();
	//	librariesParams.appearance = new Appearance(luwrain, strings);
	//	librariesParams.clickHandler = (area, index, obj)->actions.onLibraryClick(listArea, obj);
	//	librariesParams.name = strings.librariesAreaName();
	this.librariesArea = new ListArea(librariesParams);
}

{
	final ListArea.Params params = new ListArea.Params();
	params.context = getControlContext();
	params.model = new ListUtils.ListModel(app.entries);
	//	params.appearance = new Appearance(luwrain, strings);
	//	params.clickHandler = (area, index, obj)->actions.onListClick( listArea, obj);
	//	params.name = strings.itemsAreaName();
	this.listArea = new ListArea(params);
}

{
	final ListArea.Params params = new ListArea.Params();
	params.context = getControlContext();
	params.model = new ListUtils.FixedModel();
	params.appearance = new ListUtils.DefaultAppearance(getControlContext(), Suggestions.CLICKABLE_LIST_ITEM);
	//	params.clickHandler = (area, index, obj)->onClick(obj);
	//detailsParams.name = strings.detailsAreaName();
this.detailsArea = new ListArea(params);
}

//detailsArea.setListClickHandler((area, index, obj)->actions.onLinkClick(obj));
    }

}

	/*
						case AreaQuery.UNIREF_HOT_POINT:
			    {
				final Object obj = selected();
				if (obj == null || !(obj instanceof Entry))
				    return false;
				final Entry entry = (Entry)obj;
				final Link link = base.getSuitableBookLink(entry);
				if (link == null)
				    return false;
				final UniRefHotPointQuery unirefQuery = (UniRefHotPointQuery)query;
				unirefQuery.answer("url:" + base.prepareUrl(link.url).toString());
				return true;
*/


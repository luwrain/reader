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

package org.luwrain.app.narrator;

import org.luwrain.core.*;
import org.luwrain.cpanel.*;

public class ControlPanelFactory implements Factory
{
    static private final Element element = new SimpleElement(StandardElements.APPLICATIONS, ControlPanelFactory.class.getName());

    private Luwrain luwrain;

    public ControlPanelFactory(Luwrain luwrain)
    {
	NullCheck.notNull(luwrain, "luwrain");
	this.luwrain = luwrain;
    }

    @Override public Element[] getElements()
    {
	return new Element[]{element};
    }

    @Override public Element[] getOnDemandElements(Element parent)
    {
	return new Element[0];
    }

    @Override public org.luwrain.cpanel.Section createSection(Element el)
    {
	NullCheck.notNull(el, "el");
	final Strings strings = (Strings)luwrain.i18n().getStrings(Strings.NAME);
	if (el.equals(element))
	    return new SimpleSection(element, strings.settingsFormName(), (controlPanel)->SettingsForm.create(controlPanel));
	return null;
    }
}

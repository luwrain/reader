/*
   Copyright 2012-2019 Michael Pozhidaev <michael.pozhidaev@gmail.com>
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

package org.luwrain.reader;

import java.util.*;

import org.luwrain.core.*;

public class ExtraInfo
{
    public String name;
    public final HashMap<String, String> attrs = new HashMap<String, String>();
    public ExtraInfo parent = null;

    public String getName()
    {
	return name != null?name:"";
    }

    public String getAttr(String attrName)
    {
	NullCheck.notEmpty(attrName, "attrName");
	if (attrs == null || !attrs.containsKey(attrName))
	    return "";
	return attrs.get(attrName);

    }

    public boolean hasIdInChain(String idName)
    {
	NullCheck.notEmpty(idName, "idName");
	if (getAttr("id").equals(idName))
	    return true;
	if (parent != null)
	    return parent.hasIdInChain(idName);
	return false;
    }
}

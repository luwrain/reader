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

//LWR_API 1.0

package org.luwrain.reader;

import org.luwrain.core.*;
import org.luwrain.util.*;

public final class DocumentBuilderLoader
{
    static private final String LOG_COMPONENT = "doc";
    static private String PROP_PREFIX = "luwrain.ext.doc.builder.";

    public DocumentBuilder newDocumentBuilder(Luwrain luwrain, String contentType)
    {
	NullCheck.notNull(luwrain, "luwrain");
	NullCheck.notEmpty(contentType, "contentType");
	final String propName = PROP_PREFIX + prepareContentType(contentType);
	final String className = luwrain.getProperty(propName);
	if (className.isEmpty())
	{
	    Log.error(LOG_COMPONENT, "no property \'" + propName + "\'");
	    return null;
	}
	final Object obj = ClassUtils.newInstanceOf(className, DocumentBuilderFactory.class);
	if (obj == null)
	    return null;
	return ((DocumentBuilderFactory)obj).newDocumentBuilder(luwrain);
    }

    static private String prepareContentType(String contentType)
    {
	NullCheck.notNull(contentType, "contentType");
	final StringBuilder b = new StringBuilder();
	for(int i = 0;i < contentType.length();++i)
	{
	    final char c = contentType.charAt(i);
	    if (Character.isDigit(c))
		b.append("" + c); else
		if (Character.isLetter(c))
		    b.append("" + Character.toLowerCase(c)); else
		    b.append("-");
	}
	return new String(b);
    }
}

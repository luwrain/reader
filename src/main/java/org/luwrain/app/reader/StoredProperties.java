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

package org.luwrain.app.reader;

import java.util.*;

import org.luwrain.core.*;
import org.luwrain.util.*;

final class StoredProperties
{
    private final Registry registry;
    private final String path;
    final Settings.Properties sett;

    StoredProperties(Registry registry, String url)
    {
	NullCheck.notNull(registry, "registry");
	NullCheck.notEmpty(url, "url");
	this.registry = registry;
	this.path = Registry.join(Settings.PROPERTIES_PATH, Sha1.getSha1(url, "UTF-8"));
	registry.addDirectory(this.path);
	Log.debug("proba", "Adding " + path);
	this.sett = Settings.createProperties(registry, path);
    }

    static boolean hasProperties(Registry registry, String url)
    {
	NullCheck.notNull(registry, "registry");
	NullCheck.notEmpty(url, "url");
	return registry.hasDirectory(Registry.join(Settings.PROPERTIES_PATH, Sha1.getSha1(url, "UTF-8")));
    }
}

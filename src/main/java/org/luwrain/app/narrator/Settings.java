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

interface Settings
{
    static public final String REGISTRY_PATH = "/org/luwrain/app/narrator";

    String getLameCommand(String defValue);
    void setLameCommand(String command);

    static Settings create(Registry registry)
    {
	NullCheck.notNull(registry, "registry");
	return 	RegistryProxy.create(registry, REGISTRY_PATH, Settings.class);
    }
}

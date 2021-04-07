/*
   Copyright 2012-2021 Michael Pozhidaev <msp@luwrain.org>

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

import org.luwrain.core.*;

public interface Settings
{
    static final String PATH = "/org/luwrain/app/opds/libraries";

    String getLibraries(String defValue);
    void setLibraries(String value);

    static Settings create(Luwrain luwrain)
    {
	NullCheck.notNull(luwrain, "luwrain");
	return RegistryProxy.create(luwrain.getRegistry(), PATH, Settings.class);
    }
}

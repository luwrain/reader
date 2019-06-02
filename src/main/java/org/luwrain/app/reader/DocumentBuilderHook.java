/*
   Copyright 2012-2019 Michael Pozhidaev <msp@luwrain.org>
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

package org.luwrain.app.reader;

import java.io.*;
import java.util.*;
import java.util.concurrent.atomic.*;

import org.luwrain.core.*;
import org.luwrain.reader.*;

final class DocumentBuilderHook
{
    static private final String LOG_COMPONENT = "reader";
    static private final String HOOK_NAME = "luwrain.reader.doc.builder";

    private final Luwrain luwrain;

    DocumentBuilderHook(Luwrain luwrain)
    {
	NullCheck.notNull(luwrain, "luwrain ");
	this.luwrain = luwrain;
    }

    Document build(String contentType, Properties props, File file)
    {
	NullCheck.notEmpty(contentType, "contentType");
	NullCheck.notNull(props, "props");
	NullCheck.notNull(file, "file");
	final AtomicReference res = new AtomicReference();
	luwrain.xRunHooks(HOOK_NAME, (hook)->{
		try {
		    final Object obj = hook.run(new Object[]{contentType, null, file.getAbsolutePath()});
		    if (obj == null)
			return Luwrain.HookResult.CONTINUE;
		    res.set(obj);
		    return Luwrain.HookResult.BREAK;
		}
		catch(RuntimeException e)
		{
		    Log.error(LOG_COMPONENT, "unable to run the hook " + HOOK_NAME + ":" + e.getClass().getName() + ":" + e.getMessage());
		    res.set(e);
		    return Luwrain.HookResult.BREAK;
		}
	    });
	if (res.get() == null)
	    return null;
	if (res.get() instanceof RuntimeException)
	    throw (RuntimeException)res.get();
	return new HookObjectDocumentBuilder().build(res.get());
    }
}

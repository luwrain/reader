/*
   Copyright 2012-2020 Michael Pozhidaev <msp@luwrain.org>
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
import org.luwrain.script.*;
//import org.luwrain.reader.*;

final class ErrorHook
{
    static private final String LOG_COMPONENT = "reader";
    static private final String HOOK_NAME = "luwrain.reader.doc.error";

    private final Luwrain luwrain;

    ErrorHook(Luwrain luwrain)
    {
	NullCheck.notNull(luwrain, "luwrain ");
	this.luwrain = luwrain;
    }

    String[] run(Properties props, Throwable throwable)
    {
	NullCheck.notNull(props, "props");
	final PropertiesHookObject propsHookObj = new PropertiesHookObject(props);//FIXME:read only
	final Object[] args = new Object[]{propsHookObj, throwable};
	final AtomicReference res = new AtomicReference();
	luwrain.xRunHooks(HOOK_NAME, (hook)->{
		try {
		    final Object obj = hook.run(args);
		    if (obj == null)
			return Luwrain.HookResult.CONTINUE;
		    final List<String> lines = ScriptUtils.getStringArray(obj);
		    if (lines == null)
			return Luwrain.HookResult.CONTINUE;
		    res.set(lines.toArray(new String[lines.size()]));
		    return Luwrain.HookResult.BREAK;
		}
		catch(RuntimeException e)
		{
		    Log.error(LOG_COMPONENT, "unable to run the hook " + HOOK_NAME + ":" + e.getClass().getName() + ":" + e.getMessage());
		    return Luwrain.HookResult.CONTINUE;
		}
	    });
	if (res.get() == null)
	    return new String[0];
	return (String[])res.get();
    }
}

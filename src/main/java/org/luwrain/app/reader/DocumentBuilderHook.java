
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
		    return Luwrain.HookResult.CONTINUE;
		}
	    });
	if (res.get() == null)
	    return null;
	return new HookObjectDocumentBuilder().build(res.get());
    }
}

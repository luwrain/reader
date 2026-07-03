
package org.luwrain.controls.reader;

import org.luwrain.core.*;
import org.luwrain.controls.*;

public final class Factory
{
    public ReaderArea newDocumentArea(Luwrain luwrain, ControlContext context)
    {
	NullCheck.notNull(luwrain, "luwrain");
	NullCheck.notNull(context, "context");
	return new ReaderArea(context, newAnnouncement(luwrain, context));
    }

    public Strings newStrings(Luwrain luwrain)
    {
	NullCheck.notNull(luwrain, "luwrain");
	return (Strings)luwrain.i18n().getStrings(Strings.NAME);
    }

    public DefaultAnnouncement newAnnouncement(Luwrain luwrain, ControlContext context)
    {
	NullCheck.notNull(luwrain, "luwrain");
	NullCheck.notNull(context, "context");
	return new DefaultAnnouncement(context, newStrings(luwrain));
    }
}

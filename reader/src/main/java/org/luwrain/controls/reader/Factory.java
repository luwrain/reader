// SPDX-License-Identifier: BUSL-1.1
// Copyright 2012-2026 Michael Pozhidaev <msp@luwrain.org>

package org.luwrain.controls.reader;

import org.luwrain.core.*;
import org.luwrain.controls.*;

import static java.util.Objects.*;

public final class Factory
{
    public ReaderArea newDocumentArea(Luwrain luwrain, ControlContext context)
    {
	requireNonNull(luwrain, "luwrain can't be null");
	requireNonNull(context, "context can't be null");
	return new ReaderArea(context, newAnnouncement(luwrain, context));
    }

    public Strings newStrings(Luwrain luwrain)
    {
	requireNonNull(luwrain, "luwrain can't be null");
	return (Strings)luwrain.i18n().getStrings(Strings.NAME);
    }

    public DefaultAnnouncement newAnnouncement(Luwrain luwrain, ControlContext context)
    {
	requireNonNull(luwrain, "luwrain can't be null");
	requireNonNull(context, "context can't be null");
	return new DefaultAnnouncement(context, newStrings(luwrain));
    }
}

// SPDX-License-Identifier: BUSL-1.1
// Copyright 2012-2026 Michael Pozhidaev <msp@luwrain.org>

package org.luwrain.controls.reader;

import org.luwrain.core.*;
import org.luwrain.controls.*;

import static java.util.Objects.*;

/**
 * A factory for reader controls and related objects.
 *
 * <p>The methods of this factory create reader areas and their localizable
 * strings and announcement providers. Using the factory keeps the construction
 * logic in one place.</p>
 */
public final class Factory
{
    /**
     * Creates a reader area without a document.
     *
     * @param luwrain the LUWRAIN instance; must not be {@code null}
     * @param context the control context; must not be {@code null}
     * @return a new reader area
     * @throws NullPointerException if {@code luwrain} or {@code context} is {@code null}
     */
    public ReaderArea newDocumentArea(Luwrain luwrain, ControlContext context)
    {
	requireNonNull(luwrain, "luwrain can't be null");
	requireNonNull(context, "context can't be null");
	return new ReaderArea(context, newAnnouncement(luwrain, context));
    }

    /**
     * Creates the localizable strings object for the reader area.
     *
     * @param luwrain the LUWRAIN instance; must not be {@code null}
     * @return a new strings object
     * @throws NullPointerException if {@code luwrain} is {@code null}
     */
    public Strings newStrings(Luwrain luwrain)
    {
	requireNonNull(luwrain, "luwrain can't be null");
	return (Strings)luwrain.i18n().getStrings(Strings.class.getName());
    }

    /**
     * Creates the default announcement provider.
     *
     * @param luwrain the LUWRAIN instance; must not be {@code null}
     * @param context the control context; must not be {@code null}
     * @return a new announcement provider
     * @throws NullPointerException if {@code luwrain} or {@code context} is {@code null}
     */
    public DefaultAnnouncement newAnnouncement(Luwrain luwrain, ControlContext context)
    {
	requireNonNull(luwrain, "luwrain can't be null");
	requireNonNull(context, "context can't be null");
	return new DefaultAnnouncement(context, newStrings(luwrain));
    }
}

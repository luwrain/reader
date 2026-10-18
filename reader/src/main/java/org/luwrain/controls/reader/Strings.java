// SPDX-License-Identifier: BUSL-1.1
// Copyright 2012-2026 Michael Pozhidaev <msp@luwrain.org>

package org.luwrain.controls.reader;

import org.luwrain.core.annotations.*;

/**
 * Localizable strings used by the reader controls.
 *
 * <p>The strings are loaded from resource bundles and are available in English
 * and Russian.</p>
 */
@ResourceStrings(langs = { "en", "ru" })
public interface Strings
{
    /**
     * Returns the introductory announcement for a table cell.
     *
     * @param row the cell row index
     * @param col the cell column index
     * @return the localized announcement text
     */
    String tableCellIntro(int row, int col);
}

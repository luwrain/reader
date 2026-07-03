// SPDX-License-Identifier: BUSL-1.1
// Copyright 2012-2026 Michael Pozhidaev <msp@luwrain.org>

package org.luwrain.app.reader;

import java.util.*;
import java.io.*;

import org.luwrain.core.*;

import static java.util.Objects.*;

interface Settings
{
    static final String
	PATH = "/org/luwrain/app/reader",
	ATTRIBUTES_PATH = "/org/luwrain/app/reader/attributes";

    String getLocalRepoMetadata(String defValue);
    void setLocalRepoMetadata(String value);
    String getAttributes(String defValue);
    void setAttributes(String value);

    static Settings create(Luwrain luwrain)
    {
	requireNonNull(luwrain, "luwrain can't be null");
	final File appDataDir = new File(luwrain.getAppDataDir("luwrain.reader").toFile(), "settings");
	return new StandaloneSettings(appDataDir);
    }
}

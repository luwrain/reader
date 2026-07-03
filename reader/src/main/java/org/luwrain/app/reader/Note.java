// SPDX-License-Identifier: BUSL-1.1
// Copyright 2012-2026 Michael Pozhidaev <msp@luwrain.org>

package org.luwrain.app.reader;

import java.util.*;
import lombok.*;

@Data
public final class Note
{
    /** Marker value for the default bookmark. */
    static final String
	BOOKMARK = "BOOKMARK";

    /** Marker value for a user annotation note. */
    static final String
	NOTE = "NOTE";

    /** The type of this note: {@link #BOOKMARK} or {@link #NOTE}. */
    private String type;

    /** The reader position (row index) associated with this note. */
    private String pos;

    /** The user-provided comment text (may be empty). */
    private String text;

    public Note()
    {
    }

    public Note(String type, String pos, String text)
    {
	this.type = type;
	this.pos = pos;
	this.text = text;
    }
}

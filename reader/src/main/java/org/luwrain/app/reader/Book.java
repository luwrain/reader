// SPDX-License-Identifier: BUSL-1.1
// Copyright 2012-2026 Michael Pozhidaev <msp@luwrain.org>

package org.luwrain.app.reader;

import java.util.*;
import lombok.*;

@Data
public final class Book
{
    /** The unique identifier of this book (typically a SHA-1 hash of the URL). */
    private String id;
    /** Human-readable name or title of the book. */
    private String name;
    /** The URL from which the book was loaded. */
    private String url;
    /** The content type (MIME type) of the book. */
    private String contentType;
    /** The character encoding used for the book text. */
    private String charset;
    /** The time when the book was added to the local repository (milliseconds since epoch). */
    private long timestamp;

    public Book()
    {
	this.timestamp = System.currentTimeMillis();
    }

    public Book(String id, String name, String url)
    {
	this();
	this.id = id;
	this.name = name;
	this.url = url;
    }
}

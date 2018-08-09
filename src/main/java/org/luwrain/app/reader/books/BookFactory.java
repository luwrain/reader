
package org.luwrain.app.reader.books;

import java.net.*;
import java.io.*;
import java.nio.file.*;

import org.luwrain.core.*;
import org.luwrain.doctree.*;
import org.luwrain.util.*;

public class BookFactory
{
    static public Book initDaisy2(Luwrain luwrain, Document nccDoc)
    {
	NullCheck.notNull(luwrain, "luwrain");
	NullCheck.notNull(nccDoc, "nccDoc");
	final Daisy2 book = new Daisy2(luwrain);
	book.init(nccDoc);
	return book;
    }
}

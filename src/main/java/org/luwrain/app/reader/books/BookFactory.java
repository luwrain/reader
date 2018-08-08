
package org.luwrain.app.reader.books;

import java.net.*;
import java.io.*;
import java.nio.file.*;

import org.luwrain.core.NullCheck;
import org.luwrain.core.Log;
import org.luwrain.doctree.*;
import org.luwrain.util.*;

public class BookFactory
{
    static public Book initDaisy2(Document nccDoc)
    {
	NullCheck.notNull(nccDoc, "nccDoc");
	final Daisy2 book = new Daisy2();
	book.init(nccDoc);
	return book;
    }
}

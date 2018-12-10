
package org.luwrain.reader.builders.docx;

import org.luwrain.core.*;
import org.luwrain.reader.*;

public final class DocXFactory implements DocumentBuilderFactory
{
    @Override public DocumentBuilder newDocumentBuilder(Luwrain luwrain)
    {
	NullCheck.notNull(luwrain, "luwrain");
	return new DocX();
    }
}

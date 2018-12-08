
package org.luwrain.reader.builders.html;

import org.luwrain.core.*;
import org.luwrain.reader.*;

public final class HtmlFactory implements DocumentBuilderFactory
{
    @Override public DocumentBuilder newDocumentBuilder(Luwrain luwrain)
    {
	NullCheck.notNull(luwrain, "luwrain");
	return new Html();
    }
}
    

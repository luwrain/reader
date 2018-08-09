
package org.luwrain.doc.builders;

import org.luwrain.core.*;
import org.luwrain.doctree.*;

public final class HtmlFactory implements DocumentBuilderFactory
{
    @Override public DocumentBuilder newDocumentBuilder(Luwrain luwrain)
    {
	NullCheck.notNull(luwrain, "luwrain");
	return new Html();
    }
}
    


package org.luwrain.app.reader;

import org.luwrain.core.*;
import org.luwrain.doctree.*;

class AudioFollowingHandler
{
    Run prevRun;
    DoctreeArea area;

    AudioFollowingHandler(DoctreeArea area)
    {
	NullCheck.notNull(area, "area");
	this.area = area;
    }


}



package org.luwrain.app.reader;

import org.luwrain.core.*;
import org.luwrain.doctree.*;

class AudioFollowingHandler
{
    Run prevRun;
    ReaderArea area;

    AudioFollowingHandler(ReaderArea area)
    {
	NullCheck.notNull(area, "area");
	this.area = area;
    }


}



package org.luwrain.app.reader;

import org.luwrain.core.*;
import org.luwrain.doctree.*;

class AudioFollowingHandler
{
    Run prevRun;
    DocTreeArea area;

    AudioFollowingHandler(DocTreeArea area)
    {
	NullCheck.notNull(area, "area");
	this.area = area;
    }


}


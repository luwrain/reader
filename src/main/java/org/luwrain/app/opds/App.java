
package org.luwrain.app.opds;

import java.net.*;
import java.util.*;
import java.io.*;

import org.luwrain.core.*;
import org.luwrain.core.events.*;
import org.luwrain.core.queries.*;
import org.luwrain.controls.*;
import org.luwrain.app.opds.Opds.Link;
import org.luwrain.app.base.*;
import org.luwrain.app.opds.Opds.Entry;

public final class App extends AppBase<Strings>
{
    final List<Entry> entries = new ArrayList<Entry>();
    
    private MainLayout mainLayout = null;

    

    public App()
    {
	super(Strings.NAME, Strings.class);
    }

    @Override public boolean onAppInit()
    {
	this.mainLayout = new MainLayout(this);
	setAppName(getStrings().appName());
	return true;
    }

    @Override public AreaLayout getDefaultAreaLayout()
    {
	return mainLayout.getAreaLayout();
    }
}

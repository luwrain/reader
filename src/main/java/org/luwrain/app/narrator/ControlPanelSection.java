
package org.luwrain.app.narrator;

import org.luwrain.core.*;
import org.luwrain.cpanel.*;

public class ControlPanelSection extends SimpleFormSection
{
    public ControlPanelSection(Registry registry)
    {
	super("Рассказчик", BasicSections.APPLICATIONS);
	final RegistryOptions options = RegistryProxy.create(registry, "/org/luwrain/app/narrator", RegistryOptions.class);
	addString("Команда вызова lame:", "", (name, value)->options.setLameCommand(value));
    }
}

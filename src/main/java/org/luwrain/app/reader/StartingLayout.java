/*
   Copyright 2012-2021 Michael Pozhidaev <msp@luwrain.org>
   Copyright 2015-2016 Roman Volovodov <gr.rPman@gmail.com>

   This file is part of LUWRAIN.

   LUWRAIN is free software; you can redistribute it and/or
   modify it under the terms of the GNU General Public
   License as published by the Free Software Foundation; either
   version 3 of the License, or (at your option) any later version.

   LUWRAIN is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
   General Public License for more details.
*/

package org.luwrain.app.reader;

import java.util.*;
import java.io.*;

import org.luwrain.core.*;
import org.luwrain.core.events.*;
import org.luwrain.core.queries.*;
import org.luwrain.controls.*;
import org.luwrain.reader.*;
import org.luwrain.controls.reader.*;
import org.luwrain.app.reader.books.*;
import org.luwrain.app.base.*;

import org.luwrain.io.api.books.v1.*;
import org.luwrain.io.api.books.v1.users.*;

final class StartingLayout extends LayoutBase
{
    private App app;
    private final WizardArea wizardArea;

    private final WizardArea.Frame introFrame;
    private final WizardArea.Frame loginFrame;

    StartingLayout(App app)
    {
	NullCheck.notNull(app, "app");
	this.app = app;
	this.wizardArea = new WizardArea(new DefaultControlContext(app.getLuwrain())) {
		@Override public boolean onInputEvent(InputEvent event)
		{
		    NullCheck.notNull(event, "event");
		    if (app.onInputEvent(this, event))
			return true;
		    return super.onInputEvent(event);
		}
		@Override public boolean onSystemEvent(SystemEvent event)
		{
		    NullCheck.notNull(event, "event");
		    if (app.onSystemEvent(this, event))
			return true;
			return super.onSystemEvent(event);
		}
		@Override public boolean onAreaQuery(AreaQuery query)
		{
		    NullCheck.notNull(query, "query");
		    if (app.onAreaQuery(this, query))
			return true;
		    return super.onAreaQuery(query);
		}
		@Override public Action[] getAreaActions()
		{
		    return new Action[0];
		}
	    };
			this.introFrame = wizardArea.newFrame()
			.addText("Выберите, пожалуйста, способ, с помощью которого вы желаете открыть книгу или документ для чтения:")
			.addClickable("Открыть файл на диске", (values)->{ return false; })
			.addClickable("Подключиться к облачному сервису LUWRAIN Books", (values)->connectFrame());
			this.loginFrame = wizardArea.newFrame()
			.addText("Имя пользователя и пароль")
			.addInput("Адрес электронной почты:", "")
			.addInput("Пароль:", "")
			.addClickable("Подключиться", (values)->connect(values));
			wizardArea.show(introFrame);
	    }

    private boolean connectFrame()
    {
	wizardArea.show(loginFrame);
	return true;
    }

    private boolean connect(WizardArea.WizardValues values)
    {
	NullCheck.notNull(values, "values");
	final String mail = values.getText(0).trim();
	final String passwd = values.getText(1).trim();
	if (mail.isEmpty())
	{
	    app.getLuwrain().message("Не указан адрес электронной почты для подключения", Luwrain.MessageType.ERROR);
	    return true;
	}
	if (passwd.isEmpty())
	{
	    app.getLuwrain().message("Не указан пароль для подключения", Luwrain.MessageType.ERROR);
		    return true;
	}
	final App.TaskId taskId = app.newTaskId();
	return app.runTask(taskId, ()->{
		final AccessTokenQuery.Response resp;
		try {
		    resp = app.getBooks().users().accessToken().mail(mail).passwd(passwd).exec();
		}
		catch(BooksException e)
		{
		    final ErrorResponse er = e.getErrorResponse();
		    if (er == null || er.getType() == null)
		    {
			app.getLuwrain().crash(e);
			return;
		    }
		    if (er.getType().equals(AccessTokenQuery.INVALID_CREDENTIALS))
		    {
			app.getLuwrain().message("Указан неверный пароль, попробуйте ещё раз", Luwrain.MessageType.ERROR);
			return;
		    }
		    return;
		}
		catch(IOException e)
		{
		    app.getLuwrain().crash(e);
		    return;
		}
	    });
    }

    AreaLayout getLayout()
    {
	return new AreaLayout(wizardArea);
    }
    }

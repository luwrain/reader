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
import org.luwrain.controls.*;
import org.luwrain.app.base.*;

import org.luwrain.io.api.books.v1.*;
import org.luwrain.io.api.books.v1.users.*;
import org.luwrain.io.api.books.v1.collection.*;

final class StartingLayout extends LayoutBase
{
    private App app;
    private final WizardArea wizardArea;

    private final WizardArea.Frame introFrame, loginFrame, confirmationFrame;
    private String mail = "", passwd = "";

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
	.addText(app.getStrings().wizardGreetingIntro())
	.addClickable(app.getStrings().wizardGreetingRemote(), (values)->connectFrame())
	.addClickable(app.getStrings().wizardGreetingLocal(), (values)->{ app.layouts().localRepo(); return true; });
	this.loginFrame = wizardArea.newFrame()
	.addText(app.getStrings().wizardLoginIntro())
	.addInput(app.getStrings().wizardLoginMail(), "")
	.addPasswd(app.getStrings().wizardLoginPasswd(), "")
	.addClickable(app.getStrings().wizardLoginConnect(), (values)->connect(values));
	this.confirmationFrame = wizardArea.newFrame()
	.addText(app.getStrings().wizardConfirmationIntro())
	.addInput(app.getStrings().wizardConfirmationCode(), "")
	.addClickable(app.getStrings().wizardConfirmationConfirm(), (values)->confirm(values));
	wizardArea.show(introFrame);
    }

    private boolean connectFrame()
    {
	app.getLuwrain().setEventResponse(DefaultEventResponse.text(Sounds.CLICK, "Подключение к books.luwrain.org"));
	wizardArea.show(loginFrame);
	return true;
    }

    private boolean connect(WizardArea.WizardValues values)
    {
	NullCheck.notNull(values, "values");
	this.mail = values.getText(0).trim();
	this.passwd = values.getText(1).trim();
	if (mail.isEmpty())
	{
	    app.message("Не указан адрес электронной почты для подключения", Luwrain.MessageType.ERROR);
	    return true;
	}
	if (passwd.isEmpty())
	{
	    app.message("Не указан пароль для подключения", Luwrain.MessageType.ERROR);
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
			app.crash(e);
			return;
		    }
		    switch(er.getType())
		    {
		    case AccessTokenQuery.INVALID_CREDENTIALS:
			app.message("Указан неверный пароль, попробуйте ещё раз", Luwrain.MessageType.ERROR);
			return;
		    case AccessTokenQuery.NOT_REGISTERED:
			register(taskId);
			return;
		    case AccessTokenQuery.NOT_CONFIRMED:
			app.finishedTask(taskId, ()->{
	app.getLuwrain().setEventResponse(DefaultEventResponse.text(Sounds.CLICK, "Код подтверждения"));
				wizardArea.show(confirmationFrame);
				//FIXME:announcement
			    });
			return;
		    default:
			app.crash(e);
			return;
		    }
		}
		catch(IOException e)
		{
		    app.crash(e);
		    return;
		}
		final CollectionQuery.Response collectionResp;
		try {
		    collectionResp = app.getBooks().collection().collection().accessToken(resp.getAccessToken()).exec();
		}
		catch(IOException e)
		{
		    app.crash(e);
		    return;
		}
		app.finishedTask(taskId, ()->{
			app.setAccessToken(resp.getAccessToken());
			app.setRemoteBooks(collectionResp.getBooks() != null?collectionResp.getBooks():new org.luwrain.io.api.books.v1.Book[0]);
			app.layouts().remoteBooks();
		    });
	    });
    }

    private void register(App.TaskId taskId)
    {
	NullCheck.notNull(taskId, "taskId");
	NullCheck.notNull(mail, "mail");
	NullCheck.notNull(passwd, "passwd");
	final RegisterQuery.Response resp; 
	try {
	    resp = app.getBooks().users().register().mail(mail).passwd(passwd).exec();
	}
	catch(BooksException e)
	{
	    final ErrorResponse er = e.getErrorResponse();
	    if (er == null || er.getType() == null)
	    {
		app.crash(e);
		return;
	    }
	    switch(er.getType())
	    {
	    case RegisterQuery.INVALID_MAIL:
		app.message("Указан недопустимый адрес электронной почты", Luwrain.MessageType.ERROR);
		return;
	    case RegisterQuery.INVALID_PASSWORD:
    		app.message("Указан недопустимый пароль", Luwrain.MessageType.ERROR);
		return;
	    case RegisterQuery.MAIL_ADDRESS_ALREADY_IN_USE:
		app.message("На сервере уже зарегистрирован пользователь с таким адресом электронной почты", Luwrain.MessageType.ERROR);
		return;
	    case RegisterQuery.TOO_SHORT_PASSWORD:
		app.message("Указан слишком короткий пароль.", Luwrain.MessageType.ERROR);
		return;
	    default:
		app.crash(e);
		return;
	    }
	}
	catch(IOException e)
	{
	    app.crash(e);
	    return;
	}
	app.finishedTask(taskId, ()->{
	app.getLuwrain().setEventResponse(DefaultEventResponse.text(Sounds.CLICK, "Код подтверждения"));
	wizardArea.show(confirmationFrame);
	    });
    }

    private boolean confirm(WizardArea.WizardValues values)
    {
	NullCheck.notNull(values, "values");
	Log.debug("proba", "" + values.getText(0));
	final String code = values.getText(0).trim();
	if (code.isEmpty())
	{
	    app.message("Не указан код подтверждения.", Luwrain.MessageType.ERROR);
	    return true;
	}
	final App.TaskId taskId = app.newTaskId();
	return app.runTask(taskId, ()->{
		final ConfirmQuery.Response resp;
		try {
		    resp = app.getBooks().users().confirm().mail(this .mail).confirmationCode(code).exec();
		}
		catch(BooksException e)
		{
		    final ErrorResponse er = e.getErrorResponse();
		    if (er == null || er.getType() == null)
		    {
			app.crash(e);
			return;
		    }
		    switch(er.getType())
		    {
		    case ConfirmQuery.TOO_MANY_ATTEMPTS:
			app.finishedTask(taskId, ()->{
				wizardArea.show(loginFrame);
				app.message("Превышено максимальное число попыток, попробуйте зарегистрироваться ещё раз.", Luwrain.MessageType.ERROR);
			    });
			return;
		    case ConfirmQuery.INVALID_CONFIRMATION_CODE:
			app.message("Указан неверный код подтверждения, попробуйте ещё раз.", Luwrain.MessageType.ERROR);
			return;
		    default:
			app.crash(e);
			return;
		    }
		}
		catch(IOException e)
		{
		    app.crash(e);
		    return;
		}
	    });
    }

    AreaLayout getLayout()
    {
	return new AreaLayout(wizardArea);
    }
}

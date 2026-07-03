// SPDX-License-Identifier: BUSL-1.1
// Copyright 2012-2026 Michael Pozhidaev <msp@luwrain.org>

package org.luwrain.app.reader;

import java.net.*;
import java.util.*;

import org.luwrain.core.*;
import org.luwrain.popups.Popups;
import org.luwrain.cpanel.Factory;

import static java.util.Objects.*;

public final class Extension extends EmptyExtension
{
    private final Set<String> queries = new TreeSet();

    @Override public Command[] getCommands(Luwrain luwrain)
    {
	return new Command[]{ new SimpleShortcutCommand("reader") };
    }

    /*	    new Command(){
		@Override public String getName()
		{
		    return "reader-open";
		}
		@Override public void onCommand(Luwrain luwrain)
		{
		    final Strings strings = (Strings)luwrain.i18n().getStrings(Strings.NAME);
		    if (strings == null)
			return;
		    final String url = Popups.simple(luwrain, strings.openUrlPopupName(), strings.openUrlPopupPrefix(), "http://");
		    if (url != null && !url.trim().isEmpty())
			luwrain.launchApp("reader", new String[]{url.indexOf("://") >= 0?url:("http://" + url)});
		}
	    },

	    //The same as reader-open
	    new Command(){
		@Override public String getName()
		{
		    return "open-url";
		}
		@Override public void onCommand(Luwrain luwrain)
		{
		    final Strings strings = (Strings)luwrain.i18n().getStrings(Strings.NAME);
		    if (strings == null)
			return;
		    final String url = Popups.simple(luwrain, strings.openUrlPopupName(), strings.openUrlPopupPrefix(), "http://");
		    if (url != null && !url.trim().isEmpty())
			luwrain.launchApp("reader", new String[]{url.indexOf("://") >= 0?url:("http://" + url)});
		}
	    },

	    new Command(){
		@Override public String getName()
		{
		    return "reader-search-google";
		}
		@Override public void onCommand(Luwrain luwrain)
		{
		    final Strings strings = (Strings)luwrain.i18n().getStrings(Strings.NAME);
		    if (strings == null)
			return;
		    final String query = Popups.simple(luwrain, strings.searchGooglePopupName(), strings.searchGooglePopupPrefix(), "");
		    if (query != null && !query.trim().isEmpty())
			luwrain.launchApp("reader", new String[]{constructGoogleUrl(luwrain.getRegistry(), query)});
		}
	    },

	    //The same as reader-search-google
	    new Command(){
		@Override public String getName()
		{
		    return "reader-search";
		}
		@Override public void onCommand(Luwrain luwrain)
		{
		    final Strings strings = (Strings)luwrain.i18n().getStrings(Strings.NAME);
		    if (strings == null)
			return;
		    final String query = Popups.simple(luwrain, strings.searchGooglePopupName(), strings.searchGooglePopupPrefix(), "");
		    if (query != null && !query.trim().isEmpty())
			luwrain.launchApp("reader", new String[]{constructGoogleUrl(luwrain.getRegistry(), query)});
		}
	    },

	    new Command(){
		@Override public String getName()
		{
		    return "web-open";
		}
		@Override public void onCommand(Luwrain luwrain)
		{
		    final Strings strings = (Strings)luwrain.i18n().getStrings(Strings.NAME);
		    if (strings == null)
			return;
		    final String query = Popups.editWithHistory(luwrain, strings.openAutodetectPopupName(), strings.openAutodetectPopupPrefix(), "", queries, Popups.DEFAULT_POPUP_FLAGS);
		    if (query == null || query.trim().isEmpty())
			return;
		    if (query.trim().toLowerCase().startsWith("http://") || query.trim().toLowerCase().startsWith("https://"))
			luwrain.launchApp("reader", new String[]{query}); else
			luwrain.launchApp("reader", new String[]{constructGoogleUrl(luwrain.getRegistry(), query)});
		}
	    },
    */

    @Override public ExtensionObject[] getExtObjects(Luwrain luwrain)
    {
	return new ExtensionObject[]{

	    new DefaultShortcut("reader", App.class) {
		@Override public Application[] prepareApp(String[] args)
		{
		    if (args.length < 1)
			return new Application[]{new App()};
		    if (args.length == 1)
			return new Application[]{new App(args[0])};
		    /*
		    if (args.length == 2)
			return new Application[]{new App(args[0], args[1])};
		    */
		    return new Application[]{new App()};
		}
	    }
	};
	    }

    /*
    private String constructGoogleUrl(Registry registry, String query)
    {
	requireNonNull(registry, "registry can't be null");
	NullCheck.notEmpty(query, "query");
	final Settings sett = createSettings(registry);
	return "http://www.google.ru/search?q=" + URLEncoder.encode(query) + "&hl=" + sett.getGoogleLang("en") + "&ie=utf-8";
    }

    interface Settings
    {
	String getGoogleLang(String defValue);
	void setGoogleLang(String value);
    }

    static Settings createSettings(Registry registry)
    {
	requireNonNull(registry, "registry can't be null");
	return RegistryProxy.create(registry, "/org/luwrain/app/reader", Settings.class);
    }
    */
    @Override public UniRefProc[] getUniRefProcs(Luwrain luwrain)
    {
	requireNonNull(luwrain, "luwrain can't be null");
	return new UniRefProc[]{
	    new UniRefProc(){
		static private final String TYPE = "reader";
		@Override public String getUniRefType()
		{
		    return TYPE;
		}
		@Override public UniRefInfo getUniRefInfo(String uniRef)
		{
		    requireNonNull(uniRef, "uniRef can't be null");
		    if (uniRef.isEmpty() || !uniRef.startsWith(TYPE + ":"))
			return null;
		    final String url = uniRef.substring(TYPE.length() + 1);
		    if (url.isEmpty())
			return null;
		    return new UniRefInfo(uniRef, "reader", url, url);
		}
		@Override public boolean openUniRef(String uniRef, Luwrain luwrain)
		{
		    requireNonNull(uniRef, "uniRef can't be null");
		    requireNonNull(luwrain, "luwrain can't be null");
		    if (uniRef.isEmpty() || !uniRef.startsWith(TYPE + ":"))
			return false;
		    final String url = uniRef.substring(TYPE.length() + 1);
		    if (url.isEmpty())
			return false;
		    luwrain.launchApp("reader", new String[]{url});
		    return true;
		}
	    },
	};
    }
    
}

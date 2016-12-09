package com.anthonydenaud.arkrcon;


import android.content.Context;

import com.getsentry.raven.Raven;
import com.getsentry.raven.RavenFactory;
import com.getsentry.raven.event.Event;
import com.getsentry.raven.event.EventBuilder;
import com.getsentry.raven.event.interfaces.ExceptionInterface;

/**
 * Created by Anthony on 08/12/2016.
 */


public class RavenLogger {

    private static RavenLogger INSTANCE;

    private Raven raven;


    public  static synchronized RavenLogger getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new RavenLogger();
        }
        return INSTANCE;
    }

    public void init(Context context) {
        raven = RavenFactory.ravenInstance(context.getString(R.string.sentry_dsn));
    }

    public void info(Class klass, String message) {
        EventBuilder eventBuilder = new EventBuilder()
                .withMessage(message)
                .withLevel(Event.Level.INFO)
                .withLogger(klass.getName());
        raven.runBuilderHelpers(eventBuilder);
        raven.sendEvent(eventBuilder.build());
    }

    public void warn(Class klass, String message) {
        EventBuilder eventBuilder = new EventBuilder()
                .withMessage(message)
                .withLevel(Event.Level.WARNING)
                .withLogger(klass.getName());
        raven.runBuilderHelpers(eventBuilder);
        raven.sendEvent(eventBuilder.build());
    }

    public void warn(Class klass, String message, Throwable throwable) {
        EventBuilder eventBuilder = new EventBuilder()
                .withMessage(message)
                .withLevel(Event.Level.WARNING)
                .withLogger(klass.getName())
                .withSentryInterface(new ExceptionInterface(throwable));
        raven.runBuilderHelpers(eventBuilder);
        raven.sendEvent(eventBuilder.build());
    }

    public void error(Class klass, String message) {
        EventBuilder eventBuilder = new EventBuilder()
                .withMessage(message)
                .withLevel(Event.Level.ERROR)
                .withLogger(klass.getName());
        raven.runBuilderHelpers(eventBuilder);
        raven.sendEvent(eventBuilder.build());
    }

    public void error(Class klass, String message, Throwable throwable) {
        EventBuilder eventBuilder = new EventBuilder()
                .withMessage(message)
                .withLevel(Event.Level.ERROR)
                .withLogger(klass.getName())
                .withSentryInterface(new ExceptionInterface(throwable));
        raven.runBuilderHelpers(eventBuilder);
        raven.sendEvent(eventBuilder.build());
    }

}

package ru.statjobs.loader.loadsrv;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ListAppender extends AppenderBase<ILoggingEvent> {

    private static volatile Map<String,List<String>> logMap = new HashMap<>();

    @Override
    protected void append(ILoggingEvent iLoggingEvent) {
        if (!logMap.containsKey(iLoggingEvent.getLevel().toString())) {
            logMap.put(iLoggingEvent.getLevel().toString(), new ArrayList<>());
        }
        getLogMap().get(iLoggingEvent.getLevel().toString()).add(iLoggingEvent.getFormattedMessage());
    }

    synchronized public static Map<String, List<String>> getLogMap() {
        return logMap;
    }

    synchronized public static void clear() {
        logMap = new HashMap<>();
    }
}

package com.xinys.wenda.async;

import java.util.List;

public interface EventHandler {

    public void doHandle(EventModel eventModel);

    List<EventType> getSupportEventTypes();
}

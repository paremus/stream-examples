package com.paremus.example.trains.networkrail.messages;

import java.util.List;
import java.util.Map;

public class NetworkRailDataEvent {

    /**
     * The time delay between this event and the previous event
     */
    public long time;
    
    /**
     * The batch of Train movement messages that arrived in the event, encoded as JSON
     */
    public List<Map<String, Map<String,String>>> messages;
    
}

package com.paremus.example.trains.networkrail.messages;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.paremus.example.trains.networkrail.messages.TOC_ID.TOC_ID_Converter;

public class TrainMovementMessageBody {

    /**
     * The 10-character unique identity for this train at TRUST activation time
     */
    public String train_id;
    
    /**
     * The date and time that this event happened at the location
     */
    public String actual_timestamp;
    
    /**
     * The STANOX of the location at which this event happened
     */
    public String loc_stanox;

    /**
     * The planned GBTT (passenger) date and time that the event was due to happen at this location
     */
    public String gbtt_timestamp;
    
    /**
     * The planned date and time that this event was due to happen at this location
     */
    public String planned_timestamp;    
    
    /**
     * If the location has been revised, the STANOX of the location in the schedule at activation time
     */
    public String original_loc_stanox;
    
    /**
     * The planned departure time associated with the original location
     */
    public String original_loc_timestamp;    
    
    public enum EventType { ARRIVAL, DEPARTURE, DESTINATION }
    
    /**
     * The planned type of event
     *  - one of "ARRIVAL", "DEPARTURE" or "DESTINATION"
     */
    public EventType planned_event_type;    
    
    /**
     * The type of event
     *   - either "ARRIVAL" or "DEPARTURE"
     */
    public EventType event_type;    
    
    public enum EventSource { AUTOMATIC, MANUAL }
    
    /**
     * Whether the event source was "AUTOMATIC" from SMART, or "MANUAL" from TOPS or TRUST SDR
     */
    public EventSource event_source;
    
    /**
     * Set to "false" if this report is not a correction of a previous report, or "true" if it is
     */
    public Boolean correction_ind;
    
    /**
     * Set to "false" if this report is for a location in the schedule, or "true" if it is not
     */
    public Boolean offroute_ind;    
    
    /**
     * For automatic reports, either "UP" or "DOWN" depending on the direction of travel
     */
    public String direction_ind;
    
    /**
     * A single character (or blank) depending on the line the train is travelling on, e.g. F = Fast, S = Slow
     */
    public Character line_ind;
    
    /**
     * Two characters (including a space for a single character) or blank if the movement report is associated with a platform number
     */
    public String platform;
    
    /**
     * A single character (or blank) to indicate the exit route from this location
     */
    public Character route;
    
    /**
     * Where a train has had its identity changed, the current 10-character unique identity for this train
     */
    public String current_train_id;    
    
    /**
     * Train service code as per schedule
     */
    public String train_service_code;    
    
    /**
     * Operating company ID as per TOC Codes
     */
    @JsonDeserialize(converter=TOC_ID_Converter.class)
    public TOC_ID division_code;
    
    /**
     * Operating company ID as per TOC Codes
     */
    @JsonDeserialize(converter=TOC_ID_Converter.class)
    public TOC_ID toc_id;
    
    /**
     * The number of minutes variation from the scheduled time at this location. Off-route reports will contain "0"
     */
    public int timetable_variation;
    
    /**
     * One of "ON TIME", "EARLY", "LATE" or "OFF ROUTE"
     */
    public String variation_status;
    
    /**
     * The STANOX of the location at which the next report for this train is due
     */
    public String next_report_stanox;
    
    /**
     * The running time to the next location
     */
    public Integer next_report_run_time;
    
    /**
     * Set to "true" if the train has completed its journey, or "false" otherwise
     */
    public Boolean train_terminated;
    
    /**
     *     Set to "true" if this is a delay monitoring point, "false" if it is not. Off-route reports will contain "false"
     */
    public boolean delay_monitoring_point;
    
    /**
     * The TOPS train file address, if applicable
     */
    public String train_file_address;
    
    /**
     *     The STANOX of the location that generated this report. Set to "00000" for manual and off-route reports
     */
    public String reporting_stanox;
    
    /**    
     * Set to "true" if an automatic report is expected for this location, otherwise "false"
     */
    public boolean auto_expected;
    
}

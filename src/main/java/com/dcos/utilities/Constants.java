package com.dcos.utilities;


import java.util.Arrays;
import java.util.List;

public class Constants {
    public static final String INCIDENT_CLOSE_CODE = "Solved";
    public static final String INCIDENT_CLOSE_NOTES = "Infrastructure auto healed";
    public static final String INCIDENT_STATE_CLOSED = "6";
    public static final String INCIDENT_STATE_NEW = "1";
    public static final String INCIDENT_STATE_ACTIVE = "2";
    public static final String INCIDENT_STATE_PROBLEM_PENDING = "3";
    public static final String INCIDENT_STATE_PENDING_CALLER = "4";
    public static final String INCIDENT_STATE_PENDING_VENDOR = "5";

    public static final List<String> STATUS_LIST_TO_SEARCH_FOR_OPEN_INCIDENTS = Arrays.asList(INCIDENT_STATE_NEW,
            INCIDENT_STATE_ACTIVE,
            INCIDENT_STATE_PROBLEM_PENDING,
            INCIDENT_STATE_PENDING_CALLER,
            INCIDENT_STATE_PENDING_VENDOR);
}

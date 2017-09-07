package com.paremus.example.trains.networkrail.messages;

import java.util.Arrays;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.databind.util.Converter;

public enum TOC_ID {

    EB("Abellio Greater Anglia", 21, "LE"),
    HL("Arriva Trains Wales", 71, "AW"),
    HT("c2c", 79, "CC"),
    ES("Caledonian Sleeper", 35, "CS"),
    HO("Chiltern Railway", 74, "CH"),
    EH("CrossCountry", 27, "XC"),
    EN("Devon and Cornwall Railway", 34, "DC"),
    EM("East Midlands Trains", 28, "EM"),
    HB("East Coast", 61, "GR"),
    GA("Eurostar", 06, "ES"),
    EG("First Capital Connect (defunct)", 26, "FC"),
    EF("First Great Western", 25, "GW"),
    PF("First Hull Trains", 55, "HT"),
    HA("First Scotrail", 60, "SR"),
    EA("First Transpennine Express", 20, "TP"),
    HV("Gatwick Express", 81, "GX"),
    PE("GB Railfreight", 54, "ZZ"),
    EC("Grand Central", 22, "GC"),
    ET("Govia Thameslink Railway", 88, "GN"),
    EE("Heathrow Connect", 24, "HC"),
    HM("Heathrow Express", 86, "HX"),
    HZ("Island Lines", 85, "IL"),
    EJ("London Midland", 29, "LM"),
    EK("London Overground", 30, "LO"),
    XC("LUL Bakerloo Line", 91, "LT"),
    XB("LUL District Line – Wimbledon", 90, "LT"),
    XE("LUL District Line – Richmond", 93, "LT"),
    HE("Merseyrail", 64, "ME"),
    PG("Nexus (Tyne & Wear Metro)", 56, "TW"),
    PR("North Yorkshire Moors Railway", 51, "NY"),
    ED("Northern Rail", 23, "NT"),
    HY("South West Trains", 84, "SW"),
    HU("Southeastern", 80, "SE"),
    HW("Southern", 82, "SN"),
    EX("TfL Rail (will become Crossrail)", 33, "XB"),
    HF("Virgin Trains", 65, "VT"),
    PA("West Coast Railway Co.", 50, "WR"),
    UNKNOWN("Unknown", 0, "Unknown");
    
    
    private final String companyName;
    private final int numeric_code;
    private final String atoc_code;

    private TOC_ID(String companyName, int numeric_code, String atoc_code) {
        this.companyName = companyName;
        this.numeric_code = numeric_code;
        this.atoc_code = atoc_code;
    }

    public String getCompanyName() {
        return companyName;
    }

    public int getNumeric_code() {
        return numeric_code;
    }

    public String getAtoc_code() {
        return atoc_code;
    }

    public static TOC_ID fromNumericCode(int code) {
        return Arrays.stream(TOC_ID.values())
            .filter(id -> id.getNumeric_code() == code)
            .findFirst().orElseThrow(() -> new IllegalArgumentException("The code " + code + " does not match a known operator"));
    }
    
    public static class TOC_ID_Converter implements Converter<String, TOC_ID> {

        @Override
        public TOC_ID convert(String arg0) {
            try {
                int i = Integer.parseInt(arg0);
                
                return TOC_ID.fromNumericCode(i);
            } catch (NumberFormatException nfe) {
                return TOC_ID.valueOf(arg0);
            }
        }

        @Override
        public JavaType getInputType(TypeFactory arg0) {
            return arg0.constructType(String.class);
        }

        @Override
        public JavaType getOutputType(TypeFactory arg0) {
            return arg0.constructType(TOC_ID.class);
        }
        
    }
}

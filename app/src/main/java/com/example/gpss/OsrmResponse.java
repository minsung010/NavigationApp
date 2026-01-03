package com.example.gpss;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class OsrmResponse {
    @SerializedName("routes")
    public List<Route> routes;

    public static class Route {
        @SerializedName("geometry")
        public String geometry; // 인코딩된 폴리라인

        @SerializedName("legs")
        public List<Leg> legs;
    }

    public static class Leg {
        @SerializedName("steps")
        public List<Step> steps;

        @SerializedName("distance")
        public double distance; // 미터 단위

        @SerializedName("duration")
        public double duration; // 초 단위
    }

    public static class Step {
        @SerializedName("maneuver")
        public Maneuver maneuver;
        
        @SerializedName("name")
        public String name;
        
        @SerializedName("distance")
        public double distance;
    }
    
    public static class Maneuver {
        @SerializedName("type")
        public String type; // ex: "turn"
        
        @SerializedName("modifier")
        public String modifier; // ex: "left"
    }
}

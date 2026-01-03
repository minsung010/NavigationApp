package com.example.gpss;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class DirectionsResponse {
    @SerializedName("routes")
    public List<Route> routes;
    
    @SerializedName("status")
    public String status;

    @SerializedName("error_message")
    public String errorMessage;

    public static class Route {
        @SerializedName("legs")
        public List<Leg> legs;
        
        @SerializedName("overview_polyline")
        public OverviewPolyline overviewPolyline;
    }

    public static class Leg {
        @SerializedName("steps")
        public List<Step> steps;

        @SerializedName("distance")
        public Distance distance;

        @SerializedName("duration")
        public Duration duration;
    }

    public static class Step {
        @SerializedName("html_instructions")
        public String htmlInstructions;

        @SerializedName("distance")
        public Distance distance;

        @SerializedName("duration")
        public Duration duration;
        
        @SerializedName("maneuver")
        public String maneuver;
        
        @SerializedName("start_location")
        public LatLngLiteral startLocation;

        @SerializedName("end_location")
        public LatLngLiteral endLocation;
    }
    
    public static class LatLngLiteral {
        @SerializedName("lat")
        public double lat;
        @SerializedName("lng")
        public double lng;
    }

    public static class Distance {
        @SerializedName("text")
        public String text;
        @SerializedName("value")
        public int value;
    }

    public static class Duration {
        @SerializedName("text")
        public String text;
        @SerializedName("value")
        public int value;
    }
    
    public static class OverviewPolyline {
        @SerializedName("points")
        public String points;
    }
}

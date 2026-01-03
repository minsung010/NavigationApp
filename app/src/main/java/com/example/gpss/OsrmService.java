package com.example.gpss;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface OsrmService {
    // OSRM Public Demo Server
    // Format: /route/v1/driving/{longitude},{latitude};{longitude},{latitude}
    @GET("route/v1/driving/{coordinates}")
    Call<OsrmResponse> getRoute(
            @Path(value = "coordinates", encoded = true) String coordinates,
            @Query("overview") String overview, // "full"
            @Query("geometries") String geometries, // "polyline"
            @Query("steps") String steps // "true"
    );
}

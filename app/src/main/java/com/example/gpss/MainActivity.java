package com.example.gpss;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;

import android.widget.TextView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import android.widget.LinearLayout;
import android.graphics.Color;

import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;



import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import android.text.Html;
import android.speech.tts.TextToSpeech;
import android.location.Geocoder;
import android.location.Address;
import android.content.Intent;
import android.widget.ImageButton;
import android.util.Log;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import android.app.PendingIntent;
import android.widget.EditText;
import java.util.Locale;
import java.io.IOException;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final String TAG = "MainActivity";
    private static final float DISTANCE_THRESHOLD_MIN = 1.0f;
    private static final float DISTANCE_THRESHOLD_MAX = 5.0f; // 10 meters
    private FusedLocationProviderClient fusedLocationClient;
    private ActivityResultLauncher<String> locationPermissionRequest;
    private GoogleMap mMap;
    private LocationCallback locationCallback;
    private DatabaseReference locationsRef;
    private Marker userMarker;  // A single marker for the user's current location
    private Bitmap ambulanceBitmap; // Pre-loaded marker icon

    // Navigation UI Fields (New Portrait Mode)
    private LinearLayout searchContainer;
    private EditText searchEditText;
    private Button searchButton;
    private ImageButton myLocationButton;
    private Button startNavButton;
    
    private LinearLayout topTbtContainer;
    private TextView tbtMainRoad;
    private TextView tbtMainDist;
    private TextView tbtSubDist;
    private android.widget.ImageView tbtMainIcon;
    private android.widget.ImageView tbtSubIcon;
    
    private LinearLayout centerOverlay;
    private TextView centerDist;
    private android.widget.ImageView centerIcon;
    
    private TextView speedIndicator;
    
    private LinearLayout bottomInfoContainer;
    private TextView addressBar;
    private android.widget.ImageView refreshButtonIcon;
    private TextView navTime;
    private TextView navRemainingDist;
    private android.widget.ImageView stopNavButtonIcon;

    private androidx.constraintlayout.widget.ConstraintLayout ambulanceAlertCard; 
    private TextView alertTitle;
    private TextView alertMessage;
    private android.view.View alertClose;
    
    // Default Buttons Container
    private LinearLayout defaultButtons;
    
    // Logic Fields
    private boolean isNavigationMode = false;
    private Marker destinationMarker;
    private Polyline routePolyline;
    
    // Live Navigation Data
    private List<DirectionsResponse.Step> routeSteps;
    private int currentStepIndex = 0;
    private Location lastKnownLocation;
    private boolean isMapInitialized = false;
    private boolean hasSentAmbulanceNotification = false;
    private static final String CHANNEL_ID = "fcm_default_channel";

    // TTS
    private TextToSpeech tts;
    private String lastToldInstruction = "";
    
    // Rerouting and Advanced Navigation
    private static final float OFF_ROUTE_THRESHOLD = 50.0f; // 50m 이탈 시 재탐색
    private static final float ARRIVAL_THRESHOLD = 20.0f; // 20m 이내 도착 판정
    private LatLng lastDestination; // 재탐색 시 목적지 저장
    private long lastRerouteTime = 0; // 재탐색 쿨다운 (너무 빈번한 재탐색 방지)
    private static final long REROUTE_COOLDOWN_MS = 10000; // 10초 쿨다운
    
    // Distance-based Voice Guidance Flags
    private boolean announced300m = false;
    private boolean announced100m = false;
    private boolean announced50m = false;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Create the notification channel for devices running Android Oreo or higher
        // Create the notification channel for devices running Android Oreo or higher
        createNotificationChannel();

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        locationsRef = FirebaseDatabase.getInstance().getReference("users").child("app1").child("location");

        // Location permission request launcher
        locationPermissionRequest = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) {
                        startLocationUpdates();
                    } else {
                        Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
                    }
                });

        // Request permissions
        requestPermissions();

        // Setup map fragment
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // Initialize buttons
        Button zoomInButton = findViewById(R.id.zoomInButton);
        zoomInButton.setOnClickListener(v -> mMap.animateCamera(CameraUpdateFactory.zoomIn()));

        Button zoomOutButton = findViewById(R.id.zoomOutButton);
        zoomOutButton.setOnClickListener(v -> mMap.animateCamera(CameraUpdateFactory.zoomOut()));

        // Navigation UI Init (New Portrait Mode)
        topTbtContainer = findViewById(R.id.topTbtContainer);
        tbtMainRoad = findViewById(R.id.tbtMainRoad);
        tbtMainDist = findViewById(R.id.tbtMainDist);
        tbtSubDist = findViewById(R.id.tbtSubDist);
        tbtMainIcon = findViewById(R.id.tbtMainIcon);
        tbtSubIcon = findViewById(R.id.tbtSubIcon);

        centerOverlay = findViewById(R.id.centerOverlay);
        centerDist = findViewById(R.id.centerDist);
        centerIcon = findViewById(R.id.centerIcon);
        
        speedIndicator = findViewById(R.id.speedIndicator);
        
        bottomInfoContainer = findViewById(R.id.bottomInfoContainer);
        addressBar = findViewById(R.id.addressBar);
        refreshButtonIcon = findViewById(R.id.refreshButtonIcon);
        navTime = findViewById(R.id.navTime);
        navRemainingDist = findViewById(R.id.navRemainingDist);
        stopNavButtonIcon = findViewById(R.id.stopNavButtonIcon);
        
        ambulanceAlertCard = findViewById(R.id.ambulanceAlertCard);
        alertTitle = findViewById(R.id.alertTitle);
        alertMessage = findViewById(R.id.alertMessage);
        alertClose = findViewById(R.id.alertClose);
        
        defaultButtons = findViewById(R.id.defaultButtons);
        
        // Search & Start Nav
        searchContainer = findViewById(R.id.searchContainer);
        searchEditText = findViewById(R.id.searchEditText);
        searchButton = findViewById(R.id.searchButton);
        myLocationButton = findViewById(R.id.myLocationButton);
        startNavButton = findViewById(R.id.startNavButton);

        searchButton.setOnClickListener(v -> performSearch());
        myLocationButton.setOnClickListener(v -> moveToCurrentLocation());
        startNavButton.setOnClickListener(v -> {
             if (destinationMarker != null) {
                 startNavigationMode(destinationMarker.getPosition());
                 startNavButton.setVisibility(View.GONE);
                 searchContainer.setVisibility(View.GONE);
             }
        });

        // TTS Init
        tts = new TextToSpeech(this, status -> {
            if (status != TextToSpeech.ERROR) {
                tts.setLanguage(Locale.KOREAN);
            }
        });

        // Buttons
        stopNavButtonIcon.setOnClickListener(v -> stopNavigationMode());
        refreshButtonIcon.setOnClickListener(v -> refreshLocation()); // Re-use refresh logic
        
        // Alert Close Button
        alertClose.setOnClickListener(v -> ambulanceAlertCard.setVisibility(View.GONE));

        // Fetch FCM token
        fetchFcmToken();

        // Adjust padding for system bars
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Monitor Firebase location updates
        monitorLocationUpdates();
        
        // Start monitoring ambulance dispatch
        monitorAmbulanceLocation();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        boolean locationGranted = false;
        for (int i = 0; i < permissions.length; i++) {
            if (permissions[i].equals(Manifest.permission.ACCESS_FINE_LOCATION) && grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                locationGranted = true;
            }
        }
        
        if (locationGranted) {
            enableMyLocation();
            startLocationUpdates();
            moveToCurrentLocation();
        }
    }
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Default";
            String description = "Default channel for notifications";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("default", name, importance);
            channel.setDescription(description);

            // Register the channel with the system
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    private void requestPermissions() {
        List<String> permissionsToRequest = new ArrayList<>();
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS);
            }
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }

        if (!permissionsToRequest.isEmpty()) {
            ActivityCompat.requestPermissions(this, permissionsToRequest.toArray(new String[0]), 1);
        } else {
            // All permissions already granted
            startLocationUpdates();
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        isMapInitialized = true;

        // 초기 위치 대전으로 설정 (대전시청 부근) 및 건물들이 보이도록 카메라 각도(tilt) 조정
        LatLng daejeon = new LatLng(36.350411, 127.384548);
        CameraPosition initialPosition = new CameraPosition.Builder()
                .target(daejeon)
                .zoom(17.0f) // 건물들이 잘 보이도록 줌 레벨 조정
                .tilt(60.0f) // 3D 느낌을 위해 기울기 설정 (0~90)
                .bearing(0)
                .build();
        mMap.moveCamera(CameraUpdateFactory.newCameraPosition(initialPosition));

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            enableMyLocation();
            startLocationUpdates();
            // 내 위치를 가져오기 전까지는 대전을 보여줌
        } else {
            requestPermissions();
        }
    }

    private void enableMyLocation() {
        if (mMap != null && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(false); // We use our own button
            mMap.setBuildingsEnabled(true);
            mMap.setTrafficEnabled(true);
            
            mMap.setOnMapLongClickListener(this::setDestination);
        }
    }

    private void startLocationUpdates() {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setInterval(5000);
        locationRequest.setFastestInterval(2000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    lastKnownLocation = location;
                    updateFirebaseLocation(location);
                    
                    if (isNavigationMode) {
                        updateNavigationCamera(location);
                        updateNavigationUI(location);
                    }
                }
            }
        };

        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
    }

    private void updateFirebaseLocation(Location location) {
        if (location != null) {
            Log.d(TAG, "Location: " + location.getLatitude() + ", " + location.getLongitude());
            String userId = "user"; // Unique user ID

            // Save latitude and longitude as a Map
            Map<String, Object> locationData = new HashMap<>();
            locationData.put("latitude", location.getLatitude());
            locationData.put("longitude", location.getLongitude());
            locationsRef.child(userId).setValue(locationData);

            // 현재 사용자 위치는 지도에 표시하지 않음
            if (userMarker != null) {
                userMarker.remove();
                userMarker = null;
            }
        } else {
            // Remove marker if location is null
            if (userMarker != null) {
                userMarker.remove();
                userMarker = null;
            }
        }
    }

    private void moveToCurrentLocation() {
        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
                LatLng currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                CameraPosition cameraPosition = new CameraPosition.Builder()
                        .target(currentLatLng)
                        .zoom(17.0f)
                        .tilt(60.0f)
                        .build();
                mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
            } else {
                Log.e(TAG, "Current location is null.");
            }
        });
    }

    private void refreshLocation() {
        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
                updateFirebaseLocation(location);
            }
        });
    }

    private void fetchFcmToken() {
        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                String token = task.getResult();
                Log.d(TAG, "FCM Token: " + token);
            } else {
                Log.w(TAG, "Fetching FCM token failed", task.getException());
            }
        });
    }

    private void handleMarkerForOtherUsers(String userId, LatLng latLng) {
        // 현재 위치를 가져오기 위한 Location 객체
        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
                // 현재 위치와 다른 사용자 위치 간의 거리 계산
                Location currentLocation = new Location("currentLocation");
                currentLocation.setLatitude(location.getLatitude());
                currentLocation.setLongitude(location.getLongitude());

                // 다른 사용자의 위치로 Location 객체 생성
                Location otherUserLocation = new Location("otherUserLocation");
                otherUserLocation.setLatitude(latLng.latitude);
                otherUserLocation.setLongitude(latLng.longitude);

                // 거리 계산
                float distance = currentLocation.distanceTo(otherUserLocation);

                // 100미터 이상이면 마커를 표시하지 않음
                if (distance >=  DISTANCE_THRESHOLD_MIN && distance <= DISTANCE_THRESHOLD_MAX) {
                    // 현재 사용자는 표시하지 않고, 다른 사용자만 표시
                    if (!userId.equals("user")) {  // 현재 사용자 ID는 표시하지 않음
                        if (userMarker == null) {
                            // 마커 아이콘 설정
                            if (ambulanceBitmap != null) {
                                userMarker = mMap.addMarker(new MarkerOptions()
                                        .position(latLng)
                                        .title(userId + "'s Location")
                                        .icon(BitmapDescriptorFactory.fromBitmap(ambulanceBitmap))); // 리사이즈된 이미지 사용
                            }

                            // Show the Alert Card
                            if (ambulanceAlertCard != null) {
                                alertTitle.setText("주변 사용자 감지");
                                alertMessage.setText("전방 100m 이내에\n사용자가 있습니다.");
                                ambulanceAlertCard.setVisibility(View.VISIBLE);
                            }
                        } else {
                            // 기존 마커의 위치 업데이트
                            userMarker.setPosition(latLng);
                        }
                    }
                } else {
                    // 100미터 이상이면 마커 제거
                    if (userMarker != null) {
                        userMarker.remove();
                        userMarker = null;

                        // Hide the Alert Card
                        if (ambulanceAlertCard != null) {
                            ambulanceAlertCard.setVisibility(View.GONE);
                        }
                    }
                }
            } else {
                // Handle the case where current location is null
                if (userMarker != null) {
                    userMarker.remove();
                    userMarker = null;

                    // Hide the Alert Card
                    if (ambulanceAlertCard != null) {
                        ambulanceAlertCard.setVisibility(View.GONE);
                    }
                }
            }
        });
    }


    private void removeUserMarker() {
        if (userMarker != null) {
            userMarker.remove();
            userMarker = null;
        }
    }

    private void clearAllMarkers() {
        if (userMarker != null) {
            userMarker.remove();
            userMarker = null;
        }
    }





    private void saveTokenToDatabase(String token) {
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child("app1");
        userRef.child("fcmToken").setValue(token);  // FCM 토큰을 Firebase Database에 저장
    }

    private void monitorLocationUpdates() {
        locationsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Process each user
                    boolean hasActiveMarkers = false;  // Flag to check if there are active markers

                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        String userId = snapshot.getKey();
                        if (userId != null && !userId.equals("user")) {  // 현재 사용자 제외
                            Log.d(TAG, "Processing user: " + userId);

                            // Check if latitude and longitude are available
                            if (snapshot.hasChild("latitude") && snapshot.hasChild("longitude")) {
                                Double latitude = snapshot.child("latitude").getValue(Double.class);
                                Double longitude = snapshot.child("longitude").getValue(Double.class);

                                if (latitude != null && longitude != null) {
                                    LatLng latLng = new LatLng(latitude, longitude);
                                    handleMarkerForOtherUsers(userId, latLng); // Update the marker for other users
                                    hasActiveMarkers = true; // Mark that there is at least one active marker
                                }
                            }
                        }
                    }

                    // If there are no active markers, hide the view
                    if (!hasActiveMarkers && ambulanceAlertCard != null) {
                        ambulanceAlertCard.setVisibility(View.GONE);
                    }
                } else {
                    // No data available in Firebase, hide the view
                    if (ambulanceAlertCard != null) {
                        ambulanceAlertCard.setVisibility(View.GONE);
                    }
                    Log.d(TAG, "No location data available in Firebase.");
                    // Remove all markers if there's no data
                    clearAllMarkers();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.w(TAG, "Failed to read value.", databaseError.toException());
            }
        });
    }
    // --- Ambulance Receiver Logic ---
    private DatabaseReference ambulanceRef;
    private Marker ambulanceMarker;

    private void monitorAmbulanceLocation() {
        // Listen to the specific ambulance node we defined in the Ambulance App
        ambulanceRef = FirebaseDatabase.getInstance().getReference("ambulance").child("ambulance_1");

        ambulanceRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    try {
                        Double lat = snapshot.child("latitude").getValue(Double.class);
                        Double lng = snapshot.child("longitude").getValue(Double.class);
                        Boolean isDispatching = snapshot.child("isDispatching").getValue(Boolean.class);

                        // If sending invalid data or stopped dispatching
                        if (lat == null || lng == null || isDispatching == null || !isDispatching) {
                            removeAmbulanceMarker();
                            return;
                        }

                        LatLng ambulancePos = new LatLng(lat, lng);
                        updateAmbulanceMarker(ambulancePos);

                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing ambulance data: " + e.getMessage());
                    }
                } else {
                    removeAmbulanceMarker();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.w(TAG, "Ambulance listener cancelled: " + error.getMessage());
            }
        });
    }

    private void updateAmbulanceMarker(LatLng position) {
        if (lastKnownLocation == null) {
            removeAmbulanceMarker();
            return;
        }

        Location ambulanceLoc = new Location("");
        ambulanceLoc.setLatitude(position.latitude);
        ambulanceLoc.setLongitude(position.longitude);

        float distance = lastKnownLocation.distanceTo(ambulanceLoc);

        // 500m 이내일 때만 알림 표시 (사용자 요청: 반경 내)
        if (distance > 500) {
            removeAmbulanceMarker();
            return;
        }

        if (ambulanceMarker == null) {
            MarkerOptions options = new MarkerOptions()
                    .position(position)
                    .title("EMERGENCY AMBULANCE")
                    .snippet("Approaching! Please yield.");
            
            if (ambulanceBitmap != null) {
                options.icon(BitmapDescriptorFactory.fromBitmap(ambulanceBitmap));
            } else {
                options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
            }

            ambulanceMarker = mMap.addMarker(options);
            Toast.makeText(MainActivity.this, "구급차 출동 감지됨!", Toast.LENGTH_LONG).show();
            
            // 시스템 알림(Local Push) 전송
            if (!hasSentAmbulanceNotification) {
                sendSystemNotification("🚨 구급차 접근 중 🚨", 
                    String.format("전방 %.0fm에 구급차가 접근 중입니다. 좌우로 피해 길을 비켜주세요.", distance));
                hasSentAmbulanceNotification = true;
            }
        } else {
            ambulanceMarker.setPosition(position);
        }

        // 알림 카드 업데이트
        if (ambulanceAlertCard != null) {
            alertTitle.setText("119 출동차량 접근중");
            String msg = String.format("전방 %.0fm\n구급차 접근 중! 좌우로 피해 넓은 길을 만들어주세요.", distance);
            alertMessage.setText(msg);
            ambulanceAlertCard.setVisibility(View.VISIBLE);
        }
    }

    private void removeAmbulanceMarker() {
        if (ambulanceMarker != null) {
            ambulanceMarker.remove();
            ambulanceMarker = null;
        }
        if (ambulanceAlertCard != null) {
            ambulanceAlertCard.setVisibility(View.GONE);
        }
        hasSentAmbulanceNotification = false; // 상태 초기화
    }

    private void sendSystemNotification(String title, String message) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent,
                PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_alert)
                .setContentTitle(title)
                .setContentText(message)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setContentIntent(pendingIntent);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
            notificationManager.notify(1001, builder.build());
        }
    }
    // --- End Ambulance Receiver Logic ---

    private void prepareAmbulanceBitmap() {
        try {
            Bitmap originalBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ambulance_icon);
            if (originalBitmap != null) {
                int width = 100;  // Icon width
                int height = 100; // Icon height
                ambulanceBitmap = Bitmap.createScaledBitmap(originalBitmap, width, height, false);
            } else {
                Log.e(TAG, "Ambulance icon resource not found or failed to decode.");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error preparing ambulance bitmap: " + e.getMessage());
        }
    }

    // --- Navigation Custom Logic ---

    private void setDestination(LatLng latLng) {
        if (destinationMarker != null) {
            destinationMarker.remove();
        }
        if (routePolyline != null) {
            routePolyline.remove();
        }

        destinationMarker = mMap.addMarker(new MarkerOptions()
                .position(latLng)
                .title("목적지")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));

        // 시작점(내 위치) -> 목적지 경로 요청 (Real Routing)
        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
                LatLng currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                requestRoute(currentLatLng, latLng);
            }
        });
    }

    private void requestRoute(LatLng origin, LatLng dest) {
        // OSRM Public Server 사용
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://router.project-osrm.org/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        
        OsrmService service = retrofit.create(OsrmService.class);
        
        // OSRM 좌표 포맷: "경도,위도;경도,위도" (구글과 반대: lng,lat)
        String coordinates = String.format(Locale.US, "%.6f,%.6f;%.6f,%.6f", 
                origin.longitude, origin.latitude, dest.longitude, dest.latitude);

        Log.d(TAG, "OSRM Request: " + coordinates);

        service.getRoute(coordinates, "full", "polyline", "true").enqueue(new Callback<OsrmResponse>() {
            @Override
            public void onResponse(Call<OsrmResponse> call, Response<OsrmResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    if (response.body().routes != null && !response.body().routes.isEmpty()) {
                        OsrmResponse.Route route = response.body().routes.get(0);
                        
                        // 1. Draw Polyline
                        List<LatLng> points = decodePolyline(route.geometry);
                        drawRoute(points);
                        
                        // 2. Setup Navigation Data
                        if (route.legs != null && !route.legs.isEmpty()) {
                            OsrmResponse.Leg leg = route.legs.get(0);
                            
                            // Convert OSRM steps to Google logic if needed, or simplfy
                            // For this demo, we will restart navigation with basic data
                            
                            // 거리 및 시간 표시
                            int distMeters = (int) leg.distance;
                            int durationSeconds = (int) leg.duration;
                            
                            String distText = distMeters >= 1000 ? String.format(Locale.getDefault(), "%.1f km", distMeters / 1000.0) : distMeters + " m";
                            String timeText = durationSeconds / 60 + "분 소요";
                            
                            navRemainingDist.setText(distText);
                            navTime.setText(timeText);
                            
                            startNavButton.setVisibility(View.VISIBLE);
                            
                            // OSRM Steps to simpler format for our app
                            // (Simulating Google Steps struct for compatibility would be complex, 
                            // so we will just enable nav mode using the Polyline)
                            
                            zoomToFitRoute(points);
                            Toast.makeText(MainActivity.this, "경로 탐색 성공 (OSRM)", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(MainActivity.this, "경로를 찾을 수 없습니다.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.e(TAG, "OSRM Error: " + response.code());
                    Toast.makeText(MainActivity.this, "경로 서버 응답 오류", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<OsrmResponse> call, Throwable t) {
                Log.e(TAG, "Network failure", t);
                Toast.makeText(MainActivity.this, "네트워크 오류: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void zoomToFitRoute(List<LatLng> points) {
        if (points == null || points.isEmpty()) return;
        com.google.android.gms.maps.model.LatLngBounds.Builder builder = new com.google.android.gms.maps.model.LatLngBounds.Builder();
        for (LatLng latLng : points) {
            builder.include(latLng);
        }
        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 100));
    }

    private void drawRoute(List<LatLng> points) {
        if (routePolyline != null) routePolyline.remove();
        
        PolylineOptions options = new PolylineOptions()
                .addAll(points)
                .width(20)
                .color(Color.BLUE) // Main Route Color
                .geodesic(true);
        
        routePolyline = mMap.addPolyline(options);
    }

    private String getApiKeyFromManifest() {
        try {
            Bundle bundle = getPackageManager().getApplicationInfo(getPackageName(), PackageManager.GET_META_DATA).metaData;
            return bundle.getString("com.google.android.geo.API_KEY");
        } catch (PackageManager.NameNotFoundException e) {
            return null;
        }
    }
    
    /**
     * Google Directions API에서 반환된 인코딩된 폴리라인 문자열을 디코딩합니다.
     * Google Maps 폴리라인 알고리즘을 사용하여 인코딩된 문자열을 LatLng 좌표 목록으로 변환합니다.
     * 
     * @param encoded 인코딩된 폴리라인 문자열
     * @return 디코딩된 LatLng 좌표 목록
     */
    private List<LatLng> decodePolyline(String encoded) {
        List<LatLng> poly = new ArrayList<>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            LatLng p = new LatLng((((double) lat / 1E5)),
                    (((double) lng / 1E5)));
            poly.add(p);
        }
        return poly;
    }

    /**
     * 3D 내비게이션 UI를 활성화하고 경로 안내를 시작합니다.
     * UI 레이아웃을 세로 모드에 최적화된 내비게이션 위젯으로 전환하고 초기 TTS 안내를 실행합니다.
     * 
     * @param destLatLng 목적지 좌표
     */
    private void startNavigationMode(LatLng destLatLng) {
        isNavigationMode = true;
        
        // 내비게이션 UI로 전환 (세로 모드 최적화)
        topTbtContainer.setVisibility(View.VISIBLE);
        centerOverlay.setVisibility(View.VISIBLE);
        speedIndicator.setVisibility(View.VISIBLE);
        bottomInfoContainer.setVisibility(View.VISIBLE);
        
        defaultButtons.setVisibility(View.GONE);
        
        // 초기 텍스트 설정
        tbtMainRoad.setText("경로 탐색중");
        tbtMainDist.setText("...");
        addressBar.setText("목적지 안내를 시작합니다");
        
        Toast.makeText(this, "경로 안내를 시작합니다.", Toast.LENGTH_SHORT).show();
        
        // TTS
        if (routeSteps != null && !routeSteps.isEmpty()) {
            speak(Html.fromHtml(routeSteps.get(0).htmlInstructions, Html.FROM_HTML_MODE_COMPACT).toString() + " 방향으로 안내를 시작합니다.");
        }
    }

    private void performSearch() {
        String query = searchEditText.getText().toString().trim();
        if (query.isEmpty()) return;

        Geocoder geocoder = new Geocoder(this, Locale.KOREAN);
        try {
            List<Address> addresses = geocoder.getFromLocationName(query, 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                LatLng targetLatLng = new LatLng(address.getLatitude(), address.getLongitude());
                
                // Set destination marker and request route
                setDestination(targetLatLng);
                
                // Move camera to search result
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(targetLatLng, 15));
                Toast.makeText(this, address.getAddressLine(0), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "검색 결과가 없습니다.", Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "검색 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show();
        }
    }

    private void speak(String text) {
        if (tts != null) {
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
        }
    }

    private void stopNavigationMode() {
        isNavigationMode = false;
        
        // UI 복구
        topTbtContainer.setVisibility(View.GONE);
        centerOverlay.setVisibility(View.GONE);
        speedIndicator.setVisibility(View.GONE);
        bottomInfoContainer.setVisibility(View.GONE);
        
        defaultButtons.setVisibility(View.VISIBLE);
        searchContainer.setVisibility(View.VISIBLE);
        startNavButton.setVisibility(View.GONE);
        ambulanceAlertCard.setVisibility(View.GONE); // Reset alert too
        
        // 네비게이션 상태 초기화
        routeSteps = null;
        currentStepIndex = 0;
        lastToldInstruction = "";
        resetDistanceAnnouncements();
        
        // 목적지 초기화
        if (destinationMarker != null) {
            destinationMarker.remove();
            destinationMarker = null;
        }
        if (routePolyline != null) {
            routePolyline.remove();
            routePolyline = null;
        }

        // 카메라를 다시 평면으로 돌림
        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
                CameraPosition cameraPosition = new CameraPosition.Builder()
                        .target(new LatLng(location.getLatitude(), location.getLongitude()))
                        .zoom(16)
                        .tilt(0) // Reset tilt
                        .bearing(0) // Reset bearing
                        .build();
                mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
            }
        });
        
        Toast.makeText(this, "안내를 종료했습니다.", Toast.LENGTH_SHORT).show();
    }

    /**
     * 사용자를 따라가는 "3D 모드"로 지도의 카메라를 업데이트합니다.
     * 사용자의 현재 GPS 베어링(방향)과 속도에 맞춰 틸트, 줌, 회전각을 조정합니다.
     * 
     * @param location 현재 사용자 위치 정보
     */
    private void updateNavigationCamera(Location location) {
        if (mMap == null) return;

        // "3D 팔로우 모드" 카메라 시점 조정
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(new LatLng(location.getLatitude(), location.getLongitude()))
                .zoom(18) // 주행 모드 줌 레벨
                .tilt(60) // 눕혀서 보기 (3D)
                .bearing(location.getBearing()) // 진행 방향으로 회전
                .build();
        
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition), 1000, null);
    }
    
    private void updateNavigationUI(Location location) {
        if (destinationMarker == null) return;
        
        LatLng destPos = destinationMarker.getPosition();
        
        // --- 0. Check for Arrival ---
        Location finalDest = new Location("final");
        finalDest.setLatitude(destPos.latitude);
        finalDest.setLongitude(destPos.longitude);
        float totalDist = location.distanceTo(finalDest);
        
        if (totalDist < ARRIVAL_THRESHOLD) {
            // 목적지 도착!
            tbtMainRoad.setText("도착");
            addressBar.setText("목적지에 도착했습니다!");
            speak("목적지에 도착했습니다. 안내를 종료합니다.");
            
            // 3초 후 안내 종료
            new android.os.Handler().postDelayed(() -> {
                stopNavigationMode();
            }, 3000);
            return;
        }
        
        // --- 1. Off-Route Detection & Rerouting ---
        if (routeSteps != null && !routeSteps.isEmpty()) {
            float minDistToRoute = calculateMinDistanceToRoute(location);
            
            if (minDistToRoute > OFF_ROUTE_THRESHOLD) {
                long currentTime = System.currentTimeMillis();
                if (currentTime - lastRerouteTime > REROUTE_COOLDOWN_MS) {
                    Log.d(TAG, "경로 이탈 감지! 거리: " + minDistToRoute + "m. 재탐색 시작.");
                    speak("경로를 이탈했습니다. 새로운 경로를 탐색합니다.");
                    
                    // 재탐색
                    LatLng currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                    requestRouteForRerouting(currentLatLng, destPos);
                    lastRerouteTime = currentTime;
                }
            }
        }
        
        // --- 2. Step Tracking Logic with Distance-based Announcements ---
        if (routeSteps != null && currentStepIndex < routeSteps.size()) {
            DirectionsResponse.Step currentStep = routeSteps.get(currentStepIndex);
            
            // Check distance to end of current step
            Location stepEnd = new Location("stepEnd");
            stepEnd.setLatitude(currentStep.endLocation.lat);
            stepEnd.setLongitude(currentStep.endLocation.lng);
            
            float distToTurn = location.distanceTo(stepEnd);
            
            // Distance-based voice announcements
            String nextInstruction = Html.fromHtml(currentStep.htmlInstructions, Html.FROM_HTML_MODE_COMPACT).toString();
            announceByDistance(distToTurn, nextInstruction);
            
            // If close enough (e.g., 30m) and not the last step, advance
            if (distToTurn < 30 && currentStepIndex < routeSteps.size() - 1) {
                currentStepIndex++;
                resetDistanceAnnouncements(); // 다음 스텝을 위해 플래그 초기화
                updateStepUI(); // Refresh TBT text
                
                // Recalculate dist for new step
                currentStep = routeSteps.get(currentStepIndex);
                stepEnd.setLatitude(currentStep.endLocation.lat);
                stepEnd.setLongitude(currentStep.endLocation.lng);
                distToTurn = location.distanceTo(stepEnd);
            }
            
            // Update TBT Distance (Dynamic)
            String distText;
            if (distToTurn >= 1000) {
                distText = String.format("%.1fkm", distToTurn / 1000);
            } else {
                distText = String.format("%dm", (int)distToTurn);
            }
            tbtMainDist.setText(distText);
            centerDist.setText(distText);
            
            // Next Step Info (Sub TBT)
             if (currentStepIndex + 1 < routeSteps.size()) {
                 DirectionsResponse.Step nextStep = routeSteps.get(currentStepIndex + 1);
                 tbtSubDist.setText(nextStep.distance.text);
             } else {
                 tbtSubDist.setText("");
             }
             
        } else {
            // Fallback if no steps (straight line mode)
             Location destLoc = new Location("dest");
            destLoc.setLatitude(destPos.latitude);
            destLoc.setLongitude(destPos.longitude);
            float d = location.distanceTo(destLoc);
            tbtMainDist.setText(String.format("%dm", (int)d));
        }

        // --- 3. General UI Update ---
        
        // Bottom Bar Remaining Dist (Total)
        if (totalDist >= 1000) {
            navRemainingDist.setText(String.format("%.1fkm", totalDist / 1000));
        } else {
            navRemainingDist.setText(String.format("%dm", (int)totalDist));
        }
        
        // Estimated Time (simple estimation: 40km/h average)
        float avgSpeedMps = 11.1f; // ~40 km/h
        int estimatedSeconds = (int)(totalDist / avgSpeedMps);
        int minutes = estimatedSeconds / 60;
        if (minutes > 0) {
            navTime.setText(minutes + "분 소요");
        } else {
            navTime.setText("곧 도착");
        }
        
        // Speed
        float speedKmh = (location.getSpeed() * 3.6f);
        speedIndicator.setText(String.valueOf((int)speedKmh));
    }
    
    /**
     * 현재 위치에서 경로 폴리라인까지의 최소 거리를 계산합니다.
     */
    private float calculateMinDistanceToRoute(Location location) {
        if (routeSteps == null || routeSteps.isEmpty()) return 0;
        
        float minDist = Float.MAX_VALUE;
        
        for (DirectionsResponse.Step step : routeSteps) {
            Location start = new Location("start");
            start.setLatitude(step.startLocation.lat);
            start.setLongitude(step.startLocation.lng);
            
            Location end = new Location("end");
            end.setLatitude(step.endLocation.lat);
            end.setLongitude(step.endLocation.lng);
            
            // 간단한 방식: 시작점/끝점까지의 거리 중 최소값
            float distToStart = location.distanceTo(start);
            float distToEnd = location.distanceTo(end);
            
            minDist = Math.min(minDist, Math.min(distToStart, distToEnd));
        }
        
        return minDist;
    }
    
    /**
     * 거리 기반 음성 안내를 수행합니다.
     */
    private void announceByDistance(float distance, String instruction) {
        if (distance <= 300 && distance > 100 && !announced300m) {
            speak("300미터 앞에서 " + instruction);
            announced300m = true;
        } else if (distance <= 100 && distance > 50 && !announced100m) {
            speak("100미터 앞에서 " + instruction);
            announced100m = true;
        } else if (distance <= 50 && !announced50m) {
            speak("곧 " + instruction);
            announced50m = true;
        }
    }
    
    /**
     * 다음 스텝으로 이동할 때 거리 안내 플래그를 초기화합니다.
     */
    private void resetDistanceAnnouncements() {
        announced300m = false;
        announced100m = false;
        announced50m = false;
    }
    
    /**
     * 경로 재탐색을 위한 API 호출 (음성 안내 포함)
     */
    private void requestRouteForRerouting(LatLng origin, LatLng dest) {
        String apiKey = getApiKeyFromManifest(); 

        if (apiKey == null || apiKey.isEmpty()) {
            Log.e(TAG, "API Key is missing!");
            return;
        }

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://maps.googleapis.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        
        DirectionsService service = retrofit.create(DirectionsService.class);
        
        String originStr = String.format(Locale.US, "%.6f,%.6f", origin.latitude, origin.longitude);
        String destStr = String.format(Locale.US, "%.6f,%.6f", dest.latitude, dest.longitude);

        Log.d(TAG, "Rerouting: origin=" + originStr + ", destination=" + destStr);

        service.getDirections(originStr, destStr, "driving", apiKey, "ko")
                .enqueue(new Callback<DirectionsResponse>() {
            @Override
            public void onResponse(Call<DirectionsResponse> call, Response<DirectionsResponse> response) {
                if (response.isSuccessful() && response.body() != null && "OK".equals(response.body().status)) {
                    if (response.body().routes != null && !response.body().routes.isEmpty()) {
                        DirectionsResponse.Route route = response.body().routes.get(0);
                        
                        // 새 경로 그리기
                        List<LatLng> points = decodePolyline(route.overviewPolyline.points);
                        drawRoute(points);
                        
                        // 새 스텝 저장
                        if (!route.legs.isEmpty()) {
                            routeSteps = route.legs.get(0).steps;
                            currentStepIndex = 0;
                            resetDistanceAnnouncements();
                            updateStepUI();
                            
                            speak("새로운 경로를 찾았습니다. " + 
                                  Html.fromHtml(routeSteps.get(0).htmlInstructions, Html.FROM_HTML_MODE_COMPACT).toString());
                            
                            Toast.makeText(MainActivity.this, "경로가 재탐색되었습니다.", Toast.LENGTH_SHORT).show();
                        }
                    }
                } else {
                    Log.e(TAG, "Rerouting failed: " + (response.body() != null ? response.body().status : "null"));
                }
            }

            @Override
            public void onFailure(Call<DirectionsResponse> call, Throwable t) {
                Log.e(TAG, "Rerouting network failure", t);
            }
        });
    }
    
    private void updateStepUI() {
        if (routeSteps == null || currentStepIndex >= routeSteps.size()) return;
        
        DirectionsResponse.Step step = routeSteps.get(currentStepIndex);
        String instruction = Html.fromHtml(step.htmlInstructions, Html.FROM_HTML_MODE_COMPACT).toString();
        
        // Clean up instruction (remove "Head..." parts if verbose)
        tbtMainRoad.setText(instruction);

        // TTS for new instruction (새로운 스텝 시작 시에만)
        if (!instruction.equals(lastToldInstruction)) {
            // 첫 번째 스텝이 아닌 경우에만 TTS (재진입 방지)
            if (currentStepIndex > 0) {
                speak(instruction);
            }
            lastToldInstruction = instruction;
        }
        
        // Update Current Step Icon
        int iconRes = getManeuverIcon(step.maneuver);
        if (tbtMainIcon != null) tbtMainIcon.setImageResource(iconRes);
        if (centerIcon != null) centerIcon.setImageResource(iconRes);
        
        // Update Next Step Icon (Sub TBT)
        if (currentStepIndex + 1 < routeSteps.size()) {
            DirectionsResponse.Step nextStep = routeSteps.get(currentStepIndex + 1);
            int nextIconRes = getManeuverIcon(nextStep.maneuver);
            if (tbtSubIcon != null) tbtSubIcon.setImageResource(nextIconRes);
        } else {
            // 마지막 스텝이면 목적지 아이콘
            if (tbtSubIcon != null) tbtSubIcon.setImageResource(R.drawable.ic_destination);
        }
    }
    
    /**
     * maneuver 문자열에 따른 적절한 방향 아이콘 리소스 ID 반환
     */
    private int getManeuverIcon(String maneuver) {
        if (maneuver == null) {
            return R.drawable.ic_straight; // Default: 직진
        }
        
        String m = maneuver.toLowerCase();
        
        if (m.contains("uturn")) {
            return R.drawable.ic_uturn;
        } else if (m.contains("left")) {
            if (m.contains("slight") || m.contains("bear")) {
                return R.drawable.ic_slight_left;
            } else {
                return R.drawable.ic_turn_left;
            }
        } else if (m.contains("right")) {
            if (m.contains("slight") || m.contains("bear")) {
                return R.drawable.ic_slight_right;
            } else {
                return R.drawable.ic_turn_right;
            }
        } else if (m.contains("straight") || m.contains("continue") || m.contains("head")) {
            return R.drawable.ic_straight;
        } else if (m.contains("merge") || m.contains("ramp")) {
            return R.drawable.ic_straight;
        } else if (m.contains("destination") || m.contains("arrive")) {
            return R.drawable.ic_destination;
        }
        
        return R.drawable.ic_straight; // Default fallback
    }
}
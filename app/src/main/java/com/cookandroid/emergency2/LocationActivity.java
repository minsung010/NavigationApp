package com.cookandroid.emergency2;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.android.gms.maps.model.Marker;
import android.widget.Toast;

public class LocationActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private TextView currentLocationText;
    private FusedLocationProviderClient fusedLocationClient; // FusedLocationProviderClient 선언
    private GoogleMap mMap;
    private float zoomLevel = 15.0f; // 초기 줌 레벨

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location);

        currentLocationText = findViewById(R.id.currentLocationText);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this); // FusedLocationProviderClient 초기화

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        checkLocationPermission(); // 위치 권한 확인

        // 버튼 초기화
        Button zoomInButton = findViewById(R.id.zoomInButton);
        Button zoomOutButton = findViewById(R.id.zoomOutButton);

        // 확대 버튼 클릭 리스너
        zoomInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                zoomLevel++; // 줌 레벨 증가
                if (mMap != null) {
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mMap.getCameraPosition().target, zoomLevel));
                }
            }
        });

        // 축소 버튼 클릭 리스너
        zoomOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                zoomLevel--; // 줌 레벨 감소
                if (mMap != null) {
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mMap.getCameraPosition().target, zoomLevel));
                }
            }
        });
    }

    private void checkLocationPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // 권한이 부여되지 않으면 요청
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            // 권한이 부여된 경우 마지막 위치 요청
            getLastLocation();
        }
    }

    private void getLastLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            double latitude = location.getLatitude();
                            double longitude = location.getLongitude();
                            String locationText = "현재 위치: " + latitude + ", " + longitude;
                            currentLocationText.setText(locationText);

                            if (mMap != null) {
                                mMap.setMyLocationEnabled(true);
                                // 초기 카메라 위치 및 줌 설정
                                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude), zoomLevel));
                            }
                        }
                    }
                });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLastLocation();
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    private DatabaseReference ambulanceRef;
    private Marker ambulanceMarker;

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        checkLocationPermission(); // 지도 준비가 완료되면 권한 확인

        // Firebase - 구급차 위치 리스너 설정
        ambulanceRef = FirebaseDatabase.getInstance().getReference("ambulance").child("ambulance_1");
        ambulanceRef.addValueEventListener(new com.google.firebase.database.ValueEventListener() {
            @Override
            public void onDataChange(@NonNull com.google.firebase.database.DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Double lat = snapshot.child("latitude").getValue(Double.class);
                    Double lng = snapshot.child("longitude").getValue(Double.class);
                    Boolean isDispatching = snapshot.child("isDispatching").getValue(Boolean.class);

                    if (lat != null && lng != null && isDispatching != null && isDispatching) {
                        LatLng ambulanceLocation = new LatLng(lat, lng);

                        if (ambulanceMarker == null) {
                            // 마커가 없으면 새로 생성
                            ambulanceMarker = mMap.addMarker(new com.google.android.gms.maps.model.MarkerOptions()
                                    .position(ambulanceLocation)
                                    .title("구급차 출동 중!")
                                    .snippet("긴급 차량이 이동 중입니다."));
                            Toast.makeText(LocationActivity.this, "구급차 출동 감지됨!", Toast.LENGTH_SHORT).show();
                        } else {
                            // 마커가 있으면 위치 이동
                            ambulanceMarker.setPosition(ambulanceLocation);
                        }
                    } else {
                        // 출동 중이 아니면 마커 제거
                        if (ambulanceMarker != null) {
                            ambulanceMarker.remove();
                            ambulanceMarker = null;
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull com.google.firebase.database.DatabaseError error) {
                // 에러 처리
            }
        });
    }
}

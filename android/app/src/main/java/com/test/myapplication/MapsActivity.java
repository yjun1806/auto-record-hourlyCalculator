package com.test.myapplication;


import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.util.Locale;


/*
* GPS 관련 참고 사이트
* http://ankyu.entersoft.kr/Lecture/android/gps_01.asp : 위치정보 얻는 방법에 대해 자세히 설명되어 있다.
*
*
* */

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    //구글맵을 사용하기 위한 순서
    /*
    * 1. 구글 맵 API를 받아와서 매니패스트에 추가
    * 2. Fragment를 액티비티에 추가해서 그 안에서 맵을 다룬다.
    * 3. OnMapReadyCallback 인터페이스를 상속받아서 onMapReady 매소드로 구글맵을 다룬다.
    * */

    private GoogleMap mMap; // 구글맵을 사용하기 위한 변수
    private Geocoder geocoder; // 주소값을 위도,경도로 변환하거나 위도.경도값을 주소로 변환해주는 기능을 가진 클래스
    private TextView gps_info;
    private ToggleButton gps_toggle;
    private double Send_latitude;
    private double Send_longitude;
    private LocationManager lm;
    ///////////
    private static final String TAG = "MapActivity";
    private FusedLocationProviderClient mFusedLocationProviderClient; // FusedLocationProviderClient는 현재 나의 위치를 알기위한 클래스
    private Boolean mLocationPermissionsGranted = false;

    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION; // 매니패스트의 GPS 관련 권한
    private static final String COURSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1234; // ?
    private static final float DEFAULT_ZOOM = 15f; // ?
////////////////

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(final GoogleMap googleMap) { // 지도를 사용할 준비(구글플레이 서비스)가 되면 호출되는 메소드.
        //Toast.makeText(this, "Map is Ready", Toast.LENGTH_SHORT).show();
        Log.d(TAG, "onMapReady: map is ready"); // 디버깅 로그
        /////////////////
        mMap = googleMap;
        UiSettings uiSettings = mMap.getUiSettings(); // maps 패키지 안의 UiSettings 객체. 구글맵 API에있다.
        // 구글맵의 사용자 인터페이스에 대한 설정을 할 수 있다. 이 인터페이스를 얻으려면 getUiSetting() 메소드를 호출하면 된다.
        uiSettings.setZoomControlsEnabled(true); // 확대/축소 컨트롤을 사용할 수 있도록 해준다.
        geocoder = new Geocoder(this, Locale.KOREA); // 지오코더 객체를 생성해준다. Locale.KOREA는 한글을 이용할때 사용
        /////////////////
       /* if (mLocationPermissionsGranted) { // 위치권한이 승인되어 있는 경우, 초기설정은 false 이므로 실행안됨.???
           // getDeviceLocation();

            *//*
            * 권한요청에 관련된 부분. LOCATION을 이용하려면 필요한 권한이 FINE과 COARSE 2개가 있다. 정확도의 차이, 2개다 넣어주는게 좋다.
            * 안드로이드 6.0이상부터는 사용자로부터 직접 권한을 허가받는 창을 띄우도록 하고있다.
            * *//*
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
           *//* mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(false);*//*
        }*/

        ////////////// 지도를 오랫동안 누르면 마커가 생긴다.
        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng point) { // 맵에 오랫동안 클릭을 하고있으면 이벤트를 발생시키는 부분
                MarkerOptions mOptions = new MarkerOptions(); //마커에 대한 속성을 설정하는 부분
                mOptions.title("마커 좌표"); // 마커를 클릭했을 때 보여주는 제목
                Double latitude = point.latitude; // 위도
                Double longitude = point.longitude; // 경도
                // 마커의 스니펫(간단한 텍스트) 설정, 타이틀 아래 보이는 내용? 부분
                mOptions.snippet(latitude.toString() + ", " + longitude.toString());
                // LatLng: 위도 경도 쌍을 나타냄, 마커의 위치를 위도,경도로 지정해준다.
                mOptions.position(new LatLng(latitude, longitude));
                // 마커(핀) 추가
                Send_latitude = latitude; // 보낼 위도, 경도값 - 이전 액티비티로 보낼 데이터를 변수에 담아둔다.
                Send_longitude = longitude;
                googleMap.clear(); // 기존의 맵에 마커가 남아있을 수 있으므로, 화면에 있는 마커를 지워준 후 다시 그려준다.
                googleMap.addMarker(mOptions); // 맵에 마커를 추가하는 부분.

            }
        });



        // 검색을 통해 위치를 얻는 부분
        /*search_button.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) { // 클릭했을시 어떤 동작을 할지 정하는 부분
                String str = search_edit.getText().toString(); // 텍스트에디터에 어떤 내용이 들어있는지 String 변수에 담아준다.
                List<Address> addressList = null; // Address 타입의 리스트를 하나 만든다.
                try {
                    // editText에 입력한 텍스트(주소, 지역, 장소 등)을 지오 코딩을 이용해 변환
                    addressList = geocoder.getFromLocationName(
                            str, // 주소
                            20); // 최대 검색 결과 개수
                } catch (IOException e) {
                    e.printStackTrace();
                }

                assert addressList != null;
                if(addressList.size() == 0){
                    Toast.makeText(getApplicationContext(), "검색 결과가 없습니다.", Toast.LENGTH_SHORT).show();
                }else {
                    System.out.println(addressList.get(0).toString()); // Logcat에 띄워주는 역할, 로그를 남겨준다.
                    // 콤마를 기준으로 split
                    String[] splitStr = addressList.get(0).toString().split(",");
                    String address = splitStr[0].substring(splitStr[0].indexOf("\"") + 1, splitStr[0].length() - 2); // 주소
                    System.out.println(address);

                    String latitude = splitStr[10].substring(splitStr[10].indexOf("=") + 1); // 위도
                    String longitude = splitStr[12].substring(splitStr[12].indexOf("=") + 1); // 경도
                    System.out.println(latitude);
                    System.out.println(longitude);

                    // 좌표(위도, 경도) 생성
                    LatLng point = new LatLng(Double.parseDouble(latitude), Double.parseDouble(longitude));
                    // 마커 생성
                    MarkerOptions mOptions2 = new MarkerOptions();
                    mOptions2.title("search result");
                    mOptions2.snippet(address);
                    mOptions2.snippet(latitude.toString() + ", " + longitude.toString());
                    mOptions2.position(point);
                    // 마커 추가
                    mMap.clear();
                    mMap.addMarker(mOptions2);
                    Send_latitude = Double.parseDouble(latitude); // 보낼 위도, 경도값
                    Send_longitude = Double.parseDouble(longitude);
                    // 해당 좌표로 화면 줌
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(point, 15));
                }
            }
        });*/
        ////////////////////

        // Add a marker in Sydney and move the camera
        /*LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().total_positon(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(13));*/
    }


    @SuppressLint("MissingPermission")
    @Override
    protected void onCreate(Bundle savedInstanceState) { // 액티비티가 생성될때
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        setTitle("장소 추가");

        gps_toggle = (ToggleButton) findViewById(R.id.toggleButton);
        gps_toggle.setText("GPS");

        lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE); // 위치관리자 객체 얻기

        PlaceAutocompleteFragment fragment = (PlaceAutocompleteFragment)getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);

        /*if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.

            getDeviceLocation();

            return;
        }
        lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);*/


       // List<String> list = lm.getAllProviders(); // 위치제공자 모두 가져오기

        getLocationPermission();

        //지도에서 장소를 선택했을때의 이벤트를 처리하는 부분
        fragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {

                LatLng point = new LatLng(place.getLatLng().latitude, place.getLatLng().longitude);
                String address = (String) place.getAddress();
                // 마커 생성
                MarkerOptions mOptions2 = new MarkerOptions();
                mOptions2.title(address);
                mOptions2.snippet(place.getLatLng().latitude + ", " + place.getLatLng().longitude);
                mOptions2.position(point);
                // 마커 추가
                mMap.clear();
                mMap.addMarker(mOptions2);
                Send_latitude = place.getLatLng().latitude; // 보낼 위도, 경도값
                Send_longitude = place.getLatLng().longitude;
                // 해당 좌표로 화면 줌
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(point, 15));
            }

            @Override
            public void onError(Status status) {

            }
        });


        //GPS 버튼을 누르면 현재 위치를 찾는다.
        gps_toggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try{
                    if(gps_toggle.isChecked()){
                        gps_toggle.setText("수신중");
                        Toast.makeText(getApplicationContext(), "현재 위치를 찾고 있습니다.", Toast.LENGTH_SHORT).show();

                        // GPS 제공자의 정보가 바뀌면 콜백하도록 리스너 등록하기~!!!
                        lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, // 등록할 위치제공자
                                100, // 통지사이의 최소 시간간격 (miliSecond)
                                1, // 통지사이의 최소 변경거리 (m)
                                mLocationListener);
                        lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, // 등록할 위치제공자
                                100, // 통지사이의 최소 시간간격 (miliSecond)
                                1, // 통지사이의 최소 변경거리 (m)
                                mLocationListener);
                        //getDeviceLocation();
                    }else{
                        gps_toggle.setText("미수신");
                        lm.removeUpdates(mLocationListener);  //  미수신할때는 반드시 자원해체를 해주어야 한다.
                    }
                }catch(SecurityException ignored){

                }
            }
        });


        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

    }

    //권한확인 부분
    private void getLocationPermission(){
        Log.d(TAG, "getLocationPermission: getting location permissions");
        String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};
        // 권한 중, 위치 접근 권한 2가지를 확인하기 위해 String에 넣는다.

        if(ContextCompat.checkSelfPermission(this.getApplicationContext(), FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            if(ContextCompat.checkSelfPermission(this.getApplicationContext(), COURSE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                mLocationPermissionsGranted = true;
                initMap();
            }else{
                ActivityCompat.requestPermissions(this, permissions, LOCATION_PERMISSION_REQUEST_CODE);
            }
        }else{
            ActivityCompat.requestPermissions(this, permissions, LOCATION_PERMISSION_REQUEST_CODE);
        }

    }

    //GPS 수신을 이용한 메소드
    private final LocationListener mLocationListener = new LocationListener() {
        public void onLocationChanged(Location location) {
            //여기서 위치값이 갱신되면 이벤트가 발생한다.
            //값은 Location 형태로 리턴되며 좌표 출력 방법은 다음과 같다.
            Log.d("test", "onLocationChanged, location:" + location);
            double longitude = location.getLongitude(); //경도
            double latitude = location.getLatitude();   //위도
            double altitude = location.getAltitude();   //고도
            float accuracy = location.getAccuracy();    //정확도
            String provider = location.getProvider();   //위치제공자
            gps_info = (TextView)findViewById(R.id.tv);
            //Gps 위치제공자에 의한 위치변화. 오차범위가 좁다.
            //Network 위치제공자에 의한 위치변화
            //Network 위치는 Gps에 비해 정확도가 많이 떨어진다.
            gps_info.setText("위치정보 : " + provider + "\n위도 : " + longitude + "\n경도 : " + latitude
                    + "\n고도 : " + altitude + "\n정확도 : "  + accuracy);


            ///마커생성부분
            LatLng point = new LatLng(latitude, longitude);
            // 마커 생성
            MarkerOptions mOptions2 = new MarkerOptions();
            mOptions2.title("search result");
            mOptions2.position(point);
            // 마커 추가
            mMap.clear();
            mMap.addMarker(mOptions2);
            Send_latitude = latitude; // 보낼 위도, 경도값
            Send_longitude = longitude;
            // 해당 좌표로 화면 줌
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(point,15));
        }
        public void onProviderDisabled(String provider) {
            // Disabled시
            Log.d("test", "onProviderDisabled, provider:" + provider);
        }

        public void onProviderEnabled(String provider) {
            // Enabled시
            Log.d("test", "onProviderEnabled, provider:" + provider);
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
            // 변경시
            Log.d("test", "onStatusChanged, provider:" + provider + ", status:" + status + " ,Bundle:" + extras);
        }
    };





    //취소누르면 발생하는 이벤트
    public void onClick_map_cancel(View v){
        lm.removeUpdates(mLocationListener); // 만약 GPS가 켜져있으면 자원이 계속소모되므로, 꺼준다.
        finish();
    }


    ///////////////////

    /*private void getDeviceLocation(){
        Log.d(TAG, "getDeviceLocation: getting the devices current location");

        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        try{
            if(mLocationPermissionsGranted){

                final Task location = mFusedLocationProviderClient.getLastLocation();
                location.addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if(task.isSuccessful()){
                            Log.d(TAG, "onComplete: found location!");
                            Location currentLocation = (Location) task.getResult();

                            LatLng point = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
                            MarkerOptions mOptions2 = new MarkerOptions();
                            mOptions2.title("현재 위치");
                            mOptions2.position(point);
                            // 마커 추가
                            mMap.clear();
                            mMap.addMarker(mOptions2);
                            Send_latitude = currentLocation.getLatitude(); // 보낼 위도, 경도값
                            Send_longitude = currentLocation.getLongitude();

                            moveCamera(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()),
                                    DEFAULT_ZOOM);

                        }else{
                            Log.d(TAG, "onComplete: current location is null");
                            Toast.makeText(MapsActivity.this, "unable to get current location", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        }catch (SecurityException e){
            Log.e(TAG, "getDeviceLocation: SecurityException: " + e.getMessage() );
        }
    }*/

    /*private void moveCamera(LatLng latLng, float zoom){
        Log.d(TAG, "moveCamera: moving the camera to: lat: " + latLng.latitude + ", lng: " + latLng.longitude );
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));
    }*/

    private void initMap(){
        Log.d(TAG, "initMap: initializing map");
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);

        mapFragment.getMapAsync(MapsActivity.this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.d(TAG, "onRequestPermissionsResult: called.");
        mLocationPermissionsGranted = false;

        switch(requestCode){
            case LOCATION_PERMISSION_REQUEST_CODE:{
                if(grantResults.length > 0){
                    for(int i = 0; i < grantResults.length; i++){
                        if(grantResults[i] != PackageManager.PERMISSION_GRANTED){
                            mLocationPermissionsGranted = false;
                            Log.d(TAG, "onRequestPermissionsResult: permission failed");
                            return;
                        }
                    }
                    Log.d(TAG, "onRequestPermissionsResult: permission granted");
                    mLocationPermissionsGranted = true;
                    //initialize our map
                    initMap();
                }
            }
        }
    }

    //저장 버튼을 눌렀을때 발생하는 이벤트, 위도, 경도값을 돌려준다.
    public void onClick_Save(View v){
        Intent intent = getIntent();

        intent.putExtra("latitude", Send_latitude);
        intent.putExtra("longitude", Send_longitude);
        setResult(RESULT_OK, intent);
        lm.removeUpdates(mLocationListener); // 만약 GPS가 켜져있으면 자원이 계속소모되므로, 꺼준다.
        finish();
    }

    @Override // 화면 회전시 데이터가 사라지지 않도록 유지해준다.
    public void onConfigurationChanged(Configuration newConfig) {
        // TODO Auto-generated method stub
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onBackPressed() { // 뒤로가기 버튼을 눌렀을때 GPS가 수신중이라면  꺼주는 부분
        super.onBackPressed();
        lm.removeUpdates(mLocationListener);  //  미수신할때는 반드시 자원해체를 해주어야 한다.

    }
}

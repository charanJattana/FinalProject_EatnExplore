package com.example.guri.eatnexplorerestaurantfinder;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.LocationListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class HomeScreenMainActivity extends FragmentActivity implements OnMapReadyCallback,
        RegisterFragment.OnFragmentInteractionListener, LoginFragment.OnFragmentInteractionListener,
        GoogleApiClient.ConnectionCallbacks,GoogleApiClient.OnConnectionFailedListener,LocationListener
{

    private GoogleMap mMap;
    boolean isRegisterFragmentDisplayed = false;
    boolean isLoginFragmentDisplayed = false;
    private Button btnRegFragment, btnLoginFragment;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    String searchTag = "", location="";
    private EditText etSearchTag, etSearchLocation;
    Button btnSearchLoc;
    //
    private GoogleApiClient client;
    private LocationRequest locationRequest;
    private Location lastLocation;
    private Marker currentLocationMarker;
    private double latitude,longitude;
    int PROXIMITY_RADIUS=1500;
    public static final int REQUEST_LOCATION_CODE=99;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_screen_main);
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M)
        {
            checkLocationPermission();
        }
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        //initSharedPreferences();
        mapFragment.getMapAsync(this);

        btnRegFragment = (Button) findViewById(R.id.register);
        btnLoginFragment = (Button) findViewById(R.id.login);


        btnRegFragment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                displayRegisterFragment();
            }
        });

        btnLoginFragment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                displayLoginFragment();
            }
        });

        etSearchTag = (EditText) findViewById(R.id.searchBox);
        etSearchLocation = (EditText) findViewById(R.id.searchLoc);

        btnSearchLoc = (Button) findViewById(R.id.btnSearch);

        btnSearchLoc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                searchTag = etSearchTag.getText().toString();
                location = etSearchLocation.getText().toString();
                initSharedPreferences();
            }
        });


        // put search tag in searchTag and location in  location
        // and call the below commented method

    }


    public void displayRegisterFragment(){
        RegisterFragment fragment = RegisterFragment.newInstance();
        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.add(R.id.fragment_register_container, fragment).addToBackStack(null).commit();
        isRegisterFragmentDisplayed = true;
    }

    public void displayLoginFragment(){
        LoginFragment fragment = LoginFragment.newInstance();
        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.add(R.id.fragment_login_container, fragment).addToBackStack(null).commit();
        isLoginFragmentDisplayed = true;
    }


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
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
        {

            buildGoogleApiClient();
            mMap.setMyLocationEnabled(true);
        }

    }

    @Override
    public void registerUser() {

    }

    @Override
    public void onLoginFragmentInteraction(String name) {

    }


    public void initSharedPreferences()
    {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(HomeScreenMainActivity.this);
        editor = sharedPreferences.edit();
       editor.putString("searchTag",searchTag);
        editor.putString("location",location);
        editor.commit();

        listRestaurants();
    }

    public void listRestaurants()
    { //finish();
        startActivity(new Intent(this, ListRestaurants.class));

    }

    //charan methods for map


    public void nearByRest()
    {
        Object dataTransfer[]=new Object[2];
        GetNearByRestaurants getNearByRestaurants=new GetNearByRestaurants();
        mMap.clear();
        String restaurant="restaurant";
        String url=getUrl(latitude,longitude,restaurant);
        dataTransfer[0]=mMap;
        dataTransfer[1]=url;
        getNearByRestaurants.execute(dataTransfer);
        Toast.makeText(this, "Showing near by resturants", Toast.LENGTH_SHORT).show();
    }

    public String getUrl(double latitude,double longitude,String nearbyPlace)
    {

        StringBuilder googlePlaceUrl=new StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json?");
        googlePlaceUrl.append("location="+latitude+","+longitude);
        googlePlaceUrl.append("&radius="+PROXIMITY_RADIUS);
        googlePlaceUrl.append("&type="+nearbyPlace);
        googlePlaceUrl.append("&key=AIzaSyA9D-os0WYawFG93IGMkbiTASstA2fCfjo");
        return googlePlaceUrl.toString();

    }
    protected synchronized void buildGoogleApiClient() {
        client = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        client.connect();
    }

    @Override
    public void onLocationChanged(Location location) {
        latitude=location.getLatitude();
        longitude=location.getLongitude();
        lastLocation=location;
        if(currentLocationMarker!=null)
        {
            currentLocationMarker.remove();
        }
        LatLng latLng=new LatLng(location.getLatitude(),location.getLongitude());
        MarkerOptions markerOptions=new MarkerOptions();
        markerOptions.position(latLng);
        markerOptions.title("Current Location");
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));

        currentLocationMarker=mMap.addMarker(markerOptions);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(10));
        if(client!=null)
        {
            LocationServices.FusedLocationApi.removeLocationUpdates(client,this) ;
        }
        nearByRest();


    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        locationRequest = new LocationRequest();
        locationRequest.setInterval(1000);
        locationRequest.setFastestInterval(1000);
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED )
        {
            LocationServices.FusedLocationApi.requestLocationUpdates(client, locationRequest, this);

        }

    }
    public boolean checkLocationPermission()
    {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED )
        {

            if(ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.ACCESS_FINE_LOCATION))
            {

                ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},REQUEST_LOCATION_CODE);
            }else{
                ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},REQUEST_LOCATION_CODE);
            }
            return false;

        }
        return true;
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_LOCATION_CODE:
                if (grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED) {
                    if(ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION)==PackageManager.PERMISSION_GRANTED)
                    {
                        if(client==null)
                        {
                            buildGoogleApiClient();
                        }
                        mMap.setMyLocationEnabled(true);
                    }

                }
                else {
                    Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
                }
        }
    }


    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}

package com.example.guri.eatnexplorerestaurantfinder;

import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

public class GetNearByRestaurants extends AsyncTask<Object,String,String> {

    private String googleRestaurantsData;
    private GoogleMap mMap;
    private String url;
    private MarkerOptions markerOptions;
    private LatLng latLng;

    @Override
    protected String doInBackground(Object... objects) {

        mMap=(GoogleMap)objects[0];
        url=(String)objects[1];

        DownloadUrl downloadUrl=new DownloadUrl();

        try {
            googleRestaurantsData=downloadUrl.readUrl(url);

        } catch (IOException e) {
            e.printStackTrace();
        }
        return googleRestaurantsData;
    }

    @Override
    protected void onPostExecute(String s) {
        List<HashMap<String,String>> nearbyPlaceList;
        DataParser dataParser=new DataParser();
        nearbyPlaceList=dataParser.parse(s);
        Log.d("onPostExecute", ": called parse method"+s);
        showNearbyPlaces(nearbyPlaceList);
    }

    private void showNearbyPlaces(List<HashMap<String,String>> nearbyPlaceList)
    {
        for (int i=0;i<nearbyPlaceList.size();++i)
        {
            markerOptions=new MarkerOptions();
            HashMap<String,String> googlePlace=nearbyPlaceList.get(i);

            String placeName=googlePlace.get("name");
            String vicinity=googlePlace.get("vicinity");
            double lat=Double.parseDouble(googlePlace.get("lat"));
            double lng=Double.parseDouble(googlePlace.get("lng"));

            latLng=new LatLng(lat,lng);

            markerOptions.position(latLng);
            markerOptions.title(placeName+" : "+vicinity);
            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));

            mMap.addMarker(markerOptions);
            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
            mMap.animateCamera(CameraUpdateFactory.zoomTo(10));


            mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
                @Override
                public void onInfoWindowClick(Marker marker) {
                    markerOptions.position(marker.getPosition());
                    markerOptions.title(marker.getTitle());
                    markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));

                    mMap.addMarker(markerOptions);
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(marker.getPosition(),15));
                }
            });




        }
    }
}

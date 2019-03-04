package com.example.guri.eatnexplorerestaurantfinder;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DataParser {

    private HashMap<String,String> getPlace(JSONObject jsonObject)
    {
        HashMap<String,String> googleRestaurantMap=new HashMap<>();
        String placeName="-NA-";
        String vicinity="-NA-";
        String latitude="";
        String longitude="";
        String reference="";

            try {
                if(!jsonObject.isNull("name")) {
                    placeName = jsonObject.getString("name");
                }
                if(!jsonObject.isNull("vicinity"))
                {
                    vicinity=jsonObject.getString("vicinity");
                }
                latitude=jsonObject.getJSONObject("geometry").getJSONObject("location").getString("lat");
                longitude=jsonObject.getJSONObject("geometry").getJSONObject("location").getString("lng");
                reference=jsonObject.getString("reference");
                googleRestaurantMap.put("name",placeName);
                googleRestaurantMap.put("vicinity",vicinity);
                googleRestaurantMap.put("lat",latitude);
                googleRestaurantMap.put("lng",longitude);
                googleRestaurantMap.put("reference",reference);

            } catch (JSONException e) {
                e.printStackTrace();
            }

        return googleRestaurantMap;
    }


    private List<HashMap<String,String>> getPlaces(JSONArray jsonArray)
    {
        int count =jsonArray.length();
       List<HashMap<String,String>> restaurantList=new ArrayList<>();
       HashMap<String,String> restaurantMap=null;

       for(int i=0;i<count;++i)
       {
           try {
               restaurantMap=getPlace((JSONObject)jsonArray.get(i));
               restaurantList.add(restaurantMap);
           } catch (JSONException e) {
               e.printStackTrace();
           }
       }
       return restaurantList;
    }

    public List<HashMap<String,String>> parse(String jsonData)
    {
        JSONObject jsonObject;
        JSONArray jsonArray=null;
        Log.d("DataParse..", "parse: "+jsonData);

        try {
            jsonObject=new JSONObject(jsonData);
            jsonArray=jsonObject.getJSONArray("results");

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return getPlaces(jsonArray);
    }
}

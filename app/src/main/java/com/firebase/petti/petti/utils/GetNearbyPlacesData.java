package com.firebase.petti.petti.utils;

import android.os.AsyncTask;
import android.util.Log;

import com.firebase.petti.petti.utils.DataParser;
import com.firebase.petti.petti.utils.DownloadUrl;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.HashMap;
import java.util.List;

import static android.R.attr.type;

/**
 * This class represents an asynchronous task which responsible for getting a specific type of
 * places, get their information by the downloadURL utility, parsing the response and finally
 * creating the corresponding markers on top of the underlying map. It does all that in asynchronous
 * manner - without blocking main thread.
 */

public class GetNearbyPlacesData extends AsyncTask<Object, String, String> {

    private String googlePlacesData;
    private GoogleMap mMap;

    private String placeType;

    @Override
    protected String doInBackground(Object... params) {
        try {
            String url;
            mMap = (GoogleMap) params[0];
            url = (String) params[1];

            this.placeType = findType(url);

            DownloadUrl downloadUrl = new DownloadUrl();
            googlePlacesData = downloadUrl.readUrl(url);
            Log.d("!@#$%", googlePlacesData);
        } catch (Exception e) {
            Log.d("GooglePlacesReadTask", e.toString());
        }
        return googlePlacesData;
    }

    @Override
    protected void onPostExecute(String result) {
        List<HashMap<String, String>> nearbyPlacesList;
        DataParser dataParser = new DataParser();
        nearbyPlacesList =  dataParser.parse(result);
        Log.d("!@#$%", nearbyPlacesList.toString());
        ShowNearbyPlaces(nearbyPlacesList);
    }

    private void ShowNearbyPlaces(List<HashMap<String, String>> nearbyPlacesList) {
        Log.d("onPostExecute",String.format("********* type = %s size: %d", this.placeType, nearbyPlacesList.size()));
        for (int i = 0; i < nearbyPlacesList.size(); i++) {
            Log.d("!@#$%", "i: " + i);
            MarkerOptions markerOptions = new MarkerOptions();
            HashMap<String, String> googlePlace = nearbyPlacesList.get(i);
            double lat = Double.parseDouble(googlePlace.get("lat"));
            double lng = Double.parseDouble(googlePlace.get("lng"));
            Log.d("!@#$%", "lat: " + lat + "lng: " + lng);
            String placeName = googlePlace.get("place_name");
            String vicinity = googlePlace.get("vicinity");
            LatLng latLng = new LatLng(lat, lng);
            markerOptions.position(latLng);
            markerOptions.title(placeName + " : " + vicinity);
            switch(this.placeType){
                case "pet_store": markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
                    break;
                case "veterinary_care": markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
                    break;
                case "park": markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                    break;
                default: markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW));
                    break;
            }

            mMap.addMarker(markerOptions);
            //move map camera
//            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
//            mMap.animateCamera(CameraUpdateFactory.zoomTo(13));
        }
    }

    private String findType(String url){
        String[] params =url.split("&");
        for (String param : params){
            if (param.contains("type"))
                return param.split("=")[1];
        }
        return null;
    }
}

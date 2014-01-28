package com.example.stepbystep;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MainActivity extends FragmentActivity {
	GoogleMap googleMap;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		try {
			// Loading map
			initilizeMap();
		} catch (Exception e) {
			e.printStackTrace();
			}
		}
			private void initilizeMap() {
				if (googleMap == null) {
					FragmentManager myFragmentManager = getSupportFragmentManager();
					SupportMapFragment mySupportMapFragment = (SupportMapFragment) myFragmentManager
							.findFragmentById(R.id.map);
					googleMap = mySupportMapFragment.getMap();
					// latitude and longitude
					double latitude = 40.932481;
					double longitude =29.163663 ;
					 
					// create marker
					MarkerOptions marker = new MarkerOptions().position(new LatLng(latitude, longitude)).title("Hello Maps ");
					 
					// adding marker
					googleMap.addMarker(marker);

					
					// check if map is created successfully or not
			if (googleMap == null) {
			Toast.makeText(getApplicationContext(),
			"Sorry! unable to create maps", Toast.LENGTH_SHORT).show();
			}
				}
			}}

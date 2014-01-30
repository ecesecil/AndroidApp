package com.example.stepbystep;

import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MainActivity extends FragmentActivity implements LocationListener {
	GoogleMap googleMap;
	LocationManager locationManager;
	LocationListener locationListener;
	
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
		
		FragmentManager myFragmentManager = getSupportFragmentManager();
		SupportMapFragment mySupportMapFragment = (SupportMapFragment) myFragmentManager
				.findFragmentById(R.id.map);
		googleMap = mySupportMapFragment.getMap();

		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		Criteria criteria = new Criteria();
		final String provider = locationManager.getBestProvider(criteria, true);
		final Location location = locationManager.getLastKnownLocation(provider);
		locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1,
				10, this);
		final Button startButton =(Button)findViewById(R.id.startButton);
		final Button stopButton=(Button)findViewById(R.id.stopButton);
		
		startButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				Location currentLocation=locationManager.getLastKnownLocation(provider);
				double currentLatitude=currentLocation.getLatitude();
				double currentLongitude=currentLocation.getLongitude();
				
				googleMap.addMarker(new MarkerOptions()
		        .position(new LatLng(currentLatitude,currentLongitude))
		        .title("Hello world"));
				startButton.setVisibility(View.INVISIBLE);
				stopButton.setVisibility(View.VISIBLE);
			}
		});
	
		
		stopButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				double latitude=location.getLatitude()+0.00001;
				double longitude=location.getLongitude()+0.00001;
				googleMap.addMarker(new MarkerOptions()
		        .position(new LatLng(latitude,longitude))
		        .title("Hop burday�m"));
				//stopButton.setVisibility(View.GONE);
				//stopButton.setVisibility(View.VISIBLE);
				
			}
		});
	}
	@Override
	public void onLocationChanged(Location arg0) {
		googleMap.setMyLocationEnabled(true);
		googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(
				arg0.getLatitude(), arg0.getLongitude()), 15));
		 arg0.getLatitude();
	}

	@Override
	public void onProviderDisabled(String arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onProviderEnabled(String arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
		// TODO Auto-generated method stub
		System.out.println("STATUS CHANGED");

	}

	
}

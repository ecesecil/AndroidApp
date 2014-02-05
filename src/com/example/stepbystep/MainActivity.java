package com.example.stepbystep;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import android.content.Context;
import android.graphics.Color;
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
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

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

		// GoogleMap

		FragmentManager myFragmentManager = getSupportFragmentManager();
		SupportMapFragment mySupportMapFragment = (SupportMapFragment) myFragmentManager
				.findFragmentById(R.id.map);
		googleMap = mySupportMapFragment.getMap();
		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		Criteria criteria = new Criteria();
		final String provider = locationManager.getBestProvider(criteria, true);
		final Location location = locationManager
				.getLastKnownLocation(provider);
		locationManager.requestLocationUpdates(
				LocationManager.NETWORK_PROVIDER, 1, 10, this);

		// Butonlar
		final Button startButton = (Button) findViewById(R.id.startButton);
		final Button stopButton = (Button) findViewById(R.id.stopButton);

		// Path
		final List<LatLng> list = new ArrayList<LatLng>();
		final List<LatLng> deneme = new ArrayList<LatLng>();
		// deneme.

		startButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {

				Location currentLocation = locationManager
						.getLastKnownLocation(provider);
				double firstLatitude = currentLocation.getLatitude();
				double firstLongitude = currentLocation.getLongitude();

				LatLng startLatLng = new LatLng(firstLatitude, firstLongitude);
				list.add(startLatLng);
				list.add(new LatLng(40.91179370632012, 29.1926908493042));
				list.add(new LatLng(40.9153286918986, 29.181768894195557));
				list.add(new LatLng(40.91185857014307, 29.181050062179565));
				list.add(new LatLng(40.909641002096116, 29.18193519115448));
				// list.set(0, startLatLng);

				googleMap.addMarker(new MarkerOptions().position(
						new LatLng(firstLatitude, firstLongitude)).title("A"));

				startButton.setVisibility(View.INVISIBLE);
				stopButton.setVisibility(View.VISIBLE);

			}
		});

		stopButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				Location currentLocation = locationManager
						.getLastKnownLocation(provider);
				double lastLatitude = currentLocation.getLatitude();
				double lastLongitude = currentLocation.getLongitude();
				LatLng endLatLng= list.get(list.size()-1);
				//LatLng endLatLng = new LatLng(lastLatitude, lastLongitude);
				//list.add(endLatLng);
				googleMap.addMarker(new MarkerOptions().position(endLatLng).title("B"));
				stopButton.setVisibility(View.INVISIBLE);

			
				Polyline line = googleMap.addPolyline(new PolylineOptions().width(5)
						.color(Color.BLUE).geodesic(true));

				for (int z = 1; z < list.size(); z++) {
					line.setPoints(list.subList(0, z+1));
				}

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

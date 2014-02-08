package com.example.stepbystep;

import java.util.List;

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
import android.widget.TextView;

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
	boolean running = false;
	long startTime = 0;

	Polyline line;

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
		googleMap.setMyLocationEnabled(true);

		line = googleMap.addPolyline(new PolylineOptions().width(5)
				.color(Color.BLUE).geodesic(true));

		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		Criteria criteria = new Criteria();
		final String provider = locationManager.getBestProvider(criteria, true);

		locationManager.requestLocationUpdates(
				LocationManager.NETWORK_PROVIDER, 0, 0, this);
		locationManager.requestLocationUpdates(
				LocationManager.GPS_PROVIDER, 0, 0, this);
		// Butonlar
		final Button startButton = (Button) findViewById(R.id.startButton);
		final Button stopButton = (Button) findViewById(R.id.stopButton);

		// TextView bir de�i�kene atand�.
		final TextView infoBox = (TextView) findViewById(R.id.infoBox);

		startButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {

				running = true;
				infoBox.setText("Konum Bilgileri Al�n�yor..");

				// starttime '� toplam s�re hesab�n� bulurken kullanaca��z.
				startTime = System.currentTimeMillis();

				// son bilinen lokasyon de�eri marker ekleme ve polyline �izmek
				// i�in kullanaca��z.
				Location currentLocation = locationManager
						.getLastKnownLocation(provider);
				double firstLatitude = currentLocation.getLatitude();
				double firstLongitude = currentLocation.getLongitude();

				// Ba�lang�� noktas�
				LatLng startLatLng = new LatLng(firstLatitude, firstLongitude);

				// ba�lang�� noktas�n� haritada g�sterip marker ekliyoruz.
				googleMap.addMarker(new MarkerOptions().position(
						new LatLng(firstLatitude, firstLongitude)).title("A"));

				// ba�la butonuna t�klan�nca bitir butonun g�r�n�r olmas�n�
				// sa�l�yoruz.
				startButton.setVisibility(View.INVISIBLE);
				stopButton.setVisibility(View.VISIBLE);

				// enlem boylam bilgisini polyline'a ekliyoruz.
				List<LatLng> points = line.getPoints();
				points.add(startLatLng);
				line.setPoints(points);

			}
		});

		stopButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				running = false;

				// son bilinen lokasyon de�eri marker ekleme ve polyline �izmek
				// i�in kullanaca��z.
				Location currentLocation = locationManager
						.getLastKnownLocation(provider);
				double lastLatitude = currentLocation.getLatitude();
				double lastLongitude = currentLocation.getLongitude();
				LatLng endLatLng = new LatLng(lastLatitude, lastLongitude);

				// bitir butonuna marker ekliyoruz.
				googleMap.addMarker(new MarkerOptions().position(endLatLng)
						.title("B"));

				// bitir butonunu ekrandan kald�r�yoruz.
				stopButton.setVisibility(View.INVISIBLE);

				// enlem boylam de�erlerini polyline'a ekliyoruz.
				List<LatLng> points = line.getPoints();
				points.add(endLatLng);
				line.setPoints(points);

				// Toplam s�renin yazd�r�lmas�.
				long finishTime = System.currentTimeMillis();
				long diff = (finishTime - startTime) / 1000;

				String totalTime = diff > 60 ? Math.floor(diff / 60) + "dk" : diff + "sn";
				String totalDistance = getDistance(line);

				infoBox.setText("Toplam S�re: " + totalTime
						+ "\n Toplam Mesafe: " + totalDistance);

			}
		});
	}

	@Override
	public void onLocationChanged(Location arg0) {

		LatLng currentLatLng = new LatLng(arg0.getLatitude(),
				arg0.getLongitude());

		googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
				currentLatLng, 15));

		if (running) {

			List<LatLng> points = line.getPoints();
			points.add(currentLatLng);
			line.setPoints(points);

			final TextView infoBox = (TextView) findViewById(R.id.infoBox);
			String currentSpeed;
			if(arg0.getSpeed()>0){
				 currentSpeed=String.valueOf(arg0.getSpeed());
			}else{
				currentSpeed="N/A";
			}
			infoBox.setText("Anl�k H�z :" + currentSpeed
					+ "\n\n Toplam Mesafe:" + getDistance(line));
		}

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

	public String getDistance(Polyline polyline) {
		double length = com.google.maps.android.SphericalUtil
				.computeLength(polyline.getPoints());

		if (length < 1000) {
			return  Math.ceil(length) + "m";
		} else {
			return Math.ceil(length / 1000) + "km";
		}
	}
}

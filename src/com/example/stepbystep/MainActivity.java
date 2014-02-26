package com.example.stepbystep;

import java.sql.Date;
import java.util.List;
import java.util.UUID;

import android.content.Context;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.apigee.sdk.ApigeeClient;
import com.apigee.sdk.data.client.DataClient;
import com.apigee.sdk.data.client.entities.Entity;
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

	DataClient dataclient;

	Polyline line;
	// uniqe id tanmlanmasý
	UUID tripId;

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

		// apigee

		String ORGNAME = "ecesecil";
		String APPNAME = "sandbox";

		ApigeeClient apigeeclient = new ApigeeClient(ORGNAME, APPNAME,
				this.getBaseContext());
		this.dataclient = apigeeclient.getDataClient();
		this.dataclient.setClientId("b3U6fVdBBYHaEeKN3ALoGuZA3A");
		this.dataclient.setClientSecret("b3U6nJ4qCITa7zK42AArUqRUCihr6Bc");

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
		locationManager.requestLocationUpdates(
				LocationManager.NETWORK_PROVIDER, 0, 0, this);
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0,
				0, this);
		final String provider = locationManager.getBestProvider(criteria, true);

		// Butonlar
		final Button startButton = (Button) findViewById(R.id.startButton);
		final Button stopButton = (Button) findViewById(R.id.stopButton);

		// TextView bir deðiþkene atandý.
		final TextView infoBox = (TextView) findViewById(R.id.infoBox);

		startButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {

				running = true;
				infoBox.setText("Konum Bilgileri Alýnýyor..");

				// id ye deðer atama iþlemi için
				tripId = UUID.randomUUID();

				// starttime 'ý toplam süre hesabýný bulurken kullanacaðýz.
				startTime = System.currentTimeMillis();

				// son bilinen lokasyon deðeri marker ekleme ve polyline çizmek
				// için kullanacaðýz.
				Location currentLocation = locationManager
						.getLastKnownLocation(provider);
				double firstLatitude = currentLocation.getLatitude();
				double firstLongitude = currentLocation.getLongitude();

				// Baþlangýç noktasý
				LatLng startLatLng = new LatLng(firstLatitude, firstLongitude);

				// baþlangýç noktasýný haritada gösterip marker ekliyoruz.
				googleMap.addMarker(new MarkerOptions().position(
						new LatLng(firstLatitude, firstLongitude)).title("A"));

				// enlem boylam bilgisini polyline'a ekliyoruz.
				List<LatLng> points = line.getPoints();
				points.add(startLatLng);
				line.setPoints(points);

				// sent to apigee
				sentApigee(currentLocation);

				// baþla butonuna týklanýnca bitir butonun görünür olmasýný
				// saðlýyoruz.
				startButton.setVisibility(View.INVISIBLE);
				stopButton.setVisibility(View.VISIBLE);

			}
		});

		stopButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				running = false;

				// son bilinen lokasyon deðeri marker ekleme ve polyline çizmek
				// için kullanacaðýz.
				Location currentLocation = locationManager
						.getLastKnownLocation(provider);
				double lastLatitude = currentLocation.getLatitude();
				double lastLongitude = currentLocation.getLongitude();
				LatLng endLatLng = new LatLng(lastLatitude, lastLongitude);

				// bitir butonuna marker ekliyoruz.
				googleMap.addMarker(new MarkerOptions().position(endLatLng)
						.title("B"));

				// bitir butonunu ekrandan kaldýrýyoruz.
				stopButton.setVisibility(View.INVISIBLE);

				// enlem boylam deðerlerini polyline'a ekliyoruz.
				List<LatLng> points = line.getPoints();
				points.add(endLatLng);
				line.setPoints(points);
				
				sentApigee(currentLocation);
				
			

				// Toplam sürenin yazdýrýlmasý.
				long finishTime = System.currentTimeMillis();
				long diff = (finishTime - startTime) / 1000;

				String totalTime = diff > 59 ? Math.ceil(diff / 60) + "dk"
						: diff + "sn";
				double totalDistance = getDistance(line);
				int distance = (int) totalDistance;

				if (distance < 1000) {// toplam mesafe double to ing metre
					infoBox.setText("Toplam Süre: " + totalTime
							+ "\n Toplam Mesafe: " + distance + "m");
				} else {
					infoBox.setText("Toplam Süre: " + totalTime
							+ "\n Toplam Mesafe: " + distance + "km");
				}

			}
		});

	}

	public void sentApigee(Location location) {
		System.out.println(location.toString());
		Entity loc = new Entity();
		loc.setType("sbs");
		loc.setDataClient(this.dataclient);

		loc.setProperty("lat", (float) location.getLatitude());
		loc.setProperty("lng", (float) location.getLongitude());
		loc.setProperty("speed", location.getSpeed());
		loc.setUuid(tripId);
		loc.setProperty("date", location.getTime());
		EntitiyPoster poster = new EntitiyPoster();
		poster.execute(loc);
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
			sentApigee(arg0);

			final TextView infoBox = (TextView) findViewById(R.id.infoBox);
			String currentSpeed;
			if (arg0.getSpeed() > 0) {
				currentSpeed = String.valueOf(arg0.getSpeed());
			} else {
				currentSpeed = "N/A";
			}
			infoBox.setText("Anlýk Hýz :" + currentSpeed
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

	public double getDistance(Polyline polyline) {
		double length = com.google.maps.android.SphericalUtil
				.computeLength(polyline.getPoints());

		if (length < 1000) {
			return Math.ceil(length);
		} else {
			return Math.ceil(length / 1000);
		}
	}

	private class EntitiyPoster extends AsyncTask<Entity, Void, Void> {

		@Override
		protected Void doInBackground(Entity... params) {
			Entity entity = params[0];
			Log.i("location denemesi", params[0].toString());
			entity.save();
			return null;
		}

	}

	
}

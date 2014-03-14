package com.example.stepbystep;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.UUID;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;

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
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.apigee.sdk.ApigeeClient;
import com.apigee.sdk.data.client.DataClient;
import com.apigee.sdk.data.client.callbacks.ApiResponseCallback;
import com.apigee.sdk.data.client.entities.Entity;
import com.apigee.sdk.data.client.response.ApiResponse;
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
	// uniqe id tanmlanmasý
	static UUID tripId;

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
				getNearestLocation(currentLocation);

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
		new HttpAsyncTask().execute(location);
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

	private static String convertInputStreamToString(InputStream inputStream)
			throws IOException {
		BufferedReader bufferedReader = new BufferedReader(
				new InputStreamReader(inputStream));
		String line = "";
		String result = "";
		while ((line = bufferedReader.readLine()) != null)
			result += line;

		inputStream.close();
		return result;

	}

	private class HttpAsyncTask extends AsyncTask<Location, Void, Void> {
		@Override
		protected Void doInBackground(Location... params) {
			Location location = params[0];
			InputStream inputStream = null;
			String result = "";
			try {
				// 1. create HttpClient
				HttpClient httpclient = new DefaultHttpClient();

				// 2. make POST request to the given URL
				HttpPost httpPost = new HttpPost(
						"https://api.usergrid.com/ecesecil/sandbox/anils");

				String json = "";

				// 3. build jsonObject
				JSONObject jsonObject = new JSONObject();
				jsonObject.accumulate("lat", location.getLatitude());
				jsonObject.accumulate("lng", location.getLongitude());
				jsonObject.accumulate("speed", location.getSpeed());
				jsonObject.accumulate("tripId", tripId);

				// 4. convert JSONObject to JSON to String
				json = jsonObject.toString();

				// 5. set json to StringEntity
				StringEntity se = new StringEntity(json);

				// 6. set httpPost Entity
				httpPost.setEntity(se);

				// 7. Set some headers to inform server about the type of the
				// content
				httpPost.setHeader("Accept", "application/json");
				httpPost.setHeader("Content-type", "application/json");

				// 8. Execute POST request to the given URL
				HttpResponse httpResponse = httpclient.execute(httpPost);

				inputStream = httpResponse.getEntity().getContent();

				// 10. convert inputstream to string
				if (inputStream != null)
					result = convertInputStreamToString(inputStream);
				else
					result = "Did not work!";

			} catch (Exception e) {

				Log.e("hata:", e.getClass().toString());
			}

			return null;
		}
	}

	public void getNearestLocation(Location location) {
		// Create client entity
		String ORGNAME = "ecesecil";
		String APPNAME = "sandbox";
		ApigeeClient apigeeClient = new ApigeeClient(ORGNAME, APPNAME,
				this.getBaseContext());
		DataClient dataClient = apigeeClient.getDataClient();
		
		
		// Gridlerin ortanoktalarýnýn listesi sql stringdeki sorgu bunlara göre yapýlacak
		List<LatLng> gridMidPoints= new ArrayList<LatLng>();
		gridMidPoints.add(new LatLng(40.97422138080048,29.0997175395353));
		gridMidPoints.add(new LatLng(40.973395142841085,29.100200906395912));
		gridMidPoints.add(new LatLng(40.97257699505776,29.100662463647463));
		gridMidPoints.add(new LatLng(40.97171833399712,29.101102128624916));
		gridMidPoints.add(new LatLng(40.9709001654198,29.101606383919716));
		gridMidPoints.add(new LatLng(40.970041482535926,29.102078452706337));
		gridMidPoints.add(new LatLng(40.96919899035504,29.10251833498478));
		gridMidPoints.add(new LatLng(40.96837268949781,29.10303331911564));
		gridMidPoints.add(new LatLng(40.96753966953534,29.103535562753677));
		gridMidPoints.add(new LatLng(40.966719360540196,29.104050882160664));
		gridMidPoints.add(new LatLng(40.9659254339995,29.10461414605379));
		gridMidPoints.add(new LatLng(40.96513554862575,29.105198867619038));
		
		
		

		// specify the entity collection to query
		String type = "stepbystep";
		
		//gridlerin baþ/orta/son noktalarý dizide tutulacak.
		
	
		// specify a valid query string
		double lat=gridMidPoints.get(0).latitude;
		double lng=gridMidPoints.get(0).longitude;
		String query = "select * where location within 50 of lat,lng";

		// call getEntitiesAsync to initiate the asynchronous API call
		dataClient.getEntitiesAsync(type, query, new ApiResponseCallback() {

			// If getEntitiesAsync fails, catch the error
			@Override
			public void onException(Exception e) {
				// Error
			}

			// If getEntitiesAsync is successful, handle the response object
			@Override
			public void onResponse(ApiResponse response) {
				try {
					if (response != null) {
						List<Entity> entityList = response.getEntities();
						if (entityList != null) {
							JSONObject jsonObject;
							double c = 0;
							for (Entity entity : entityList) {
								jsonObject = new JSONObject(entity
										.getProperties().get("location")
										.toString());
								c += jsonObject.getDouble("speed");
								System.out.println("speed = "
										+ jsonObject.getDouble("speed"));
							}
							System.out.println("ortalama hýz:" + c
									/ entityList.size());
						}
					}
				} catch (Exception e) { // The API request returned an error
					e.printStackTrace();
				}
			}
		});

	}


}
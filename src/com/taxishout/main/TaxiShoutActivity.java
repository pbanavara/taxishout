package com.taxishout.main;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;

import org.json.JSONArray;
import org.json.JSONObject;

public class TaxiShoutActivity extends MapActivity implements OnClickListener{

	private MapController mapController;
	private MapView mapView;
	private LocationManager locationManager;
	private MyOverlays itemizedoverlay;
	private MyLocationOverlay myLocationOverlay;
	private final Handler myHandler = new Handler();
	final Runnable updateRunnable = new Runnable() {
        public void run() {
            //call the activity method that updates the UI
        	mapView.getController().animateTo(myLocationOverlay.getMyLocation());
			setVis();
            
        }
    };
    private void doSomeHardWork()
    {
         //.... hard work
          
         //update the UI using the handler and the runnable
         myHandler.post(updateRunnable);

    }
	private void setVis()
	{
		Button now =(Button)findViewById(R.id.now);
		Button later =(Button)findViewById(R.id.later);
		now.setVisibility(View.VISIBLE);
		later.setVisibility(View.VISIBLE);
		now.setOnClickListener(this);
	}
	private String getData(int lat,int lng,int rad,int limit)
	{
		StringBuilder builder = new StringBuilder();
		String url="http://www.myeur.org/showpoint?lat="+lat+"&lng="+lng+"&rad="+rad+"&limit="+limit;
		HttpClient client = new DefaultHttpClient();
		HttpGet httpGet = new HttpGet(url);
		try {
			HttpResponse response = client.execute(httpGet);
			StatusLine statusLine = response.getStatusLine();
			int statusCode = statusLine.getStatusCode();
			if (statusCode == 200) {
				HttpEntity entity = response.getEntity();
				InputStream content = entity.getContent();
				BufferedReader reader = new BufferedReader(new InputStreamReader(content));
				String line;
				while ((line = reader.readLine()) != null) {
					builder.append(line);
				}
			} else {
				return "FAIL";
			}
		} catch (ClientProtocolException e) {
			e.printStackTrace();
			return "FAIL";
		} catch (IOException e) {
			e.printStackTrace();
			return "FAIL";
		}
		return builder.toString();
	}
	public void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		setContentView(R.layout.main); // bind the layout to the activity

		// Configure the Map
		mapView = (MapView) findViewById(R.id.mapview);
		mapView.setBuiltInZoomControls(false);
		mapView.setSatellite(true);
		mapController = mapView.getController();
		mapController.setZoom(15); // Zoon 1 is world view
		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0,
				0, new GeoUpdateHandler());
		myLocationOverlay = new MyLocationOverlay(this, mapView);
		mapView.getOverlays().add(myLocationOverlay);
		

		myLocationOverlay.runOnFirstFix(new Runnable() {
			public void run() {
				
				doSomeHardWork();
			}
		});
		//mapOverlays.remove(1);
		//Log.d("JSON",mapOverlays.toString());
		
		Button b = (Button)findViewById(R.id.now);
		b.setOnClickListener(this);
	}

	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}

	public class GeoUpdateHandler implements LocationListener {

		@Override
		public void onLocationChanged(Location location) {
			int lat = (int) (location.getLatitude() * 1E6);
			int lng = (int) (location.getLongitude() * 1E6);
			GeoPoint point = new GeoPoint(lat, lng);
			mapController.animateTo(point); // mapController.setCenter(point);
			
		}

		@Override
		public void onProviderDisabled(String provider) {
		}

		@Override
		public void onProviderEnabled(String provider) {
		}

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
		}
	}

	
	@Override
	protected void onResume() {
		super.onResume();
		myLocationOverlay.enableMyLocation();
		myLocationOverlay.enableCompass();
	}

	@Override
	protected void onPause() {
		super.onResume();
		myLocationOverlay.disableMyLocation();
		myLocationOverlay.disableCompass();
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		if(v.getId()==R.id.now)
		{
			GeoPoint g = myLocationOverlay.getMyLocation();
			String JSONStr =getData(g.getLatitudeE6(),g.getLongitudeE6(),2,10);
			if(JSONStr!="FAIL")
			{
			try {
				JSONArray jsonArray = new JSONArray(JSONStr);
				Drawable drawable = this.getResources().getDrawable(R.drawable.point);
				itemizedoverlay = new MyOverlays(drawable,this);
				List<Overlay> mapOverlays = mapView.getOverlays();
				while(mapOverlays.size()>2)
					mapOverlays.remove(1);
				for (int i = 0; i < jsonArray.length(); i++) {
					JSONObject jsonObject = jsonArray.getJSONObject(i);
					int Lat=jsonObject.getInt("Lattitude");
					int Lng = jsonObject.getInt("Longitude");
					Log.d("JSON",jsonObject.getString("Name"));
					GeoPoint point = new GeoPoint(Lat, Lng);
					OverlayItem overlayitem = new OverlayItem(point,"Driver",jsonObject.getString("Name"));
					itemizedoverlay.addOverlay(overlayitem);
									
				}
				mapOverlays.add(itemizedoverlay);
				Toast.makeText(getApplicationContext(), "Read your data!", Toast.LENGTH_LONG).show();
			} catch (Exception e) {
				e.printStackTrace();
			}
			}
			else
			{
				Toast.makeText(getApplicationContext(), "Could not get Data\nPlease check your connection!", Toast.LENGTH_LONG).show();
			}
		}
	}
}

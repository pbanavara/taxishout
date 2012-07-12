package com.taxishout.main;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Stack;

import org.json.JSONArray;
import org.json.JSONException;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;

import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class DestinationActivity extends MapActivity implements OnTouchListener,OnClickListener{
	private MapController mapController;
	private MapView mapView;
	private LocationManager locationManager;
	private MyOverlays itemizedoverlay;
	private MyLocationOverlay myLocationOverlay;
	private int lat,lng;
	private int zoomlevel;
	private Stack st;
	private String message, number, address;
	private int objectId;
	
	public void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		setContentView(R.layout.destination); // bind the layout to the activity
		Bundle extras = getIntent().getExtras();
		zoomlevel = 11;
		if (extras != null) {
		    address = extras.getString("myAddress");
		    lat = extras.getInt("lat");
		    lng = extras.getInt("lng");
		    message = (String) extras.get("msg");
		    number = (String) extras.get("num");
		    objectId = extras.getInt("id");
		    st = new Stack();
		    Log.d("JSON",address);
		}
		Button b = (Button) findViewById(R.id.later);
		b.setOnClickListener(this);
		b = (Button) findViewById(R.id.now);
		b.setOnClickListener(this);
		mapView = (MapView) findViewById(R.id.mapview);
		mapView.setOnTouchListener(this);
		mapView.setBuiltInZoomControls(false);
		mapView.setSatellite(true);
		mapView.setClickable(false);
		mapController = mapView.getController();
		//this.setOnTouchListener();
		mapController.setZoom(zoomlevel); // Zoom 15 shows locations close to the user
		GeoPoint g = new GeoPoint(lat,lng);
		st.push(g.getLatitudeE6());
		st.push(g.getLongitudeE6());
		mapView.getController().animateTo(g);
				
		
    	
	}
	@Override
	public boolean onTouch(View v, MotionEvent event) {
		// TODO Auto-generated method stub
		if(v.getId()==R.id.mapview)
		{
			if(zoomlevel<11)
			{
				Button b = (Button) findViewById(R.id.later);
				b.setVisibility(View.GONE);
			}
			else
			{
				Button b = (Button) findViewById(R.id.later);
				b.setVisibility(View.VISIBLE);
			}
			if(zoomlevel>15)
			{
				Button b = (Button) findViewById(R.id.now);
				b.setVisibility(View.VISIBLE);
			}
			else
			{
				Button b = (Button) findViewById(R.id.now);
				b.setVisibility(View.GONE);
			}
			if(zoomlevel>17)
			{
				
			}
			else
			{
				double width = mapView.getWidth();
				double height= mapView.getHeight();
				GeoPoint g =mapView.getMapCenter();
				int lat_span = mapView.getLatitudeSpan();
				int lng_span = mapView.getLongitudeSpan();
				int lat_tl = g.getLatitudeE6() + lat_span/2; 
				int lng_tl = g.getLongitudeE6() - lng_span/2;
				float xpos =  (float) (event.getX()/width);
				float ypos = (float) (event.getY()/height);
				int newLat = (int) (lat_tl - lat_span*ypos); 
				int newLng = (int) (lng_tl + lng_span*xpos);
				GeoPoint G = new GeoPoint(newLat,newLng);
				Toast.makeText(getApplicationContext(), "X:"+xpos+"\nY:"+ypos, Toast.LENGTH_SHORT).show();
				mapView.getController().animateTo(G);
				mapController.setZoom(++zoomlevel); 
				st.push(newLat);
				st.push(newLng);
				
			}
		}
		return false;
	}

	@Override
	protected boolean isRouteDisplayed() {
		// TODO Auto-generated method stub
		return false;
	}
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		if(v.getId()==R.id.later)
		{
			if(st.size()>1)
			{
				int Lng = (Integer) st.pop();
				int Lat = (Integer) st.pop();
				GeoPoint G = new GeoPoint(Lat,Lng);
				mapView.getController().animateTo(G);
				mapController.setZoom(--zoomlevel);
			}
			else
				Toast.makeText(getApplicationContext(), "Can't zoom out any more", Toast.LENGTH_SHORT).show();
			
				
			
		}
		else if(v.getId()==R.id.now)
		{
			LayoutInflater li = LayoutInflater.from(this);
			View promptsView = li.inflate(R.layout.prompts, null);
			AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
					this);
			// set prompts.xml to alertdialog builder
			alertDialogBuilder.setView(promptsView);
			final EditText userInput = (EditText) promptsView
					.findViewById(R.id.editTextDialogUserInput);
			userInput.setHint("Enter a custom message");
			TextView tv = (TextView) promptsView.findViewById(R.id.textView1);
			Geocoder gc = new Geocoder(getBaseContext(),Locale.getDefault());
	    	int lngMD = (Integer) st.pop();
	    	int latMD = (Integer) st.pop();
	    	st.push(latMD);
	    	st.push(lngMD);
	    	double Lati = latMD/1000000.0;
	    	double Longi = lngMD/1000000.0;
	    	
	    	try {
				List<Address> addresses = gc.getFromLocation(Lati, Longi, 1);
				Log.d("JSON",addresses.toString());
				String destAddress="";
				if(addresses.size()<1)
				{
					Log.d("ADDRESS","NOT FOUND!");
					
				}
				else 
				{
					int j = 0;
					Address gen = addresses.get(0);
					Log.d("JSON",gen.getLocality());
					while(j<=gen.getMaxAddressLineIndex())
					{
						destAddress =destAddress+gen.getAddressLine(j)+",";
						j++;
					}
					
					
					tv.setText(message + "Destination is :"+destAddress);	
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			// set dialog message
			alertDialogBuilder
				.setCancelable(false)
				.setPositiveButton("Send message",
				  new DialogInterface.OnClickListener() {
				    public void onClick(DialogInterface dialog,int id) {
				    	//Set isAvaiable to false
						//get address first
				    	
				    	
				    	setUnavailable(objectId);
						//Sending SMS; PendingIntent activities to be added
						String sms = "New Client Request" + "\n" + 
						"Location:" + address + "\nComments:" + userInput.getText();
						String SENT = "SMS_SENT";
						PendingIntent sentIntent = PendingIntent.getBroadcast(getBaseContext(), 0, new Intent(SENT), 0);
						SmsManager SMS = SmsManager.getDefault();
				       	//SMS.sendTextMessage(DriverNumber, null, sms, sentIntent, null);	
				    }

					private void setUnavailable(int objectId) {
						// TODO Auto-generated method stub
						
					}
				  })
				.setNeutralButton("Call driver", new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						Intent i = new Intent(Intent.ACTION_CALL);
						i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
						i.setData(Uri.parse("tel:"+number));
						startActivity(i);
						
					}
				})
				.setNegativeButton("Cancel",
				  new DialogInterface.OnClickListener() {
				    public void onClick(DialogInterface dialog,int id) {
					dialog.cancel();
				    }
				  });

			// create alert dialog
			AlertDialog alertDialog = alertDialogBuilder.create();

			// show it
			alertDialog.show();
		}
		
		
	}
	

}

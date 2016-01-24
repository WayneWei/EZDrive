package com.example.waynewei.ezdrive;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.animation.OvershootInterpolator;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.Query;
import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.mikepenz.fontawesome_typeface_library.FontAwesome;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

	private MyApplication globalVariable;
	private GoogleMap mMap;
	private LatLng position;
	private FloatingActionMenu menu;
	private Location myLocation;
	private Marker marker;
	private ArrayList<Accident> accidents = new ArrayList<>();
	private ArrayList<Marker> markers = new ArrayList<>();
	private CameraPosition cameraPosition;
	private LocationManager locationManager;
	private Firebase ref;
	private final long duration = 3600000;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

		globalVariable = (MyApplication) getApplicationContext();

		createDrawer(toolbar);

		setUpMapIfNeeded();
		createCustomAnimation();

		Firebase.setAndroidContext(this);




	}


	public void createDrawer(Toolbar toolbar) {

		new DrawerBuilder()
				.withActivity(this)
				.withToolbar(toolbar)
				.addDrawerItems(
						new PrimaryDrawerItem().withName(R.string.drawer_item_home).withIcon(FontAwesome.Icon.faw_home).withSelectable(false),
						new PrimaryDrawerItem().withName(R.string.drawer_item_contact).withIcon(FontAwesome.Icon.faw_bullhorn).withSelectable(false)
				)
				.withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
					@Override
					public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
						// do something with the clicked item :D
						Intent intent = null;

						if (drawerItem != null) {
							if (drawerItem.getIdentifier() == 1) {
//								if (globalVariable.checkInternet())
////									intent = new Intent(this, SignInActivity.class);
//								else
//									globalVariable.noticeInternet(MainActivity.this, view);
							}
							if (intent != null) {
								startActivity(intent);
							}

						}

						return false;
					}
				})
				.addStickyDrawerItems(
						new SecondaryDrawerItem().withName(R.string.drawer_item_settings).withIcon(FontAwesome.Icon.faw_cog).withIdentifier(10),
						new SecondaryDrawerItem().withName(R.string.drawer_item_open_source).withIcon(FontAwesome.Icon.faw_github)
				)
				.withSelectedItem(-1)
				.build();

	}

	private void createCustomAnimation() {
		menu = (FloatingActionMenu) findViewById(R.id.expanded_menu);

		AnimatorSet set = new AnimatorSet();

		ObjectAnimator scaleOutX = ObjectAnimator.ofFloat(menu.getMenuIconView(), "scaleX", 1.0f, 0.2f);
		ObjectAnimator scaleOutY = ObjectAnimator.ofFloat(menu.getMenuIconView(), "scaleY", 1.0f, 0.2f);

		ObjectAnimator scaleInX = ObjectAnimator.ofFloat(menu.getMenuIconView(), "scaleX", 0.2f, 1.0f);
		ObjectAnimator scaleInY = ObjectAnimator.ofFloat(menu.getMenuIconView(), "scaleY", 0.2f, 1.0f);

		scaleOutX.setDuration(50);
		scaleOutY.setDuration(50);

		scaleInX.setDuration(150);
		scaleInY.setDuration(150);

		scaleInX.addListener(new AnimatorListenerAdapter() {
			@Override
			public void onAnimationStart(Animator animation) {
				menu.getMenuIconView().setImageResource(menu.isOpened()
						? R.drawable.ic_close_white_24dp : R.drawable.ic_warning);
			}
		});

		set.play(scaleOutX).with(scaleOutY);
		set.play(scaleInX).with(scaleInY).after(scaleOutX);
		set.setInterpolator(new OvershootInterpolator(2));

		menu.setIconToggleAnimatorSet(set);

		FloatingActionButton fabAccident = (FloatingActionButton) findViewById(R.id.fab_accident);
		fabAccident.setOnClickListener(this);
		FloatingActionButton fabConstruction = (FloatingActionButton) findViewById(R.id.fab_construction);
		fabConstruction.setOnClickListener(this);
		FloatingActionButton fabTrafficControl = (FloatingActionButton) findViewById(R.id.fab_traffic_control);
		fabTrafficControl.setOnClickListener(this);
		FloatingActionButton fabBarrier = (FloatingActionButton) findViewById(R.id.fab_barrier);
		fabBarrier.setOnClickListener(this);
	}

	private void setUpMapIfNeeded() {
		// Do a null check to confirm that we have not already instantiated the map.
		if (mMap == null) {
			// Try to obtain the map from the SupportMapFragment.
			mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.google_map))
					.getMap();
			if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
				// TODO: Consider calling
				//    ActivityCompat#requestPermissions
				// here to request the missing permissions, and then overriding
				//   public void onRequestPermissionsResult(int requestCode, String[] permissions,
				//                                          int[] grantResults)
				// to handle the case where the user grants the permission. See the documentation
				// for ActivityCompat#requestPermissions for more details.
				return;
			}

			locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

			myLocation = locationManager.getLastKnownLocation(
					LocationManager.GPS_PROVIDER);
			if (myLocation == null) {
				myLocation = locationManager.getLastKnownLocation(
						LocationManager.NETWORK_PROVIDER);
			}

			// Check if we were successful in obtaining the map.
			if (mMap != null) {

				mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {

					@Override
					public boolean onMarkerClick(Marker marker) {
						if (marker.getTitle().equals("center")){
							marker.hideInfoWindow();
							return true;
						}
						else{
							marker.showInfoWindow();
							return false;
						}

					}

				});


				LocationListener locationListener = new LocationListener() {

					@Override
					public void onProviderDisabled(String provider) {

					}

					@Override
					public void onLocationChanged(Location location) {

						position = new LatLng(location.getLatitude(), location.getLongitude());

						myLocation = location;
						marker.remove();
						cameraPosition = new CameraPosition.Builder()
								.target(position)
								.zoom(18)
								.tilt(60)
								.build();
						mMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
						marker = mMap.addMarker(new MarkerOptions().title("center").position(position).icon(BitmapDescriptorFactory.fromResource(R.drawable.car)));

						Log.d("My location:", String.valueOf(position));
					}

					@Override
					public void onStatusChanged(String provider, int status, Bundle extras) {

					}

					@Override
					public void onProviderEnabled(String provider) {

					}
				};

				long minTime = 5 * 1000; // Minimum time interval for update in seconds, i.e. 5 seconds.
				long minDistance = 10; // Minimum distance change for update in meters, i.e. 10 meters.

				locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, minTime, minDistance, locationListener);
				locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, minTime, minDistance, locationListener);


				position = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());

				cameraPosition = new CameraPosition.Builder()
						.target(position)
						.zoom(18)
						.tilt(60)
						.build();

				// 使用動畫的效果移動地圖
				mMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
				marker = mMap.addMarker(new MarkerOptions().title("center").position(position).icon(BitmapDescriptorFactory.fromResource(R.drawable.car)));




			}
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu_main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();

		//noinspection SimplifiableIfStatement
		if (id == R.id.current_location) {
			if(cameraPosition!=null)
				mMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
			return true;
		}

		return super.onOptionsItemSelected(item);
	}


	@Override
	public void onClick(View v) {
		String title = null;
		int type = 0;
		int icon = 0;
		switch (v.getId()) {
			case R.id.fab_accident:
				type = 0;
				title = getString(R.string.accident);
				icon = R.drawable.ic_accident_red;
				break;
			case R.id.fab_construction:
				type = 1;
				title = getString(R.string.construction);
				icon = R.drawable.ic_construction_yellow;
				break;
			case R.id.fab_traffic_control:
				type = 2;
				title = getString(R.string.traffic_control);
				icon = R.drawable.ic_traffic_control_green;
				break;
			case R.id.fab_barrier:
				type = 3;
				title = getString(R.string.barrier);
				icon = R.drawable.ic_barrier_blue;
				break;
		}

		final String finalTitle = title;
		final int finalType = type;
		new MaterialDialog.Builder(this)
				.title(title)
				.iconRes(icon)
				.inputType(InputType.TYPE_CLASS_TEXT |
						InputType.TYPE_TEXT_VARIATION_PERSON_NAME |
						InputType.TYPE_TEXT_FLAG_CAP_WORDS)
				.positiveText(R.string.confirm)
				.negativeText(R.string.cancel)
				.titleColorRes(R.color.md_cyan_800)
				.input(R.string.description, 0, true, new MaterialDialog.InputCallback() {
					@Override
					public void onInput(MaterialDialog dialog, CharSequence input) {
						String notice = null;
						long timestamp = System.currentTimeMillis();
						if (checkAccidentExist(timestamp)) {
							notice = finalTitle + " 已通報";
						} else {

							Firebase newPostRef = ref.child("accident").push();
							Accident accident = new Accident();
							accident.setDescription(String.valueOf(input));
							accident.setType(finalType);
							accident.setPosition(myLocation.getLatitude(), myLocation.getLongitude());
							accident.setTimestamp(timestamp);
							newPostRef.setValue(accident);
							notice = finalTitle + " 通報成功";

						}
						Snackbar.make(menu, notice, Snackbar.LENGTH_SHORT).show();
						menu.close(true);


					}
				}).show();
	}

	private boolean checkAccidentExist(long timestamp) {
		for(Accident accident : accidents){
			if(accident.getPosition().getLat() == myLocation.getLatitude() &&
					accident.getPosition().getLng() == myLocation.getLongitude()){
				if(timestamp - accident.getTimestamp() < duration)
					return true;
			}
		}
		return false;
	}


	@Override
	protected void onStart() {
		super.onStart();

		if(!globalVariable.checkInternet()){
			globalVariable.noticeInternet(this, menu);
		}

		if ( !locationManager.isProviderEnabled( LocationManager.GPS_PROVIDER ) ){
			Snackbar.make(menu, getString(R.string.no_gps_title), Snackbar.LENGTH_LONG)
					.setAction("SETTING", new View.OnClickListener() {
						@Override
						public void onClick(View view) {
							new MaterialDialog.Builder(MainActivity.this)
									.title(R.string.no_gps_title)
									.content(R.string.gps_detail)
									.positiveText(R.string.confirm)
									.negativeText(R.string.cancel)
									.callback(new MaterialDialog.ButtonCallback() {
										@Override
										public void onPositive(MaterialDialog dialog) {
											startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
										}
									})
									.show();
						}
					})
					.show();
		}

		ref = new Firebase("https://glaring-torch-4222.firebaseio.com/");
		System.out.println(System.currentTimeMillis() - duration);
		final Query queryRef = ref.child("accident").orderByChild("timestamp").startAt(System.currentTimeMillis() - duration);

		queryRef.addChildEventListener(new ChildEventListener() {
			@Override
			public void onChildAdded(final DataSnapshot dataSnapshot, String s) {

				System.out.println(dataSnapshot.getChildrenCount());
				Accident accident = dataSnapshot.getValue(Accident.class);
								int icon = 0;
								String title = null;
								switch (accident.getType()) {
									case 0:
										title = getString(R.string.accident);
										icon = R.drawable.ic_accident_red;
										break;
									case 1:
										title = getString(R.string.construction);
										icon = R.drawable.ic_construction_yellow;
										break;
									case 2:
										title = getString(R.string.traffic_control);
										icon = R.drawable.ic_traffic_control_green;
										break;
									case 3:
										title = getString(R.string.barrier);
										icon = R.drawable.ic_barrier_blue;
										break;
								}

								final Marker marker = mMap.addMarker(new MarkerOptions()
										.title(title)
										.snippet(accident.getType() + "-" + accident.getDescription() + "-" + accident.getTimestamp())
										.position(new LatLng(accident.getPosition().getLat(), accident.getPosition().getLng()))
										.icon(BitmapDescriptorFactory.fromResource(icon)));

								mMap.setInfoWindowAdapter(new AccidentInfoWindowAdapter(MainActivity.this, accident));

								markers.add(marker);
								accidents.add(accident);

//
			}

			@Override
			public void onChildChanged(DataSnapshot dataSnapshot, String s) {

			}

			@Override
			public void onChildRemoved(DataSnapshot dataSnapshot) {

			}

			@Override
			public void onChildMoved(DataSnapshot dataSnapshot, String s) {

			}

			@Override
			public void onCancelled(FirebaseError firebaseError) {

			}
		});

	}
}


package com.afrometal.radoslaw.maplines;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.JointType;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.RoundCap;
import com.google.gson.Gson;

import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback {

    private GoogleMap mMap;
    private Places places;
    private ArrayList<Marker> markers;
    private Polyline polyline;

    private ArrayList<Place> restoredMarkersPlaces = null;
    private ArrayList<LatLng> restoredPolylinePoints = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        Menu menu = navigationView.getMenu();

        places = getPlacesFromJson(R.raw.places);
        places.sort();
        for (Place p : places.getPlaces()) {
            p.setId(View.generateViewId());
            MenuItem item = menu.add(R.id.places_group, (int) p.getId(), Menu.NONE, p.getCity());
            item.setIcon(R.drawable.ic_menu_place);
        }

        markers = new ArrayList<>();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (polyline == null) {
                    List<LatLng> points = new ArrayList<>();
                    for (Marker m : markers) {
                        points.add(m.getPosition());
                    }

                    if (points.size() <= 1) {
                        Toast.makeText(getApplicationContext(), getString(R.string.more_markers), Toast.LENGTH_SHORT).show();
                    } else {
                        drawLine(points);
                    }
                } else {
                    deleteLine();
                }
            }
        });
    }

    private Places getPlacesFromJson(int id) {
        Gson gson = new Gson();
        Reader reader = new InputStreamReader(getResources().openRawResource(id));

        return gson.fromJson(reader, Places.class);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        long id = item.getItemId();

        if (id == R.id.nav_zoom_out) {
            mMap.animateCamera(CameraUpdateFactory.zoomBy(-1 * mMap.getMaxZoomLevel()));
        } else if (id == R.id.nav_clear) {
            deleteLine();
        } else if (id == R.id.nav_clear_all) {
            for (Marker m : markers) {
                m.remove();
            }
            markers.clear();

            if (polyline != null) {
                polyline.remove();
                polyline = null;
                ((FloatingActionButton) findViewById(R.id.fab)).setImageResource(R.drawable.ic_menu_timeline);
            }
        } else {
            Place place = places.getById(id);

            List<Double> coordinates = place.getCoordinates();
            LatLng latLng = new LatLng(coordinates.get(0), coordinates.get(1));

            for (Marker m : markers) {
                if (m.getTag() == place) {
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, Math.min(mMap.getMaxZoomLevel(), 10.0f)));
                    return false;
                }
            }

            Marker marker = drawMarker(place);

            mMap.animateCamera(
                    CameraUpdateFactory.newLatLngZoom(
                            marker.getPosition(),
                            Math.min(mMap.getMaxZoomLevel(), 10.0f))
            );

            Snackbar.make(
                    findViewById(R.id.coordinator_layout),
                    place.getCity(),
                    Snackbar.LENGTH_SHORT)
                    .show();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return false;
    }

    /**
     * Draws line connecting visible markers.
     */
    private void drawLine(List<LatLng> points) {
        PolylineOptions line = new PolylineOptions()
                .startCap(new RoundCap())
                .endCap(new RoundCap())
                .color(getColor(R.color.colorPrimaryDark))
                .jointType(JointType.ROUND)
                .addAll(points);

        polyline = mMap.addPolyline(line);

        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (LatLng p : points) {
            builder.include(p);
        }

        LatLngBounds bounds = builder.build();

        int width = findViewById(R.id.map).getWidth();
        int height = findViewById(R.id.map).getHeight();
        int padding = (int) (width * 0.1);

        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, width, height, padding));

        ((FloatingActionButton) findViewById(R.id.fab)).setImageResource(R.drawable.ic_menu_delete_white);
    }

    /**
     * Deletes line from map with option to undo.
     */
    private void deleteLine() {
        if (polyline == null) {
            return;
        }

        final Polyline backup = polyline;
        polyline.remove();
        polyline = null;

        Snackbar snackbar = Snackbar.make(
                findViewById(R.id.coordinator_layout),
                getResources().getString(R.string.lines_removed),
                Snackbar.LENGTH_LONG)
                .setAction(getResources().getString(R.string.undo), new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        drawLine(backup.getPoints());
                    }
                });
        snackbar.show();
        ((FloatingActionButton) findViewById(R.id.fab)).setImageResource(R.drawable.ic_menu_timeline);
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
            @Override
            public void onMarkerDragStart(Marker marker) {
                final Place place = (Place) marker.getTag();
                markers.remove(marker);
                marker.remove();
                Snackbar snackbar = Snackbar.make(
                        findViewById(R.id.coordinator_layout),
                        String.format(
                                "%s %s",
                                marker.getTitle(),
                                getResources().getString(R.string.remove_marker)),
                        Snackbar.LENGTH_LONG
                ).setAction(getResources().getString(R.string.undo), new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Marker marker = drawMarker(place);

                        mMap.animateCamera(
                                CameraUpdateFactory.newLatLngZoom(
                                        marker.getPosition(),
                                        Math.min(mMap.getMaxZoomLevel(), 10.0f))
                        );
                    }
                });
                snackbar.show();
            }

            @Override
            public void onMarkerDrag(Marker marker) {

            }

            @Override
            public void onMarkerDragEnd(Marker marker) {

            }
        });

        if (restoredMarkersPlaces != null) {
            for (Place p : restoredMarkersPlaces) {
                drawMarker(p);
            }
        }
        if (restoredPolylinePoints != null && restoredPolylinePoints.size() >= 2) {
            drawLine(restoredPolylinePoints);
        }
    }

    private Marker drawMarker(Place place) {
        List<Double> coordinates = place.getCoordinates();
        LatLng latLng = new LatLng(coordinates.get(0), coordinates.get(1));

        Marker marker = mMap.addMarker(new MarkerOptions()
                .position(latLng)
                .draggable(true)
                .title(place.getCity()));
        marker.setTag(place);
        markers.add(marker);

        return marker;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        ArrayList<Place> markerPlaces = new ArrayList<>();
        for (Marker m : markers) {
            Place p = (Place) m.getTag();
            Log.d("savedPlace", p.toString());
            markerPlaces.add(p);
        }
        outState.putParcelableArrayList("markersPlaces", markerPlaces);
        if (polyline != null) {
            outState.putParcelableArrayList("polylinePoints", (ArrayList) polyline.getPoints());
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        // Restore data to add to map in onMapReady callback
        restoredMarkersPlaces = savedInstanceState.getParcelableArrayList("markersPlaces");
        restoredPolylinePoints = savedInstanceState.getParcelableArrayList("polylinePoints");
        super.onRestoreInstanceState(savedInstanceState);
    }
}

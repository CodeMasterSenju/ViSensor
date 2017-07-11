/* Copyright 2017 Artur Baltabayev, Jean-Josef Büschel, Martin Kern, Gabriel Scheibler
 *
 * This file is part of ViSensor.
 *
 * ViSensor is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * ViSensor is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with ViSensor.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.artur.softwareproject;

import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.JsonReader;
import android.util.JsonToken;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.algo.NonHierarchicalDistanceBasedAlgorithm;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.Collection;

import static com.artur.softwareproject.BluetoothConnectionList.EXTRA_FILES;

public class VRmenuMap extends AppCompatActivity implements OnMapReadyCallback,
                                                            ClusterManager.OnClusterClickListener<GeoItem>,
                                                            ClusterManager.OnClusterItemClickListener<GeoItem>,
                                                            ClusterManager.OnClusterItemInfoWindowClickListener<GeoItem>,
                                                            ClusterManager.OnClusterInfoWindowClickListener<GeoItem>
{

    private GoogleMap mMap;
    private ClusterManager<GeoItem> mClusterManager;

    private String[] sessionFileNames;
    private final String path = "/ViSensor/Json";
    private final File pathName = new File(Environment.getExternalStorageDirectory().toString() + path);

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vrmenu_map);
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true); //implements the back button (android handles that by default)


        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        sessionFileNames = pathName.list();
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap)
    {
        mMap = googleMap;
        setUpClusterer();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.vr_map_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        if (item.getItemId() == R.id.vr_menu_all_locations)
        {

            Intent vrIntent = new Intent(this, VRmenu.class);
            //vrIntent.putExtra(EXTRA_FILES, array);
            vrIntent.putExtra(EXTRA_FILES, (String)null);
            VRmenuMap.this.startActivity(vrIntent);

            return true;
        }
        if (item.getItemId() == android.R.id.home)
        {
            finish();
            return true;
        }


        return super.onOptionsItemSelected(item);
    }

    private void setUpClusterer()
    {
        // Position the map.
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(47.6779, 9.1732), 7));

        // Initialize the manager with the context and the map.
        // (Activity extends context, so we can pass 'this' in the constructor.)
        mClusterManager = new ClusterManager<>(this, mMap);

        mClusterManager.setAlgorithm(new NonHierarchicalDistanceBasedAlgorithm<GeoItem>());
        mClusterManager.setRenderer(new CustomClusterRenderer(this, mMap, mClusterManager));

        // Point the map's listeners at the listeners implemented by the cluster
        // manager.
        mMap.setOnCameraIdleListener(mClusterManager);
        mMap.setOnMarkerClickListener(mClusterManager);
        mMap.setOnInfoWindowClickListener(mClusterManager);

        mClusterManager.setOnClusterClickListener(this);
        mClusterManager.setOnClusterItemClickListener(this);
        mClusterManager.setOnClusterInfoWindowClickListener(this);
        mClusterManager.setOnClusterItemInfoWindowClickListener(this);


        // Add cluster items (markers) to the cluster manager.
        addItems();

        positionCamera();
    }

    private void addItems()
    {
        String baseDirectory = Environment.getExternalStorageDirectory().getAbsolutePath();

        for (String item : sessionFileNames)
        {
            File f = new File(baseDirectory + "/ViSensor/JSON/" + item);
            LatLng l = getLatLng(f);
            GeoItem geoItem = new GeoItem(l.latitude, l.longitude, item);
            mClusterManager.addItem(geoItem);
        }


    }

    /**
     * position Camera so that all markers are visible
     */
    private void positionCamera()
    {
        LatLngBounds.Builder builder = new LatLngBounds.Builder();

        Collection<GeoItem> items = mClusterManager.getAlgorithm().getItems();


        for (GeoItem item : items)
        {
            builder.include(item.getPosition());
        }

        try
        {
            LatLngBounds llb = builder.build();
            mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(llb, 120));
        }
        catch (Exception e)
        {e.printStackTrace();}
    }

    @Override
    public boolean onClusterClick(Cluster cluster)
    {
        return false; // default onClusterClicked is called after
    }

    @Override
    public boolean onClusterItemClick(GeoItem clusterItem)
    {
        return  false;  // default onClusterItemClicked is called after
    }

    private LatLng getLatLng(File f)
    {
        try
        {
            FileInputStream fileInputStream = new FileInputStream(f);
            JsonReader reader = new JsonReader(new InputStreamReader(fileInputStream, "UTF-8"));

            reader.setLenient(true);
            reader.beginObject();

            double lat = 0;
            double lng = 0;

            while (reader.hasNext() && reader.peek() == JsonToken.NAME)
            {
                String name = reader.nextName();
                if (!name.equals("coordinates"))
                {
                    reader.skipValue();
                } else
                {
                    reader.beginObject();
                    while (reader.hasNext() && reader.peek() == JsonToken.NAME)
                    {
                        switch (reader.nextName()) {
                            case "latitude":
                                lat = reader.nextDouble();
                                break;
                            case "longitude":
                                lng = reader.nextDouble();
                                break;
                            default:
                                reader.skipValue();
                                break;
                        }

                    }
                    return new LatLng(lat, lng);
                }
            }

            Log.d("Failed", "Coordinates not found in json file");
            return new LatLng(lat, lng);


        } catch (Exception e)
        {
            e.printStackTrace();
            Log.d("Failed", "Error reading coordinates from json file");
            return new LatLng(51.5145160, -0.1270060);//Brasserie Max - London
        }
    }

    @Override
    public void onClusterInfoWindowClick(Cluster<GeoItem> cluster)
    {
        String[] filenames = new String[cluster.getSize()];
        Collection<GeoItem> items = cluster.getItems();
        int i = 0;
        for (GeoItem item : items)
        {
            filenames[i] = item.getFilename();
            i++;
        }

        //Start VRmenu with the selected dataSets
        Intent vrIntent = new Intent(this, VRmenu.class);
        vrIntent.putExtra(EXTRA_FILES, filenames);
        VRmenuMap.this.startActivity(vrIntent);
    }

    @Override
    public void onClusterItemInfoWindowClick(GeoItem clusterItem)
    {
        //start web server
        Intent webServerIntent = new Intent(this, SimpleWebServer.class);
        startService(webServerIntent);

        //start vr visualisation of the selected dataset in chrome
        String fileName = clusterItem.getFilename();

        String json = fileName.split("\\.")[0];

        String requestURL = String.format("http://localhost:8080/index.html?file=%s?sensor=%s", Uri.encode(json), Uri.encode("illuminance"));

        Intent webVRIntent = new Intent(Intent.ACTION_VIEW);
        webVRIntent.addCategory(Intent.CATEGORY_BROWSABLE);
        webVRIntent.setData(Uri.parse(requestURL));
        webVRIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        webVRIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        webVRIntent.setPackage("com.android.chrome");//Use Google Chrome

        startActivity(webVRIntent);
    }
}

//EOF
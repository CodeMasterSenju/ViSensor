package com.artur.softwareproject;

import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterItem;
import com.google.maps.android.clustering.ClusterManager;

import java.io.File;
import java.util.Collection;

import static com.artur.softwareproject.BluetoothConnectionList.EXTRA_FILES;

public class VRmenuMap extends AppCompatActivity implements OnMapReadyCallback, ClusterManager.OnClusterClickListener, ClusterManager.OnClusterItemClickListener
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
        getSupportActionBar().setDisplayHomeAsUpEnabled(true); //implements the back button (android handles that by default)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
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

        /*// Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));*/
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
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(51.503186, -0.126446), 10));

        // Initialize the manager with the context and the map.
        // (Activity extends context, so we can pass 'this' in the constructor.)
        mClusterManager = new ClusterManager<GeoItem>(this, mMap);

        // Point the map's listeners at the listeners implemented by the cluster
        // manager.
        mMap.setOnCameraIdleListener(mClusterManager);
        mMap.setOnMarkerClickListener(mClusterManager);

        mClusterManager.setOnClusterClickListener(this);
        mClusterManager.setOnClusterItemClickListener(this);


        // Add cluster items (markers) to the cluster manager.
        addItems();
        addItems();
        addItems();
        mClusterManager.cluster();
    }


    private void addItems()
    {
        String baseDirectory = Environment.getExternalStorageDirectory().getAbsolutePath();

        /*// Set some lat/lng coordinates to start with.
        double lat = 51.5145160;
        double lng = -0.1270060;

        // Add ten cluster items in close proximity, for purposes of this example.
        for (int i = 0; i < 10; i++)
        {
            double offset = i / 60d;
            lat = lat + offset;
            lng = lng + offset;
            GeoItem offsetItem = new GeoItem(lat, lng, "hi");
            mClusterManager.addItem(offsetItem);
        }*/
        int i = 0;
        for (String item : sessionFileNames)
        {
            File f = new File(baseDirectory + "/ViSensor/JSON/" + item);
            LatLng l = getLatLng(f);
            GeoItem geoItem = new GeoItem(l.latitude + (i / 60d), l.longitude + (i / 60d), item);
            mClusterManager.addItem(geoItem);
            i++;
        }
    }

    @Override
    public boolean onClusterClick(Cluster cluster)
    {
        String[] filenames = new String[cluster.getSize()];
        Collection<GeoItem> items = cluster.getItems();
        int i = 0;
        for (GeoItem item : items)
        {
            filenames[i] = item.getFilename();
            i++;
        }

        Intent vrIntent = new Intent(this, VRmenu.class);
        vrIntent.putExtra(EXTRA_FILES, filenames);
        VRmenuMap.this.startActivity(vrIntent);


        return false;
    }

    @Override
    public boolean onClusterItemClick(ClusterItem clusterItem)
    {
        String fileName = ((GeoItem) clusterItem).getFilename();

        String baseDirectory = Environment.getExternalStorageDirectory().getAbsolutePath();

        String json = "JSON/" + fileName;
        String obj = "OBJ/" + fileName.replace("json", "obj");

        File objFile = new File(baseDirectory + "/ViSensor/OBJ/" + fileName.replace("json", "obj"));
        File jsonFile = new File(baseDirectory + "/ViSensor/JSON/" + fileName);
        File html = new File(baseDirectory + "/ViSensor/halloWelt.html");

        Uri webVRUri = Uri.parse("content://com.android.provider/ViSensor/ViSensor/index.html?sensor=light?file=" + json);

        Intent webVRIntent = new Intent(Intent.ACTION_VIEW);
        webVRIntent.addCategory(Intent.CATEGORY_BROWSABLE);
        webVRIntent.setData(webVRUri);
        webVRIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        webVRIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        webVRIntent.setPackage("com.android.chrome");//Use Google Chrome

        startActivity(webVRIntent);
        return false;
    }

    private LatLng getLatLng(File f)
    {
        double lat = 51.5145160;
        double lng = -0.1270060;

        return new LatLng(lat, lng);
    }
}

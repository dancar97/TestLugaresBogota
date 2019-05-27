package com.geogeeks.lugaresbogota;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.esri.arcgisruntime.ArcGISRuntimeEnvironment;
import com.esri.arcgisruntime.concurrent.ListenableFuture;
import com.esri.arcgisruntime.data.Feature;
import com.esri.arcgisruntime.data.FeatureQueryResult;
import com.esri.arcgisruntime.data.QueryParameters;
import com.esri.arcgisruntime.data.ServiceFeatureTable;
import com.esri.arcgisruntime.geometry.Envelope;
import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.geometry.SpatialReferences;
import com.esri.arcgisruntime.layers.FeatureLayer;
import com.esri.arcgisruntime.loadable.LoadStatus;
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.Basemap;
import com.esri.arcgisruntime.mapping.GeoElement;
import com.esri.arcgisruntime.mapping.Viewpoint;
import com.esri.arcgisruntime.mapping.view.Callout;
import com.esri.arcgisruntime.mapping.view.DefaultMapViewOnTouchListener;
import com.esri.arcgisruntime.mapping.view.Graphic;
import com.esri.arcgisruntime.mapping.view.IdentifyGraphicsOverlayResult;
import com.esri.arcgisruntime.mapping.view.IdentifyLayerResult;
import com.esri.arcgisruntime.mapping.view.LocationDisplay;
import com.esri.arcgisruntime.mapping.view.MapView;
import com.esri.arcgisruntime.portal.Portal;
import com.esri.arcgisruntime.portal.PortalItem;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity {
    private int requestCode = 2;
    String[] reqPermissions = new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission
            .ACCESS_COARSE_LOCATION};

    private MapView mMapView;
    private Callout mCallout;
    private TextView textoNombre;
    private LocationDisplay mLocationDisplay;
    private ServiceFeatureTable mServiceFeatureTable;
    private ServiceFeatureTable mServiceFeatureTable0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // inflate MapView from layout
        mMapView = (MapView) findViewById(R.id.mapView);
        // create an ArcGISMap with BasemapType topo
        final ArcGISMap map = new ArcGISMap(Basemap.Type.TOPOGRAPHIC, 4.6097102, -74.081749, 12);
        // set the ArcGISMap to the MapView
        mServiceFeatureTable = new ServiceFeatureTable("https://services.arcgis.com/8DAUcrpQcpyLMznu/arcgis/rest/services/Cafes_y_Restaurantes_Tematicos_en_Bogota_WFL1/FeatureServer/0");
        mServiceFeatureTable0 = new ServiceFeatureTable("https://services.arcgis.com/8DAUcrpQcpyLMznu/arcgis/rest/services/Cafes_y_Restaurantes_Tematicos_en_Bogota_WFL1/FeatureServer/1");
        final FeatureLayer featureLayer = new FeatureLayer(mServiceFeatureTable);
        final FeatureLayer featureLayer0 = new FeatureLayer(mServiceFeatureTable0);
        map.getOperationalLayers().add(featureLayer);


        map.getOperationalLayers().add(featureLayer0);
        mMapView.setMap(map);
        textoNombre = (TextView) findViewById(R.id.textView);
        textoNombre.setMovementMethod(new ScrollingMovementMethod());
        // get the callout that shows attributes
        mCallout = mMapView.getCallout();
        // create the service feature table

        // create the feature layer using the service feature table


        // add the layer to the map




        mLocationDisplay = mMapView.getLocationDisplay();

        if (!mLocationDisplay.isStarted()){
            mLocationDisplay.startAsync();
    }
        // Listen to changes in the status of the location data source.
        mLocationDisplay.addDataSourceStatusChangedListener(new LocationDisplay.DataSourceStatusChangedListener() {
            @Override
            public void onStatusChanged(LocationDisplay.DataSourceStatusChangedEvent dataSourceStatusChangedEvent) {

                // If LocationDisplay started OK, then continue.
                if (dataSourceStatusChangedEvent.isStarted())
                    return;

                // No error is reported, then continue.
                if (dataSourceStatusChangedEvent.getError() == null)
                    return;

                // If an error is found, handle the failure to start.
                // Check permissions to see if failure may be due to lack of permissions.
                boolean permissionCheck1 = ContextCompat.checkSelfPermission(MainActivity.this, reqPermissions[0]) ==
                        PackageManager.PERMISSION_GRANTED;
                boolean permissionCheck2 = ContextCompat.checkSelfPermission(MainActivity.this, reqPermissions[1]) ==
                        PackageManager.PERMISSION_GRANTED;

                if (!(permissionCheck1 && permissionCheck2)) {
                    // If permissions are not already granted, request permission from the user.
                    ActivityCompat.requestPermissions(MainActivity.this, reqPermissions, requestCode);
                } else {
                    // Report other unknown failure types to the user - for example, location services may not
                    // be enabled on the device.
                    String message = String.format("Error in DataSourceStatusChangedListener: %s", dataSourceStatusChangedEvent
                            .getSource().getLocationDataSource().getError().getMessage());
                    Toast.makeText(MainActivity.this, message, Toast.LENGTH_LONG).show();

                    // Update UI to reflect that the location display did not actually start
                    mLocationDisplay.setAutoPanMode(LocationDisplay.AutoPanMode.RECENTER);
                    if (!mLocationDisplay.isStarted())
                        mLocationDisplay.startAsync();
                }
            }
        });

        // set an on touch listener to listen for click events
        mMapView.setOnTouchListener(new DefaultMapViewOnTouchListener(this, mMapView) {
            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                // remove any existing callouts
                if (mCallout.isShowing()) {
                    mCallout.dismiss();
                }
                // get the point that was clicked and convert it to a point in map coordinates
                final Point clickPoint = mMapView
                        .screenToLocation(new android.graphics.Point(Math.round(e.getX()), Math.round(e.getY())));
                // create a selection tolerance
                int tolerance = 10;
                double mapTolerance = tolerance * mMapView.getUnitsPerDensityIndependentPixel();
                // use tolerance to create an envelope to query
                Envelope envelope = new Envelope(clickPoint.getX() - mapTolerance, clickPoint.getY() - mapTolerance,
                        clickPoint.getX() + mapTolerance, clickPoint.getY() + mapTolerance, map.getSpatialReference());


                QueryParameters query = new QueryParameters();
                query.setGeometry(envelope);


                // request all available attribute fields


                final ListenableFuture<FeatureQueryResult> future = mServiceFeatureTable.queryFeaturesAsync(query, ServiceFeatureTable.QueryFeatureFields.LOAD_ALL);



                // add done loading listener to fire when the selection returns
                future.addDoneListener(new Runnable() {
                                           @Override
                                           public void run() {
                                               try {
                                                   //call get on the future to get the result
                                                   FeatureQueryResult result = future.get();
                                                   // create an Iterator
                                                   Iterator<Feature> iterator = result.iterator();
                                                   // create a TextView to display field values
                                                   TextView calloutContent = new TextView(getApplicationContext());
                                                   calloutContent.setTextColor(Color.BLACK);
                                                   calloutContent.setSingleLine(false);
                                                   calloutContent.setVerticalScrollBarEnabled(true);
                                                   calloutContent.setScrollBarStyle(View.SCROLLBARS_INSIDE_INSET);
                                                   calloutContent.setMovementMethod(new ScrollingMovementMethod());
                                                   calloutContent.setLines(5);
                                                   // cycle through selections
                                                   int counter = 0;
                                                   Feature feature;
                                                   while (iterator.hasNext()) {
                                                       feature = iterator.next();
                                                       // create a Map of all available attributes as name value pairs
                                                       Map<String, Object> attr = feature.getAttributes();
                                                       Set<String> keys = attr.keySet();
                                                       Log.e("asd","a"+keys.size());
                                                       int count = 0;



                                   /* if(!attr.get("foto").equals(null)){
                                    new DownloadImageTask((ImageView) findViewById(R.id.imageView))
                                            .execute((String) attr.get("foto"));

                                    }*/
                                                           //calloutContent.append(""key + " | " + attr.get("nombre") + "\n");
                                                           textoNombre.setText((String) attr.get("nombre"));
                                                           textoNombre.scrollTo(0,0);


                                                       calloutContent.append("Nombre "+ " : " + attr.get("nombre") + "\n"+
                                                               "Direccion  "+ " : " + attr.get("Match_addr") + "\n"+
                                                               "Horario  "+ " : " + attr.get("Horario") + "\n");
                                                       counter++;
                                                       // center the mapview on selected feature
                                                       Envelope envelope = feature.getGeometry().getExtent();
                                                       mMapView.setViewpointGeometryAsync(envelope, 200);
                                                       // show CallOut
                                                       mCallout.setLocation(clickPoint);
                                                       mCallout.setContent(calloutContent);
                                                       mCallout.show();
                                                   }
                                               } catch (Exception e) {
                                                   Log.e(getResources().getString(R.string.app_name), "Select feature failed: " + e.getMessage());
                                               }
                                           }
                                       }
                );








                return super.onSingleTapConfirmed(e);
            }
        });
















        mMapView.setOnTouchListener(new DefaultMapViewOnTouchListener(this, mMapView) {
            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                // remove any existing callouts
                if (mCallout.isShowing()) {
                    mCallout.dismiss();
                }
                // get the point that was clicked and convert it to a point in map coordinates
                final Point clickPoint = mMapView
                        .screenToLocation(new android.graphics.Point(Math.round(e.getX()), Math.round(e.getY())));
                // create a selection tolerance
                int tolerance = 10;
                double mapTolerance = tolerance * mMapView.getUnitsPerDensityIndependentPixel();
                // use tolerance to create an envelope to query
                Envelope envelope = new Envelope(clickPoint.getX() - mapTolerance, clickPoint.getY() - mapTolerance,
                        clickPoint.getX() + mapTolerance, clickPoint.getY() + mapTolerance, map.getSpatialReference());
                QueryParameters query = new QueryParameters();
                query.setGeometry(envelope);
                // request all available attribute fields
                final ListenableFuture<FeatureQueryResult> future = mServiceFeatureTable0
                        .queryFeaturesAsync(query, ServiceFeatureTable.QueryFeatureFields.LOAD_ALL);
                // add done loading listener to fire when the selection returns


                future.addDoneListener(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            //call get on the future to get the result
                            FeatureQueryResult result = future.get();
                            // create an Iterator
                            Iterator<Feature> iterator = result.iterator();
                            // create a TextView to display field values
                            TextView calloutContent = new TextView(getApplicationContext());
                            calloutContent.setTextColor(Color.BLACK);
                            calloutContent.setSingleLine(false);
                            calloutContent.setVerticalScrollBarEnabled(true);
                            calloutContent.setScrollBarStyle(View.SCROLLBARS_INSIDE_INSET);
                            calloutContent.setMovementMethod(new ScrollingMovementMethod());
                            calloutContent.setLines(5);
                            // cycle through selections
                            int counter = 0;
                            Feature feature;
                            while (iterator.hasNext()) {
                                feature = iterator.next();
                                // create a Map of all available attributes as name value pairs
                                Map<String, Object> attr = feature.getAttributes();
                                Set<String> keys = attr.keySet();
                                Log.e("asd","a"+keys.size());
                                int count = 0;

                                for (String key : keys) {

                                    count++;

                                    Object value = attr.get(key);
                                    // format observed field value as date
                                    if (value instanceof GregorianCalendar) {
                                        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MMM-yyyy", Locale.US);
                                        value = simpleDateFormat.format(((GregorianCalendar) value).getTime());
                                    }
                                    // append name value pairs to TextView


                                   /* if(!attr.get("foto").equals(null)){
                                    new DownloadImageTask((ImageView) findViewById(R.id.imageView))
                                            .execute((String) attr.get("foto"));

                                    }*/
                                    //calloutContent.append(""key + " | " + attr.get("nombre") + "\n");
                                    textoNombre.setText((String) attr.get("nombre"));
                                    textoNombre.scrollTo(0,0);

                                }
                                calloutContent.append("Nombre "+ " : " + attr.get("nombre") + "\n"+
                                        "Direccion  "+ " : " + attr.get("Match_addr") + "\n"+
                                        "Horario  "+ " : " + attr.get("Horario") + "\n");
                                counter++;
                                // center the mapview on selected feature
                                Envelope envelope = feature.getGeometry().getExtent();
                                mMapView.setViewpointGeometryAsync(envelope, 200);
                                // show CallOut
                                mCallout.setLocation(clickPoint);
                                mCallout.setContent(calloutContent);
                                mCallout.show();
                            }
                        } catch (Exception e) {
                            Log.e(getResources().getString(R.string.app_name), "Select feature failed: " + e.getMessage());
                        }
                    }
                });
                return super.onSingleTapConfirmed(e);
            }
        });

    }



    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        // If request is cancelled, the result arrays are empty.
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            // Location permission was granted. This would have been triggered in response to failing to start the
            // LocationDisplay, so try starting this again.
            mLocationDisplay.startAsync();
        } else {
            // If permission was denied, show toast to inform user what was chosen. If LocationDisplay is started again,
            // request permission UX will be shown again, option should be shown to allow never showing the UX again.
            // Alternative would be to disable functionality so request is not shown again.
            Toast.makeText(MainActivity.this, "Permiso de ubicacion denegado", Toast
                    .LENGTH_SHORT).show();

            // Update UI to reflect that the location display did not actually start
            mLocationDisplay.setAutoPanMode(LocationDisplay.AutoPanMode.RECENTER);
            if (!mLocationDisplay.isStarted())
                mLocationDisplay.startAsync();
        }
    }


    @Override
    protected void onPause() {
        super.onPause();
        mMapView.pause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mMapView.resume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mMapView.dispose();
    }




    public void onClickCalf(View v)
    {
        Toast.makeText(this, "Clicked on Button", Toast.LENGTH_LONG).show();
        Intent myIntent = new Intent(MainActivity.this, RankPageActivity.class);
        myIntent.putExtra("key", textoNombre.getText()); //Optional parameters
        MainActivity.this.startActivity(myIntent);

    }

    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;

        public DownloadImageTask(ImageView bmImage) {
            this.bmImage = bmImage;
        }

        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            Bitmap mIcon11 = null;
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return mIcon11;
        }

        protected void onPostExecute(Bitmap result) {
            bmImage.setImageBitmap(result);
        }
    }
}
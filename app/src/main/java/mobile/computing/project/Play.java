package mobile.computing.project;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.style.layers.PropertyFactory;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static com.mapbox.mapboxsdk.camera.CameraUpdateFactory.newCameraPosition;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconAllowOverlap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconOffset;

public class Play extends Activity  implements OnMapReadyCallback, MapboxMap.OnMapClickListener{

    public static final String BASE_URL="https://ewserver.di.unimi.it/mobicomp/mostri/";
    public static final String GET_MAP="getmap.php";
    public static final String GET_IMAGE="getimage.php";
    public static final String FIGHT_EAT="fighteat.php";

    private static final String SOURCE_ID = "SOURCE_ID";
    private static final String ICON_ID = "ICON_ID";
    private static final String LAYER_ID = "LAYER_ID";

    public Location ultimaPosizione;
    private MapView mapView;
    private MapboxMap mapboxMap;
    private FusedLocationProviderClient fusedLocationClient;

    public RequestQueue myRequestQueue = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Mapbox.getInstance(this, "pk.eyJ1IjoieWFzc2luZTk3IiwiYSI6ImNrMzVtZWFwMjA5MmEzZHFqdDRiNGExMzIifQ.gemPT-eRrkMTbxLOB_517w");
        setContentView(R.layout.activity_play);

        mapView = findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);

        //CHIEDO I PERMESSI PER USARE LA POSIZIONE

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    0);
        }

        mapView.getMapAsync(this);

        Button btnindietro=findViewById(R.id.button3);
        btnindietro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent vaiIndietro=new Intent(getApplicationContext(), MainActivity.class);
                startActivity(vaiIndietro);
            }
        });

    }


    //GESTIONE DEL PERMESSO DI LOCALIZZAZIONE
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case 0: {
                if (grantResults.length > 0 && grantResults[0] != PackageManager.PERMISSION_GRANTED){
                    Toast.makeText(this, "ATTENZIONE: devi fornire il permesso per utilizzare la localizzazione!", Toast.LENGTH_SHORT).show();
                    Intent tornaIndietro=new Intent(getApplicationContext(), MainActivity.class);
                    startActivity(tornaIndietro);
                }
            }
        }
    }


    public CameraPosition impostaPosizione(final double lat, final double lon, final boolean find){
                if(find){
                    CameraPosition position = new CameraPosition.Builder()
                            .target(new LatLng(lat, lon))
                            .zoom(17)
                            .tilt(50)
                            .bearing(0)
                            .build();
                    return position;
                } else {
                    CameraPosition position = new CameraPosition.Builder()
                            .target(new LatLng(lat, lon))
                            .zoom(0)
                            .tilt(0)
                            .bearing(0)
                            .build();
                    return position;
                }

            }

    @Override
    public void onMapReady(@NonNull final MapboxMap mapboxMap) {

        this.mapboxMap=mapboxMap;

        mapboxMap.setStyle(Style.MAPBOX_STREETS, new Style.OnStyleLoaded() {
                    @Override
                    public void onStyleLoaded(@NonNull Style style) {

                        List<Feature> symbolLayerIconFeatureList = new ArrayList<>();
                        ArrayList<Oggetto> oggetti = OggettiMappa.getInstance().getOggettiMappaList();

                        Log.d("Play","Sono dentro la funzione onMapReady.");
                        Log.d("Play","La dimensione dell'arraylist è: "+OggettiMappa.getInstance().getSize());
                        Log.d("Play","Arraylist :  "+oggetti);

                        for(int j=0; j<OggettiMappa.getInstance().getSize(); j++){

                            Oggetto obj=oggetti.get(j);
                            if(obj.getType()=="MO"){
                                Double lat=obj.getLat();
                                Log.d("Play","Latitudine: "+lat);
                                Double lon=obj.getLon();
                                Log.d("Play","Longitudine: "+lon);
                                symbolLayerIconFeatureList.add(Feature.fromGeometry(
                                        Point.fromLngLat(lon, lat)));
                            }
                        }

                        style.addSource(new GeoJsonSource(SOURCE_ID,
                                FeatureCollection.fromFeatures(symbolLayerIconFeatureList)));

                        style.addImage(ICON_ID, BitmapFactory.decodeResource(
                                Play.this.getResources(), R.drawable.monster_icon));

                        style.addLayer(new SymbolLayer(LAYER_ID, SOURCE_ID)
                                .withProperties(PropertyFactory.iconImage(ICON_ID),
                                        iconAllowOverlap(true),
                                        iconOffset(new Float[]{0f, -9f})));

                        //PRENDO L'ULTIMA POSIZIONE NOTA
                        fusedLocationClient = LocationServices.getFusedLocationProviderClient(Play.this);
                        fusedLocationClient.getLastLocation()
                                .addOnSuccessListener(Play.this, new OnSuccessListener<Location>() {
                                    @Override
                                    public void onSuccess(Location location) {
                                        // Got last known location. In some rare situations this can be null.
                                        Log.d("Play", "La posizione rilevata è la seguente: "+location);
                                        ultimaPosizione=location;
                                        if (location != null) {     //IN REALTÀ CI VANNO I LOCATION.GET()
                                            double lat=45.476677;//location.getLatitude();
                                            double lon=9.231477;//location.getLongitude();
                                            mapboxMap.animateCamera(newCameraPosition(
                                                    impostaPosizione(lat,lon,true)), 5000);
                                        } else {Log.d("Play","Nessuna posizione rilevata");
                                            mapboxMap.animateCamera(newCameraPosition(impostaPosizione
                                                            (45.476682, 9.231629, false)),
                                                    5000);
                                            Toast.makeText(getApplicationContext(), "Nessuna posizione rilevata", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                    }
                }
        );

    }

    @Override
    public void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();

        myRequestQueue= Volley.newRequestQueue(this);

        JSONObject jsonBody = new JSONObject();
        try {
            //vado a prendere il mio session_id dalle shared preferences
            SharedPreferences sharedPref = getApplicationContext().getSharedPreferences(
                    getString(R.string.preference_file_session_id), Context.MODE_PRIVATE);
            String ses_ID = sharedPref.getString(getString(R.string.preference_file_session_id), "");
            //metto il valore della session_id nella stringa della richiesta
            jsonBody.put("session_id", ses_ID);
            Log.d("Play","Ottimo, ho aggiunto l'id alla chiamata:"+ses_ID);
        } catch (JSONException e) {
            e.printStackTrace();
            Log.d("Play","Male, NON ho aggiunto l'id alla chiamata.");
        }


        JsonObjectRequest getMap_Request = new JsonObjectRequest
                (Request.Method.POST, BASE_URL + GET_MAP, jsonBody, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        //aggiungo ciò che ho trovato nel model, però
                        OggettiMappa.getInstance().populate(response);
                        Log.d("Play", "Bene, ho chiesto la mappa");

                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // TODO: Handle error
                        Log.d("Play", "Non sono riuscito a fare la richiesta, qualcosa è andato storto :(");
                    }
                });

        myRequestQueue.add(getMap_Request);

        mapView.getMapAsync(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    protected void createLocationRequest() {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setInterval(10000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        //ATTENZIONE !!!!!!!!!!!  FORSE QUESTA PARTE NON SERVE NEMMENO
        /*
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);

        SettingsClient client = LocationServices.getSettingsClient(this);
        Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());

        //SETTAGGI DEL DEVICE PERFETTI
        task.addOnSuccessListener(this, new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                // All location settings are satisfied. The client can initialize
                // location requests here.

            }
        });

        //SETTAGGI DEL DEVICE NON OTTIMI
        task.addOnFailureListener(this, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if (e instanceof ResolvableApiException) {
                    // Location settings are not satisfied, so we show to the user a dialog.
                    try {
                        // Show the dialog by calling startResolutionForResult(),
                        // and check the result in onActivityResult().
                        ResolvableApiException resolvable = (ResolvableApiException) e;
                        resolvable.startResolutionForResult(Play.this, 0);
                    } catch (IntentSender.SendIntentException sendEx) {
                        // Ignore the error.
                        Log.d("Play","Errore bellamente ignorato");
                    }
                }
            }
        });

         */

    }


    @Override
    public boolean onMapClick(@NonNull LatLng point) {
        return false;
    }
}

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

    private static final String LAYER_MOSTRI = "LAYER_MOSTRI";
    private static final String LAYER_CARAMELLE = "LAYER_CARAMELLE";

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

        //GESTIONE BOTTONE INDIETRO
        Button btnindietro=findViewById(R.id.button3);
        btnindietro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent vaiIndietro=new Intent(getApplicationContext(), MainActivity.class);
                startActivity(vaiIndietro);
            }
        });
    }



    @Override
    public void onMapReady(@NonNull final MapboxMap mapboxMap) {

        this.mapboxMap=mapboxMap;

        mapboxMap.setStyle(Style.MAPBOX_STREETS, new Style.OnStyleLoaded() {
                    @Override
                    public void onStyleLoaded(@NonNull Style style) {

                        //POSIZIONO LE ICONE SULLA MAPPA
                        List<Feature> symbolLayerMonsterFeatureList = new ArrayList<>();
                        List<Feature> symbolLayerCandyFeatureList = new ArrayList<>();
                        ArrayList<Oggetto> oggetti = OggettiMappa.getInstance().getOggettiMappaList();

                        Log.d("Play","La dimensione dell'arraylist è: "+OggettiMappa.getInstance().getSize());

                        for(int j=0; j<OggettiMappa.getInstance().getSize(); j++){
                            Oggetto obj=oggetti.get(j);
                            String tipoObj=obj.getType();
                            Double lat=obj.getLat();
                            Double lon=obj.getLon();

                            if(tipoObj.equals("MO")){
                                symbolLayerMonsterFeatureList.add(Feature.fromGeometry(Point.fromLngLat(lon, lat)));
                            }else{
                                symbolLayerCandyFeatureList.add(Feature.fromGeometry(Point.fromLngLat(lon, lat)));
                            }
                        }

                        // Add the SymbolLayer icon image to the map style
                        style.addSource(new GeoJsonSource("SOURCEMOSTRI_ID",
                                FeatureCollection.fromFeatures(symbolLayerMonsterFeatureList)));

                        style.addSource(new GeoJsonSource("SOURCECANDY_ID",
                                FeatureCollection.fromFeatures(symbolLayerCandyFeatureList)));

                        // Adding a GeoJson source for the SymbolLayer icons
                        style.addImage("ICONMOSTRI_ID", BitmapFactory.decodeResource(
                                Play.this.getResources(), R.drawable.monster_icon));

                        style.addImage("ICONCANDY_ID", BitmapFactory.decodeResource(
                                Play.this.getResources(), R.drawable.candy_icon));



                        // Adding the actual SymbolLayer to the map style. An offset is added that the bottom of the red
                        // marker icon gets fixed to the coordinate, rather than the middle of the icon being fixed to
                        // the coordinate point. This is offset is not always needed and is dependent on the image
                        // that you use for the SymbolLayer icon.
                        style.addLayer(new SymbolLayer(LAYER_MOSTRI, "SOURCEMOSTRI_ID")
                                .withProperties(PropertyFactory.iconImage("ICONMOSTRI_ID"),
                                        iconAllowOverlap(true),
                                        iconOffset(new Float[]{0f, -9f})));

                        style.addLayer(new SymbolLayer(LAYER_CARAMELLE, "SOURCECANDY_ID")
                                .withProperties(PropertyFactory.iconImage("ICONCANDY_ID"),
                                        iconAllowOverlap(true),
                                        iconOffset(new Float[]{0f, -9f})));


                        //PRENDO L'ULTIMA POSIZIONE NOTA E MI POSIZIONO LÌ
                        fusedLocationClient = LocationServices.getFusedLocationProviderClient(Play.this);
                        fusedLocationClient.getLastLocation()
                                .addOnSuccessListener(Play.this, new OnSuccessListener<Location>() {
                                    @Override
                                    public void onSuccess(Location location) {
                                        // Got last known location. In some rare situations this can be null.
                                        Log.d("Play", "La posizione rilevata è la seguente: "+location);
                                        ultimaPosizione=location;
                                        if (location != null) {     //IN REALTÀ CI VANNO I LOCATION.GET()
                                            double lat=location.getLatitude();
                                            double lon=location.getLongitude();
                                            mapboxMap.animateCamera(newCameraPosition(
                                                    impostaPosizione(lat,lon,true)), 5000);
                                        } else {Log.d("Play","Nessuna posizione rilevata");
                                            mapboxMap.animateCamera(newCameraPosition(impostaPosizione
                                                            (45.472806, 9.182028, false)),
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
    public void onResume() {
        super.onResume();
        mapView.onResume();

        //CHIEDO AL SERVER QUALI SONO GLI OGGETTI PRESENTI NELLA MAPPA E LI SALVO
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
            Log.d("Play","Male, NON sono riuscito ad aggiungere l'id alla chiamata.");
        }

        JsonObjectRequest getMap_Request = new JsonObjectRequest
                (Request.Method.POST, BASE_URL + GET_MAP, jsonBody, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        //aggiungo ciò che ho trovato nel model, però
                        OggettiMappa.getInstance().populate(response);
                        Log.d("Play", "Bene, ho chiesto la mappa");
                        mapView.getMapAsync(Play.this);
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("Play", "Non sono riuscito a fare la richiesta, qualcosa è andato storto.");
                    }
                });

        myRequestQueue.add(getMap_Request);
    }

    @Override
    public void onStart() {
        super.onStart();
        mapView.onStart();
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

    //SETTO LA POSIZIONE DELLA CAMERA
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
    public boolean onMapClick(@NonNull LatLng point) {
        return false;
    }
}

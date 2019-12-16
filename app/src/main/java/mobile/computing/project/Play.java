package mobile.computing.project;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PointF;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.mapbox.android.core.permissions.PermissionsListener;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.location.LocationComponent;
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions;
import com.mapbox.mapboxsdk.location.LocationComponentOptions;
import com.mapbox.mapboxsdk.location.OnCameraTrackingChangedListener;
import com.mapbox.mapboxsdk.location.OnLocationClickListener;
import com.mapbox.mapboxsdk.location.modes.CameraMode;
import com.mapbox.mapboxsdk.location.modes.RenderMode;
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

public class Play extends Activity implements OnMapReadyCallback, OnLocationClickListener, OnCameraTrackingChangedListener, PermissionsListener {

    public static final String BASE_URL="https://ewserver.di.unimi.it/mobicomp/mostri/";
    public static final String GET_MAP="getmap.php";
    public static final String GET_IMAGE="getimage.php";
    private static final String LAYER_MOSTRI = "LAYER_MOSTRI";
    private static final String LAYER_CARAMELLE = "LAYER_CARAMELLE";
    public String immBase64= "";
    public Location ultimaPosizione;
    private MapView mapView;
    private MapboxMap mapboxMap;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationComponent locationComponent;
    private boolean isInTrackingMode;
    int primaVolta=-1;
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
    public void onResume(){
        super.onResume();
        mapView.onResume();
        richiestaMappa();
    }

    //METODO CHE RICHIEDE I DATI DELLA MAPPA AL SERVER
    public void richiestaMappa(){
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
        } catch (JSONException e) {
            e.printStackTrace();
            Log.d("Play","Male, NON sono riuscito ad aggiungere l'id alla chiamata.");
        }

        JsonObjectRequest getMap_Request = new JsonObjectRequest
                (Request.Method.POST, BASE_URL + GET_MAP, jsonBody, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        OggettiMappa.getInstance().svuota();
                        OggettiMappa.getInstance().populate(response);
                        Log.d("Play", "Ho chiesto la mappa");
                        mapView.getMapAsync(Play.this);
                        Log.d("Play", "Ho richiamato il metodo on MapReady");
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("Play", "Non sono riuscito a fare la richiesta, qualcosa è andato storto.");
                    }
                });

        myRequestQueue.add(getMap_Request);
    }

    //IMPOSTO LE ICONE SULLA MAPPA
    @Override
    public void onMapReady(@NonNull final MapboxMap mapboxMap) {
        this.mapboxMap=mapboxMap;
        Log.d("Play", "È stato  richiamato il metodo onMapReady");
        mapboxMap.setStyle(Style.MAPBOX_STREETS, new Style.OnStyleLoaded() {
                    @Override
                    public void onStyleLoaded(@NonNull Style style) {

                        //INSERISCO I DATI DI XP E LP
                        infoUtente();

                        primaVolta++;
                        Log.d("Play", "Il valore di primaVolta è: "+primaVolta);
                        //VADO A POSIZIONARMI SULLA MAPPA
                        if(primaVolta==0){
                            Log.d("Play", "Sono entrato nell'IF");
                            doveSono();
                        }

                        //POSIZIONO LE ICONE SULLA MAPPA
                        mettiIcone(style);

                        //METTO L'ICONA DELLA POSIZIONE ATTUALE DELL'UTENTE
                        if(primaVolta==0){
                            Log.d("Play", "Sono entrato nell'IF");
                            enableLocationComponent(style);
                        }

                    }
                }
        );

        mapboxMap.addOnMapClickListener(    new MapboxMap.OnMapClickListener() {
            @Override
            public boolean onMapClick(@NonNull LatLng point) {
                clickIcona(point);
                return true;
            }
        });
    }

    //PRENDO LP E XP DA METTERE NELLA SCHERMATA PLAY
    public void infoUtente(){
        SharedPreferences sharedPref = getApplicationContext().getSharedPreferences(
                getString(R.string.preference_file_session_id), Context.MODE_PRIVATE);
        String sessionId = sharedPref.getString(getString(R.string.preference_file_session_id), "");
        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("session_id", sessionId);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        JsonObjectRequest getProfile_Request = new JsonObjectRequest("https://ewserver.di.unimi.it/mobicomp/mostri/getprofile.php",
                jsonBody,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        TextView tvxp=findViewById(R.id.textView7);
                        TextView tvlp=findViewById(R.id.textView8);
                        User u= new User(response);
                        ImageView profileImage= findViewById(R.id.playButton);
                        tvxp.setText("  XP: "+u.getXP()+"  ");
                        tvlp.setText("  LP: "+u.getLP()+"  ");
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast toast = Toast.makeText(getApplicationContext(), "Richiesta fallita", Toast.LENGTH_SHORT);
                        toast.show();
                    }
                }
        );
        requestQueue.add(getProfile_Request);
    }

    //TROVO E IMPOSTO POSIZIONE ATTUALE USER
    public void doveSono(){
        //PRENDO L'ULTIMA POSIZIONE NOTA E MI POSIZIONO LÌ
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(Play.this);
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(Play.this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // Got last known location. In some rare situations this can be null.
                        ultimaPosizione=location;
                        if (location != null) {
                            Log.d("Play", "Ultima posizione rilevata");
                            double lat=location.getLatitude();
                            double lon=location.getLongitude();
                            mapboxMap.animateCamera(newCameraPosition(
                                    impostaPosizione(lat,lon,true)), 2000);//true vuole dire che la posizione che setto è quella dell'utente, quindi le impostazioni della camera saranno diverse
                        } else {Log.d("Play","Nessuna posizione rilevata!");
                            mapboxMap.animateCamera(newCameraPosition(impostaPosizione
                                            (45.464188, 9.190639, false)), //false vuole dire che la posizione che setto non è quella dell'utente, quindi le impostazioni della camera saranno diverse
                                    2000);
                            Toast.makeText(getApplicationContext(), "Nessuna posizione rilevata, assicurarsi" +
                                    " di aver attivato il GPS.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    //POSIZIONO LE ICONE SULLA MAPPA
    public void mettiIcone(Style style){

        List<Feature> symbolLayerMonsterFeatureList = new ArrayList<>();
        List<Feature> symbolLayerCandyFeatureList = new ArrayList<>();
        ArrayList<Oggetto> oggetti = OggettiMappa.getInstance().getOggettiMappaList();

        for(int j=0; j<OggettiMappa.getInstance().getSize(); j++){
            Oggetto obj=oggetti.get(j);
            String tipoObj=obj.getType();
            double lat=obj.getLat();
            double lon=obj.getLon();

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
        style.addLayer(new SymbolLayer(LAYER_CARAMELLE, "SOURCECANDY_ID")
                .withProperties(PropertyFactory.iconImage("ICONCANDY_ID"),
                        iconAllowOverlap(true),
                        iconOffset(new Float[]{0f, -9f})));

        style.addLayer(new SymbolLayer(LAYER_MOSTRI, "SOURCEMOSTRI_ID")
                .withProperties(PropertyFactory.iconImage("ICONMOSTRI_ID"),
                        iconAllowOverlap(true),
                        iconOffset(new Float[]{0f, -9f})));
    }

    //METTO L'ICONA DELLA POSIZIONE ATTUALE DELL'UTENTE
    @SuppressWarnings( {"MissingPermission"})
    private void enableLocationComponent(@NonNull Style loadedMapStyle) {

        // Create and customize the LocationComponent's options
        LocationComponentOptions customLocationComponentOptions = LocationComponentOptions.builder(this)
                .layerBelow(LAYER_MOSTRI)
                .bearingTintColor(Color.CYAN)
                .accuracyAlpha(.3f)
                .accuracyColor(Color.CYAN)
                .build();

        // Get an instance of the component
        locationComponent = mapboxMap.getLocationComponent();

        LocationComponentActivationOptions locationComponentActivationOptions =
                LocationComponentActivationOptions.builder(this, loadedMapStyle)
                        .locationComponentOptions(customLocationComponentOptions)
                        .useDefaultLocationEngine(true)
                        .build();

        // Activate with options
        locationComponent.activateLocationComponent(locationComponentActivationOptions);

        // Enable to make component visible
        locationComponent.setLocationComponentEnabled(true);

        // Set the component's camera mode
        locationComponent.setCameraMode(CameraMode.TRACKING_COMPASS);

        // Set the component's render mode
        locationComponent.setRenderMode(RenderMode.COMPASS);

        // Add the location icon click listener
        locationComponent.addOnLocationClickListener(this);

        // Add the camera tracking listener. Fires if the map camera is manually moved.
        locationComponent.addOnCameraTrackingChangedListener(this);

        Button you = findViewById(R.id.buttonYou);

        you.setOnClickListener (new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                doveSono();
                locationComponent.setCameraMode(CameraMode.TRACKING);
            }
        });
    }

    //QUANDO CLICCO SU UNA ICONA
    public void clickIcona(@NonNull LatLng point) {

        PointF screenPoint = mapboxMap.getProjection().toScreenLocation(point);
        List<Feature> featuresMostri = mapboxMap.queryRenderedFeatures(screenPoint, LAYER_MOSTRI);
        List<Feature> featuresCandy = mapboxMap.queryRenderedFeatures(screenPoint, LAYER_CARAMELLE);

        double latm=-1;
        double lonm=-1;
        double latc=-1;
        double lonc=-1;

        if (!featuresMostri.isEmpty() && featuresCandy.isEmpty()) {
            Feature selectedFeature = featuresMostri.get(0);
            Point position = (Point) selectedFeature.geometry();
            latm = position.latitude();
            lonm = position.longitude();
        }
        if (!featuresCandy.isEmpty() && featuresMostri.isEmpty()) {
            Feature selectedFeature = featuresCandy.get(0);
            Point position = (Point) selectedFeature.geometry();
            latc = position.latitude();
            lonc = position.longitude();
        }
        if (!featuresCandy.isEmpty() && !featuresMostri.isEmpty()) {
            Feature selectedFeature = featuresCandy.get(0);
            Point position1 = (Point) selectedFeature.geometry();
            Feature selectedFeature2 = featuresCandy.get(0);
            Point position2 = (Point) selectedFeature2.geometry();
            latm = position1.latitude();
            lonm = position1.longitude();
            latc = position2.latitude();
            lonc = position2.longitude();
        }
        if (featuresCandy.isEmpty() && featuresMostri.isEmpty())    return;

        //prendo gli oggetti che ci sono sulla mappa
        ArrayList<Oggetto> objs=OggettiMappa.getInstance().getOggettiMappaList();

        //cerco l'id dell'oggetto cliccato perchè serve per la chiamata
        int nOggetto=-1;
        int posizione=-1;
        double temp = Math.pow(10, 4);
        latm=Math.round(latm * temp) / temp;
        lonm=Math.round(lonm * temp) / temp;
        latc=Math.round(latc * temp) / temp;
        lonc=Math.round(lonc * temp) / temp;
        for(int i=0; i<objs.size(); i++){
            Oggetto obj=objs.get(i);
            if((obj.getLat()==latm && obj.getLon()==lonm)||(obj.getLat()==latc && obj.getLon()==lonc)){
                nOggetto=obj.getId();
                posizione=i;
                break;
            }
        }

        //Se NON trovo nessun oggetto con quell'ID
        if(nOggetto==-1 || objs.get(posizione).getId()!=nOggetto) {
            Toast.makeText(getApplicationContext(), "L'oggetto è scappato ! Continua a cercare ...", Toast.LENGTH_SHORT).show();
            return;
        }
        if(nOggetto!=-1 || objs.get(posizione).getId()==nOggetto) { //Se trovo un oggetto con quell'ID
            //FACCIO LA CHIAMATA PER PRENDERE L'IMMAGINE
            richiediImgOggetto(nOggetto, posizione);
            return;
        }

    }

    //GESTIONE DEL PERMESSO DI LOCALIZZAZIONE
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
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
                    .zoom(14)
                    .tilt(50)
                    .build();
            return position;
        } else {
            CameraPosition position = new CameraPosition.Builder()
                    .target(new LatLng(lat, lon))
                    .zoom(11)
                    .build();
            return position;
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    /*protected void createLocationRequest() {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setInterval(10000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        //ATTENZIONE !!!!!!!!!!!  FORSE QUESTA PARTE NON SERVE NEMMENO

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);

        SettingsClient client = LocationServices.getSettingsClient(this);
        Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());

        //SETTAGGI DEL DEVICE OTTIMI
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



    }*/ //È DA FARE?

    //RICHIEDI INFORMAZIONI DI UN OGGETTO SPECIFICO
    public void richiediImgOggetto(final int numeroOggetto, final int posizione){
        //prendo il mio session id perchè serve per la chiamata
        SharedPreferences sharedPref = getApplicationContext().getSharedPreferences(
                getString(R.string.preference_file_session_id), Context.MODE_PRIVATE);
        String sessionId = sharedPref.getString(getString(R.string.preference_file_session_id), "");

        //sono pronto per fare la chiamata
        myRequestQueue= Volley.newRequestQueue(this);
        JSONObject jsonBody = new JSONObject();
        try {
            //metto il valore della session_id e del target_id nella stringa della richiesta
            jsonBody.put("session_id", sessionId+"");
            jsonBody.put("target_id", numeroOggetto+"");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest getImage_Request = new JsonObjectRequest
                (Request.Method.POST, BASE_URL + GET_IMAGE, jsonBody, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            immBase64=response.getString("img");
                            mostraOggetto(numeroOggetto, posizione);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("Play", "Non sono riuscito a fare la richiesta, qualcosa è andato storto.");
                    }
                });

        myRequestQueue.add(getImage_Request);
    }

    //MOSTRA LA SCHERMATA DELLE INFORMAZIONI DEL MOSTRO
    public void mostraOggetto(int nOggetto, int posizione) {
        ArrayList<Oggetto> objs=OggettiMappa.getInstance().getOggettiMappaList();
        Oggetto obj=objs.get(posizione);
        Intent apriInfo=new Intent(Play.this, infoOggetto.class);

        apriInfo.putExtra("id", nOggetto+"");
        apriInfo.putExtra("tipo", obj.getType());
        double cifre = Math.pow(10, 4);
        apriInfo.putExtra("lat", Double.toString(Math.round(obj.getLat() * cifre) / cifre));
        apriInfo.putExtra("lon", Double.toString(Math.round(obj.getLon() * cifre) / cifre));
        apriInfo.putExtra("size", obj.getSize());
        apriInfo.putExtra("nome", obj.getName());
        apriInfo.putExtra("img", immBase64);
        startActivity(apriInfo);
    }

    @Override
    public void onCameraTrackingDismissed() {
        isInTrackingMode = false;
    }

    @Override
    public void onCameraTrackingChanged(int currentMode) {
        //METODO RICHIESTO, MA NON CI INTERESSA
    }

    @Override
    public void onExplanationNeeded(List<String> permissionsToExplain) {
        //nessuna spiegazione... non annoiamo l'utente ;)
    }

    @Override
    public void onPermissionResult(boolean granted) {
        //L'UNICO CASO L'ABBIAMO GIÀ GESTITO SOPRA, IN UN'ALTRA FUNZIONE MOLTO SIMILE
        //QUESTO È ANCORA DA FARE
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
    public void onLocationComponentClick() {
    }
}

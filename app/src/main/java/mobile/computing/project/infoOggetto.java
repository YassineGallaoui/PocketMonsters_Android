package mobile.computing.project;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.snackbar.Snackbar;
import com.mapbox.turf.TurfMeasurement;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Objects;

public class infoOggetto extends Activity {

    public static final String BASE_URL="https://ewserver.di.unimi.it/mobicomp/mostri/";
    public static final String FIGHT_EAT="fighteat.php";
    public RequestQueue myRequestQueue = null;
    String id;
    String tipo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info_oggetto);
        DisplayMetrics dm=new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        int width=dm.widthPixels;
        int height=dm.heightPixels;
        getWindow().setLayout((int)(width*.9), (int)(height*.8));

        Bundle extras = getIntent().getExtras();

        assert extras != null;
        id= extras.getString("id");
        Log.d("infoOggetto","Mi viene passato correttamente l'id, che è: "+id);
        final String imgBase64= extras.getString("img");
        byte[] decodedString = Base64.decode(imgBase64, Base64.DEFAULT);
        Bitmap decodedImg = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);

        String nome= extras.getString("nome");
        tipo= extras.getString("tipo");
        String size= extras.getString("size");
        double latitudine= Double.valueOf(Objects.requireNonNull(extras.getString("lat")));
        double longitudine= Double.valueOf(Objects.requireNonNull(extras.getString("lon")));
        com.mapbox.geojson.Point posO= com.mapbox.geojson.Point.fromLngLat(longitudine, latitudine);
        double latU= Double.valueOf(Objects.requireNonNull(extras.getString("latU")));
        double lonU= Double.valueOf(Objects.requireNonNull(extras.getString("lonU")));
        com.mapbox.geojson.Point posU = com.mapbox.geojson.Point.fromLngLat(lonU, latU);

        ImageView imgView = findViewById(R.id.imageView);
        imgView.setImageBitmap(decodedImg);
        TextView nomeOggetto=findViewById(R.id.textView);
        nomeOggetto.setText(nome);
        TextView dimensione=findViewById(R.id.textView2);
        TextView description=findViewById(R.id.textView9);
        TextView description2=findViewById(R.id.textView11);
        if(size.equals("S")){
            dimensione.setText("Dimensione: S");
            if(tipo.equals("CA"))
                description.setText("Puoi guadagnare da 0 a 50 LP");
            else
                description.setText("Puoi guadagnare 1 XP");
                description2.setText("... o perdere da 0 a 50 LP");
        }

        if(size.equals("M")){
            dimensione.setText("Dimensione: M");
            if(tipo.equals("CA"))
                description.setText("Puoi guadagnare da 25 a 75 LP");
            else
                description.setText("Puoi guadagnare 3 XP");
                description2.setText("... o perdere da 25 a 75 LP");
        }

        if(size.equals("L")){
            dimensione.setText("Dimensione: L");
            if(tipo.equals("CA"))
                description.setText("Puoi guadagnare da 50 a 100 LP");
            else
                description.setText("Puoi guadagnare 10 XP");
                description2.setText("... o perdere da 50 a 100 LP");
        }


        Button lascia=findViewById(R.id.button6);
        if(tipo.equals("MO")){
            lascia.setText(" Scappa! ");
        } else lascia.setText(" Non ho fame ");
        lascia.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        Button azione=findViewById(R.id.button7);
        if(tipo.equals("MO")){
            azione.setText(" Lotta! ");
        } else azione.setText(" Mangia! ");

        Log.d("infoOggetto","posizione dell'oggetto: "+latitudine+ ", "+longitudine);
        Log.d("infoOggetto","Distanza rilevata: "+TurfMeasurement.distance( posO, posU));

            //SE L'OGGETTO DI INTERESSE È LONTANO MASSIMO 50 METRI, ALLORA POSSO FARE FIGHTEAT, ALTRIMENTI NO
        if(TurfMeasurement.distance( posO, posU)>0.05){ //QUESTO ANDRÀ CAMBIATO IN MAGGIORE!!
            //azione.setEnabled(false);
            azione.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //Toast.makeText(getApplicationContext(), "Sei troppo lento!", Toast.LENGTH_LONG).show();
                    Snackbar.make(findViewById(android.R.id.content), "Sei troppo lontano!", Snackbar.LENGTH_LONG).show();
                }
            });
        } else {
            //SE L'OGGETTO DI INTERESSE È LONTANO MASSIMO 50 METRI, ALLORA POSSO FARE FIGHTEAT, ALTRIMENTI NO
            azione.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //prendo il mio session id perchè serve per la chiamata
                    SharedPreferences sharedPref = getApplicationContext().getSharedPreferences(
                            getString(R.string.preference_file_session_id), Context.MODE_PRIVATE);
                    String sessionId = sharedPref.getString(getString(R.string.preference_file_session_id), "");

                    //sono pronto per fare la chiamata
                    myRequestQueue= Volley.newRequestQueue(infoOggetto.this);
                    JSONObject jsonBody = new JSONObject();
                    try {
                        //metto il valore della session_id e del target_id nella stringa della richiesta
                        jsonBody.put("session_id", sessionId);
                        jsonBody.put("target_id", id);
                        Log.d("infoOggetto","OK, ho preso il session ID "+sessionId+"e il target ID"+id);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    JsonObjectRequest getResults_Request = new JsonObjectRequest
                            (Request.Method.POST, BASE_URL + FIGHT_EAT, jsonBody, new Response.Listener<JSONObject>() {

                                @Override
                                public void onResponse(JSONObject response) {
                                    try {
                                        Log.d("infoOggetto","Ora inizio a fare la richiesta di fightEat()");
                                        Log.d("infoOggetto","RISULTATI: "+response.getString("died")
                                                +", "+response.getString("lp")+", "+response.getString("xp"));

                                        Intent getResults=new Intent(infoOggetto.this, infoRisultato.class);
                                        getResults.putExtra("img", imgBase64);
                                        getResults.putExtra("life", response.getString("died"));
                                        getResults.putExtra("type", tipo);
                                        getResults.putExtra("lp",response.getString("lp"));
                                        getResults.putExtra("xp",response.getString("xp"));
                                        startActivity(getResults);
                                        finish();
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

                    myRequestQueue.add(getResults_Request);
                }
            });
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
    }

}

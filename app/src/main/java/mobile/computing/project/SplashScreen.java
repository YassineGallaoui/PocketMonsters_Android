package mobile.computing.project;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;

import androidx.appcompat.app.AlertDialog;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

public class SplashScreen extends Activity {

    private ImageView miaImmagine;
    public RequestQueue myRequestQueue = null;
    public static final String BASE_URL = "https://ewserver.di.unimi.it/mobicomp/mostri/";
    public static final String REGISTER="register.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        miaImmagine = (ImageView) findViewById(R.id.splashImageView);
        miaImmagine.setImageResource(R.mipmap.splash_image);

        if(checkConnection()){
            Thread timerThread = new Thread() {
                public void run() {
                    try {
                            Log.d("SplashScreen", "Sei già connesso alla rete");
                            iscrizione();
                            sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } finally {
                        Intent vaiAlGioco=new Intent(getApplicationContext(), MainActivity.class);
                        startActivity(vaiAlGioco);
                    }
                }
            };
            timerThread.start();
        } else {
            finestraAlert();
        }
    }

    public boolean checkConnection(){
        ConnectivityManager connectivityManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        return (connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED);
    }

    public void finestraAlert(){
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(SplashScreen.this);
        //alertDialogBuilder.setTitle("C'è un problema ...");
        //alertDialogBuilder.setMessage("Nessuna connessione Internet rilevata.");
        alertDialogBuilder.setCancelable(false);
        // set the custom layout
        final View customLayout = getLayoutInflater().inflate(R.layout.custom_alert_dialog, null);
        alertDialogBuilder.setView(customLayout);
        Button btn=customLayout.findViewById(R.id.retry);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(checkConnection()){
                    iscrizione();
                    Intent vaiAlGioco=new Intent(getApplicationContext(), MainActivity.class);
                    startActivity(vaiAlGioco);
                } else {
                    final Animation animShake = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.shake);
                    v.startAnimation(animShake);
                }
            }
        });
        AlertDialog dialog = alertDialogBuilder.create();
        dialog.show();
    }

    //DEVO AVERE UN SESSION_ID
    public void iscrizione(){
        myRequestQueue= Volley.newRequestQueue(this);
        //Controllo se nelle preferences ho già un session ID, nel caso non faccio la richiesta :)
        SharedPreferences sharedPref = getApplicationContext().getSharedPreferences(
                getString(R.string.preference_file_session_id), Context.MODE_PRIVATE);
        String ses_ID = sharedPref.getString(getString(R.string.preference_file_session_id), "");
        if(ses_ID!=""){
            Log.d("MainActivity", "Session ID già presente, non faccio la chiamata per richiederne un altro.");
            Log.d("MainActivity", "Session ID : "+ses_ID);
        } else {
            Log.d("MainActivity", "Session ID NON presente, faccio la chiamata per chiederne uno.");
            JsonObjectRequest register_Request = new JsonObjectRequest
                    (Request.Method.POST, BASE_URL + REGISTER, null, new Response.Listener<JSONObject>() {

                        @Override
                        public void onResponse(JSONObject response) {
                            Log.d("Main Activity", "Bene, ho creato la richiesta");

                            //qua devo salvare il file nelle shared preferences, quindi lo faccio subito:
                            SharedPreferences sharedPref = getApplicationContext().getSharedPreferences(
                                    getString(R.string.preference_file_session_id), Context.MODE_PRIVATE);
                            String session_ID;
                            try {
                                session_ID = response.getString("session_id");
                                Log.d("MainActivity", "Il nuovo Session_ID ottenuto è il seguente: "+session_ID);
                            } catch (JSONException e) {
                                session_ID = "Attenzione! Non sono riuscito a leggere bene il session_ID, c'è stato qualche problema";
                                e.printStackTrace();
                            }
                            SharedPreferences.Editor editor = sharedPref.edit();
                            editor.putString(getString(R.string.preference_file_session_id), session_ID);
                            editor.commit();

                        }
                    }, new Response.ErrorListener() {

                        @Override
                        public void onErrorResponse(VolleyError error) {
                            // TODO: Handle error
                            Log.d("Main Activity", "Non sono riuscito a fare la richiesta, qualcosa è andato storto :(");
                        }
                    });

            myRequestQueue.add(register_Request);
        }
    }
}
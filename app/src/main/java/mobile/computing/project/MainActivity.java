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
import android.widget.Button;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends Activity {
    public static final String BASE_URL="https://ewserver.di.unimi.it/mobicomp/mostri/";
    public static final String REGISTER="register.php";

    public RequestQueue myRequestQueue = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button profilo=findViewById(R.id.profileButton);
        profilo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent vaiAlProfilo= new Intent(getApplicationContext(), Profilo.class);
                startActivity(vaiAlProfilo);
            }
        });

        Button gioca=findViewById(R.id.userImage);
        gioca.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //CONTROLLO SE INTERNET È ACCESO
                ConnectivityManager connectivityManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
                if(connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                        connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED) {
                    //SIAMO CONNESSI A UNA QUALCHE RETE
                    Intent vaiAlGioco=new Intent(getApplicationContext(), Play.class);
                    startActivity(vaiAlGioco);
                }
                else{
                    //NON SIAMO CONNESSI A NESSUNA RETE
                    Toast.makeText(getApplicationContext(), "È necessaria una connessione a Internet attiva", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();

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

package mobile.computing.project;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

public class Profilo extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profilo);
    }

    protected void onResume() {
        super.onResume();
        final String sessionId = "ZfyZbWCaGvmgzf0A";

        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("session_id", sessionId);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        RequestQueue requestQueue = Volley.newRequestQueue(this);

        JsonObjectRequest getProfileRequest = new JsonObjectRequest("https://ewserver.di.unimi.it/mobicomp/mostri/getprofile.php",
                jsonBody,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("Example5", response.toString());
                        getProfileResponse(response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast toast = Toast.makeText(getApplicationContext(), "richiesta fallita", Toast.LENGTH_SHORT);
                        toast.show();
                    }
                }
        );

        requestQueue.add(getProfileRequest);
    }

    public void getProfileResponse(JSONObject response){


    }

}


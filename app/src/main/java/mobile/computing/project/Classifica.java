package mobile.computing.project;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

public class Classifica extends Activity {
    public RequestQueue rankRequesteQueue=null;
    public static final String URL= "https://ewserver.di.unimi.it/mobicomp/mostri/";
    public static final String RANKING="ranking.php";

    @Override
    protected void onCreate(Bundle savedIstanceState){
        super.onCreate(savedIstanceState);
        setContentView(R.layout.activity_classifica);
        RecyclerView list=findViewById(R.id.list);
        list.setLayoutManager(new LinearLayoutManager(this));
        final UserAdapter userAdapter= new UserAdapter(this, this, UserModel.getInstance().getRanking());
        list.setAdapter(userAdapter);
        rankRequesteQueue= Volley.newRequestQueue(this);

        SharedPreferences sharedPref = getApplicationContext().getSharedPreferences(
                getString(R.string.preference_file_session_id), Context.MODE_PRIVATE);
        String sessionId = sharedPref.getString(getString(R.string.preference_file_session_id), "");

        JSONObject jsonBody= new JSONObject();
        try{
            jsonBody.put("session_id", sessionId);
        } catch (JSONException e){
            e.printStackTrace();
        }

        JsonObjectRequest getRankingRequest = new JsonObjectRequest(
                URL+ RANKING,
                jsonBody,
                new Response.Listener<JSONObject>(){
                    @Override
                    public void onResponse(JSONObject response) {
                        UserModel.getInstance().uploadRanking(response);
                        userAdapter.notifyDataSetChanged();
                        Log.d("richiesta andata bene", response.toString());
                    }
                },
                new Response.ErrorListener(){
                    @Override
                    public void onErrorResponse(VolleyError error){
                        Toast toast= Toast.makeText(getApplicationContext(), "Richiesta Fallita", Toast.LENGTH_SHORT);
                        toast.show();
                    }

                }
        );

        rankRequesteQueue.add(getRankingRequest);
    }

    @Override
    protected void onResume(){
        super.onResume();



    }
}

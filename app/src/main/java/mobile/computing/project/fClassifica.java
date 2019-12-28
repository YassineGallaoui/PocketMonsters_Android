package mobile.computing.project;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;


public class fClassifica extends Fragment {

    public RequestQueue rankRequesteQueue = null;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view= inflater.inflate(R.layout.fragment_f_classifica, container, false);
        RecyclerView list = view.findViewById(R.id.list);

        list.setLayoutManager(new LinearLayoutManager(getActivity()));
        final UserAdapter userAdapter = new UserAdapter(getContext(), getActivity(), UserModel.getInstance().getRanking());

        list.setAdapter(userAdapter);

        return view;
    }

    @Override
    public void onResume() {
        //onStart rende il fragment visibile all'utente
        super.onResume();
        Log.d("ciao", "sono partito");
        //creo un elemento di tipo RecyclerView e gli associo un elemento XML di tipo lista

        rankRequesteQueue = Volley.newRequestQueue(getActivity().getApplicationContext());

        SharedPreferences sharedPref = getActivity().getApplicationContext().getSharedPreferences(
                getString(R.string.preference_file_session_id), Context.MODE_PRIVATE);
        String sessionId = sharedPref.getString(getString(R.string.preference_file_session_id), "");

        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("session_id", sessionId);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest getRankingRequest = new JsonObjectRequest(
                "https://ewserver.di.unimi.it/mobicomp/mostri/ranking.php",
                jsonBody,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        UserModel.getInstance().uploadRanking(response);
                        //userAdapter.notifyDataSetChanged();
                        Log.d("richiesta andata bene", response.toString());
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast toast = Toast.makeText(getActivity().getApplicationContext(), "Richiesta Fallita", Toast.LENGTH_SHORT);
                        toast.show();
                    }

                }
        );

        rankRequesteQueue.add(getRankingRequest);
    }
}
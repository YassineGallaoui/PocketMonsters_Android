package mobile.computing.project;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class Profilo extends Activity {
    private int PICK_IMAGE_REQUEST = 1;
    private String imgBase64;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profilo);

        Button goToRanking=findViewById(R.id.button5);
        goToRanking.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent vaiAClassifica= new Intent(Profilo.this, Classifica.class);
                startActivity(vaiAClassifica);
            }
        });

        final Button goBack=findViewById(R.id.button3);
        goBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent vaiIndietro= new Intent(getApplicationContext(), MainActivity.class);
                startActivity(vaiIndietro);
            }
        });
    }


    protected void onResume() {
        super.onResume();
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
                        Log.d("Richiesta a buon fine", response.toString());
                        getProfile_Response(response);
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

    public void getProfile_Response(JSONObject response){
        TextView profileXP= findViewById(R.id.profileXP);
        TextView profileLP= findViewById(R.id.profileLP);
        TextView username= findViewById(R.id.username);
        User u= new User(response);
        ImageView profileImage= findViewById(R.id.profileImage);
        // se il parametro IMG è diverso da null, allora prendi stringa base64 e converti in bitmap
        if (u.getImage()!= null) {
            String imgBase64 = u.getImage();
            Bitmap img = base64ToBitmap(imgBase64);
            profileImage.setImageBitmap(img);
            Log.d("immagine", String.valueOf(img));

        }
        profileXP.setText(Integer.toString(u.getXP()) + " XP");
        profileLP.setText(Integer.toString(u.getLP())+ " LP");
        username.setText(u.getUsername());
        Log.d("getProfileResponse", "sono Entrato" );

    }

    public void apriLibreria(View v){
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {

            Uri uri = data.getData();

            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                Log.d("immagine", String.valueOf(bitmap));

                ImageView imageView =findViewById(R.id.profileImage);
                imageView.setImageBitmap(bitmap);
                imgBase64= bitmapToBase64(bitmap);
                Log.d("base64",bitmapToBase64(bitmap));

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    public void saveChanges(View v){
        SharedPreferences sharedPref = getApplicationContext().getSharedPreferences(
                getString(R.string.preference_file_session_id), Context.MODE_PRIVATE);
        String sessionId = sharedPref.getString(getString(R.string.preference_file_session_id), "");
        Log.d("sessionId", sessionId);

        Log.d("Fine", "Cliccato fine");
        //qua dovrò fare una chiamata al server..
        EditText username=findViewById(R.id.username);
        String value=username.getText().toString();
        Log.d("Username", value);

        JSONObject jsonBody= new JSONObject();
        try{
            jsonBody.put("session_id", sessionId);
            jsonBody.put("username", value);
            jsonBody.put("img", imgBase64);
        }catch(JSONException e){
            e.printStackTrace();
        }

        RequestQueue saveQueue= Volley.newRequestQueue(this);

        JsonObjectRequest setProfile_request= new JsonObjectRequest("https://ewserver.di.unimi.it/mobicomp/mostri/setprofile.php", jsonBody,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("SetProfile", "Andato a buon fine");
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("SetProfile", "Andata male");
                    }
                }
        );
        saveQueue.add(setProfile_request);
    }

    private String bitmapToBase64(Bitmap bitmap){
        ByteArrayOutputStream byteArrayOutputStream= new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
        byte[] byteArray= byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(byteArray, Base64.DEFAULT);
    }

    private Bitmap base64ToBitmap(String b64){
        byte[] imageAsBytes= Base64.decode(b64.getBytes(), Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(imageAsBytes,0, imageAsBytes.length);
    }


}


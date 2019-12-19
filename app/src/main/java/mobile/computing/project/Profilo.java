package mobile.computing.project;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
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

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class Profilo extends AppCompatActivity {
    private int PICK_IMAGE_REQUEST = 1;
    private String imgBase64="";
    private String imgBase64Nuova="";
    public RequestQueue rankRequesteQueue=null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profilo);

        Button salva=findViewById(R.id.buttonFine);
        salva.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveChanges();
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

        //CHIEDO AL SERVER L'IMMAGINE DA METTERE
        SharedPreferences sharedPref = getApplicationContext().getSharedPreferences(
                getString(R.string.preference_file_session_id), Context.MODE_PRIVATE);
        String sessionId = sharedPref.getString(getString(R.string.preference_file_session_id), "");

        JSONObject jsonBody = new JSONObject();
        try {jsonBody.put("session_id", sessionId); }
        catch (JSONException e) {e.printStackTrace(); }

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        JsonObjectRequest getProfile_Request = new JsonObjectRequest(Request.Method.POST,"https://ewserver.di.unimi.it/mobicomp/mostri/getprofile.php",
                jsonBody,new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        //GESTISCO LA RISPOSTA, OVVERO INSERISCO LE COSE (NOME ECC...) DENTRO I CAMPI
                        User u= new User(response);
                        String xp=u.getXP()+"";
                        String lp=u.getLP()+"";
                        String username=u.getUsername()+"";
                        String image=u.getImage()+"";
                        impostaLayout(username, image, xp, lp);
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


    public void vaiAlSecFragment(View v) {
        fClassifica fragmentC= new fClassifica();
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.container, fragmentC);
        transaction.commit();
        visClassifica();
        //Intent vaiAClassifica= new Intent(Profilo.this, Classifica.class);
        //startActivity(vaiAClassifica);
    }

    public void visClassifica(){
        RecyclerView list=findViewById(R.id.list);
        list.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
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
                "https://ewserver.di.unimi.it/mobicomp/mostri/ranking.php",
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

    public void impostaLayout(String user, String image, String xp, String lp){
        TextView profileXP= findViewById(R.id.profileXP);
        TextView profileLP= findViewById(R.id.profileLP);
        TextView profileUsername= findViewById(R.id.username);
        ImageView profileImage= findViewById(R.id.userImage);

        // se il parametro IMG è diverso da null, allora prendi stringa base64 e converti in bitmap
        //SE C'È UNA IMMAGINE SETTATA DAL SERVER ALLORA LA MOSTRO
        if (!(image.equals("null")) && imgBase64Nuova.equals("")){
            imgBase64 = image;
            Bitmap img = base64ToBitmap(imgBase64);
            profileImage.setImageBitmap(img);
        }
        //SE C'È UNA IMMAGINE SETTATA DAL SERVER ALLORA LA MOSTRO
        if (!(image.equals("null")) && !imgBase64Nuova.equals("")) {
            imgBase64 = imgBase64Nuova;
            Bitmap img = base64ToBitmap(imgBase64);
            profileImage.setImageBitmap(img);
            saveChanges();
        }
        profileUsername.setText(user);
        profileXP.setText(xp);
        profileLP.setText(lp);
    }

    //APRO LA FINESTRA PER FAR SCEGLIERE UNA NUOVA IMMAGINE ALL'UTENTE
    public void apriLibreria(View v){
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    //GESTISCO LA NUOVA IMMAGINE SCELTA DALL'UTENTE
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri uri = data.getData();
            try {
                ImageView imageView =findViewById(R.id.userImage);
                imageView.setImageBitmap(MediaStore.Images.Media.getBitmap(getContentResolver(), uri));
                BitmapDrawable drawable = (BitmapDrawable) imageView.getDrawable();
                Bitmap bitmapCorretta = drawable.getBitmap();
                imgBase64Nuova= bitmapToBase64(bitmapCorretta);
                imgBase64=imgBase64Nuova;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void saveChanges(){

        Log.d("Profilo", "Cliccato fine");

            //FACCIO CHIAMATA AL SERVER PER SETTARE IL NUOVO NOME E/O LA NUOVA FOTO PROFILO
        SharedPreferences sharedPref = getApplicationContext().getSharedPreferences(
                getString(R.string.preference_file_session_id), Context.MODE_PRIVATE);
        String sessionId = sharedPref.getString(getString(R.string.preference_file_session_id), "");
        EditText username=findViewById(R.id.username);
        String value=username.getText().toString();
        RequestQueue saveQueue= Volley.newRequestQueue(this);
        JSONObject jsonBody= new JSONObject();
        try{
            jsonBody.put("session_id", sessionId);
            jsonBody.put("username", value);
            Log.d("Profilo", "La stringa che descrive la nuova immagine è: "+imgBase64);
            jsonBody.put("img", imgBase64);
        }catch(JSONException e){
            e.printStackTrace();
        }

        JsonObjectRequest setProfile_request= new JsonObjectRequest(Request.Method.POST,"https://ewserver.di.unimi.it/mobicomp/mostri/setprofile.php", jsonBody,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("SetProfile", "Andato a buon fine");
                        Toast.makeText(getApplicationContext(), "Data successfully saved : )", Toast.LENGTH_SHORT).show();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("SetProfile", "Andata male");
                        Toast.makeText(getApplicationContext(), "Something went wrong : (", Toast.LENGTH_SHORT).show();
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

    protected void onResume() { super.onResume(); }
}


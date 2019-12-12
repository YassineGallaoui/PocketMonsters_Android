package mobile.computing.project;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.widget.ImageView;
import android.widget.TextView;

public class infoOggetto extends Activity {

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
        String id= extras.getString("id");
        //String img64= extras.getString("img");
        //byte[] decodedString = Base64.decode(img64, Base64.DEFAULT);
        //Bitmap decodedImg = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
        String nome= extras.getString("nome");
        String tipo= extras.getString("tipo");
        String size= extras.getString("size");
        String latitudine= extras.getString("lat");
        String longitudine= extras.getString("lon");

        //ImageView img = findViewById(R.id.imageView) ;
        //img.setImageBitmap(decodedImg);
        TextView tv1=findViewById(R.id.textView);
        tv1.setText(nome);
        TextView tv2=findViewById(R.id.textView2);
        tv2.setText(size);
        TextView tv3=findViewById(R.id.textView3);
        tv3.setText(latitudine);
        TextView tv4=findViewById(R.id.textView4);
        tv4.setText(longitudine);
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

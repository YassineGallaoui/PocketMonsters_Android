package mobile.computing.project;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
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

public class infoRisultato extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info_risultato);

        DisplayMetrics dm=new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);

        int width=dm.widthPixels;
        int height=dm.heightPixels;

        getWindow().setLayout((int)(width*.9), (int)(height*.8));

        Log.d("infoRisultato","Sono entrato nella nuova activity");
        Bundle extras = getIntent().getExtras();
        ImageView im1=findViewById(R.id.imageView);
        byte[] decodedString = Base64.decode(extras.getString("img"), Base64.DEFAULT);
        Bitmap decodedImg = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
        im1.setImageBitmap(decodedImg);
        TextView tv1=findViewById(R.id.textView2);
        if((extras.getString("life")).equals("false")){
            if((extras.getString("type")).equals("MO"))
                tv1.setText("Hai vinto!");
            else tv1.setText("Mangiata !");
        } else {
                tv1.setText("Sei morto...");
        }
        TextView LifePoints=findViewById(R.id.textView4);
        LifePoints.setText(extras.getString("lp"));
        TextView ExpPoints=findViewById(R.id.textView6);
        ExpPoints.setText(extras.getString("xp"));

        Button ok= findViewById(R.id.button7);
        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    @Override
    protected void onRestart() {
        super.onRestart();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}

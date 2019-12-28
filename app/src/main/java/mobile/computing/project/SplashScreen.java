package mobile.computing.project;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.widget.ImageView;

public class SplashScreen extends Activity {

    private ImageView miaImmagine;
    int t=1000;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        miaImmagine = (ImageView) findViewById(R.id.splashImageView);
        miaImmagine.setImageResource(R.mipmap.splash_image);

        Thread timerThread = new Thread() {
            public void run() {
                try {
                    sleep(t);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    //CONTROLLO SE INTERNET È ACCESO
                    ConnectivityManager connectivityManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
                    while(true){
                        if(connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                                connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED) {
                            //SIAMO CONNESSI A UNA QUALCHE RETE
                            Intent vaiAlGioco=new Intent(getApplicationContext(), MainActivity.class);
                            startActivity(vaiAlGioco);
                        }
                    }
                }
            }
        };
        timerThread.start();
    }
}
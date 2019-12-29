package mobile.computing.project;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

public class SplashScreen extends Activity {

    private ImageView miaImmagine;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        miaImmagine = (ImageView) findViewById(R.id.splashImageView);
        miaImmagine.setImageResource(R.mipmap.splash_image);

        Thread timerThread = new Thread() {
            public void run() {
                try {
                    while(!checkConnection()){
                        Log.d("SplashScreen", "Non sei connesso alla rete");
                        sleep(100);
                    }
                    if(checkConnection()){
                        Log.d("SplashScreen", "Ora sei connesso alla rete");
                        sleep(900);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    Intent vaiAlGioco=new Intent(getApplicationContext(), MainActivity.class);
                    startActivity(vaiAlGioco);
                }
            }
        };
        timerThread.start();
    }

    public boolean checkConnection(){
        ConnectivityManager connectivityManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        return (connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED);
    }
}
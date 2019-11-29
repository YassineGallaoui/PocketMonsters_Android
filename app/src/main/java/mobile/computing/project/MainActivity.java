package mobile.computing.project;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button profilo=findViewById(R.id.button2);
        profilo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent vaiAlProfilo= new Intent(getApplicationContext(), profilo.class);
                startActivity(vaiAlProfilo);
            }
        });

        Button gioca=findViewById(R.id.button);
        gioca.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent vaiAlGioco=new Intent(getApplicationContext(), play.class);
                startActivity(vaiAlGioco);
            }
        });
    }
}

package barqsoft.footballscores.Utilities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import barqsoft.footballscores.R;

/**
 * Created by Carlos on 16/10/2015.
 */
public class ConnectionActivity extends AppCompatActivity {
    private Button button;
     @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //super.onCreate(savedInstanceState, persistentState);
        setContentView(R.layout.connection_activity);
         button= (Button) findViewById(R.id.retryButton);
         button.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 if(Utilies.isNetworkAvailable(ConnectionActivity.this)){
                     finish();
                 }
             }
         });
     }
}

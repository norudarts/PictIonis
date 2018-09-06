package etna.hyvernparede.pictionis;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.google.firebase.database.DatabaseReference;

import etna.hyvernparede.pictionis.drawing.DrawingView;

public class DrawingActivity extends AppCompatActivity {

    private DrawingView drawingView;
    private DatabaseReference databaseReference;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drawing);


    }
}

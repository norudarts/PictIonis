package etna.hyvernparede.pictionis;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Random;
import java.util.stream.Stream;

public class Dictionary {

    private final String TAG = "Dictionary";
    private Context context;

    private ArrayList<String> words;

    public Dictionary(Context context) {
        words = new ArrayList<>();
        this.context = context;
    }

    public void loadDictionary() {
        try {
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(context.getAssets().open("dictionary")));

            String line;
            while ((line = reader.readLine()) != null) {
                words.add(line);
            }
        } catch (IOException e) {
            Log.e(TAG, "Erreur lors du chargement du dictionnaire : " + e.toString());
            Toast.makeText(context, "Le dictionnaire n'a pas pu être lu.", Toast.LENGTH_SHORT).show();
        }
    }

    public String getRandomWord() {
        return words.get(new Random().nextInt(words.size()));
    }
}

package mobile.computing.project;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class OggettiMappa {

    private ArrayList<Oggetto> OggettiMappaList;
    private static final OggettiMappa ourInstance = new OggettiMappa();

    public static OggettiMappa getInstance() {
        return ourInstance;
    }

    private OggettiMappa() {
        OggettiMappaList = new ArrayList<>();
    }

    public void populate(JSONObject serverResponse) {
        Log.d("OggettiMappa", "I am populating the model");
        try {
            JSONArray OggettiMappaJSON = serverResponse.getJSONArray("mapobjects");
            for (int i = 0; i < OggettiMappaJSON.length(); i++) {
                JSONObject singoloOggettoJSON = OggettiMappaJSON.getJSONObject(i);
                Oggetto oggetto = new Oggetto(singoloOggettoJSON);
                OggettiMappaList.add(oggetto);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    public Oggetto getOggetto(int i) {
        return OggettiMappaList.get(i);
    }

    public void svuota(){
        OggettiMappaList.clear();
    }

    public int getSize(){
        return OggettiMappaList.size();
    }

    public ArrayList<Oggetto> getOggettiMappaList() {
        return OggettiMappaList;
    }

}



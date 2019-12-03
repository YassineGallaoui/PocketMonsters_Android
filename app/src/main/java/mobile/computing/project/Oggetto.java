package mobile.computing.project;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

public class Oggetto {
    private int id;
    private double latitude;
    private double longitude;
    private String type;
    private String size;
    private String name;

    public Oggetto() {
    }

    public Oggetto(int ID, double lat, double lon, String tipo, String grandezza, String nome) {
        this();
        this.id = ID;
        this.latitude = lat;
        this.longitude = lon;
        this.type = tipo;
        this.size = grandezza;
        this.name = nome;
    }

    public Oggetto(JSONObject oggettoJSON) {
        this();
        try {
            this.id = Integer.parseInt(oggettoJSON.getString("id"));
            Log.d("Oggetto",""+id);
            this.latitude = Double.parseDouble(oggettoJSON.getString("lat"));
            Log.d("Oggetto",""+latitude);
            this.longitude = Double.parseDouble(oggettoJSON.getString("lon"));
            Log.d("Oggetto",""+longitude);
            this.type = oggettoJSON.getString("type");
            Log.d("Oggetto",""+type);
            this.size = oggettoJSON.getString("size");
            Log.d("Oggetto",""+size);
            this.name = oggettoJSON.getString("name");
            Log.d("Oggetto",""+name);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public int getId() {
        return id;
    }

    public double getLat() {
        return latitude;
    }

    public double getLon() {
        return longitude;
    }

    public String getType() {
        return type;
    }

    public String getSize() {
        return size;
    }

    public String getName() {
        return name;
    }

}
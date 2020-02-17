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
    private String img;

    public Oggetto() {
    }

    public Oggetto(int ID, double lat, double lon, String tipo, String grandezza, String nome, String immagine) {
        this();
        this.id = ID;
        this.latitude = lat;
        this.longitude = lon;
        this.type = tipo;
        this.size = grandezza;
        this.name = nome;
        this.img = immagine;
    }

    public Oggetto(JSONObject oggettoJSON) {
        this();
        try {
            this.id = Integer.parseInt(oggettoJSON.getString("id"));
            this.latitude = Double.parseDouble(oggettoJSON.getString("lat"));
            this.longitude = Double.parseDouble(oggettoJSON.getString("lon"));
            this.type = oggettoJSON.getString("type");
            this.size = oggettoJSON.getString("size");
            this.name = oggettoJSON.getString("name");
            this.img = "";
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

    public void setImg(JSONObject immagine) {
        try {
            this.img=immagine.getString("img");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public String getImg() {
        return img;
    }

}
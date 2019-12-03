package mobile.computing.project;

import org.json.JSONException;
import org.json.JSONObject;

public class User {
    private String sessionId;
    private String username;
    private int XP;
    private int LP;
    //da rivedere
    private String image;


    //costruttore generico
    public User(String sessionId, String username, String image){
        this.sessionId=sessionId;
        this.username=username;
        this.image=image;
    }

    public User(JSONObject userJSON){
        try {
            this.username=userJSON.getString("username");
            this.image=userJSON.getString("img");
            this.XP=userJSON.getInt("xp");
            this.LP=userJSON.getInt("lp");
        }
        catch (JSONException e){
            e.printStackTrace();
        }

    }

    public String getUsername(){
        return username;
    }

    public void setUsername(String username){
        this.username= username;
    }

    public String image(){
        return image;
    }

    public void setImage(String image){
        this.image=image;
    }

    public int getXP(){
        return XP;
}

    public int getLP(){
        return LP;
    }



}


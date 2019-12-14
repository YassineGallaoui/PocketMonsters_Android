package mobile.computing.project;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class UserModel {
    private ArrayList<User> usersTop20;
    private static final UserModel ourInstance = new UserModel();

    public static UserModel getInstance() {
        return ourInstance;
    }

    private UserModel() {
        usersTop20= new ArrayList<>();
    }

    public void uploadRanking(JSONObject response){
        try{
            JSONArray rankingJSON= response.getJSONArray("ranking");
            for (int i=0; i<rankingJSON.length(); i++){
                JSONObject userJSON= rankingJSON.getJSONObject(i);
                User user= new User(userJSON);
                usersTop20.add(user);
            }
        }catch (JSONException e){
            e.printStackTrace();
        }
    }

    public ArrayList<User> getRanking() {
        return usersTop20;
    }
}

package mobile.computing.project;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class UserModel {
    private static final UserModel ourInstance = new UserModel();
    private ArrayList<User> usersTop20;

    private UserModel() {
        usersTop20 = new ArrayList<>();
    }

    public static UserModel getInstance() {
        return ourInstance;
    }

    public void uploadRanking(JSONObject response) {
        try {
            JSONArray rankingJSON = response.getJSONArray("ranking");
            usersTop20.clear();
            for (int i = 0; i < rankingJSON.length(); i++) {
                JSONObject userJSON = rankingJSON.getJSONObject(i);
                User user = new User(userJSON);
                usersTop20.add(user);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public ArrayList<User> getRanking() {
        return usersTop20;
    }
}

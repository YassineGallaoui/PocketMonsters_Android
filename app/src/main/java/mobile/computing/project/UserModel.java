package mobile.computing.project;

import java.util.ArrayList;

public class UserModel {
    private ArrayList<User> users;
    private static final UserModel ourInstance = new UserModel();

    public static UserModel getInstance() {
        return ourInstance;
    }

    private UserModel() {
        users= new ArrayList<>();
    }


}

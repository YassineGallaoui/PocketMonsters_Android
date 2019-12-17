package mobile.computing.project;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class UserAdapter extends RecyclerView.Adapter<ListElement> {
    private LayoutInflater inflater;
    private Activity parentActivity;
    private ArrayList<User> usersTop20;
    int pos=0;

    public UserAdapter(Context context, Activity parentActivity, ArrayList<User> usersTop20){
        this.inflater= LayoutInflater.from(context);
        this.parentActivity= parentActivity;
        this.usersTop20= usersTop20;
    }

    @Override
    public ListElement onCreateViewHolder(ViewGroup parent, int viewType){
        View view=inflater.inflate(R.layout.list_element, parent, false);
        return new ListElement(view, parentActivity);
    }

    @Override
    public void onBindViewHolder(ListElement holder, int position){
        User user=usersTop20.get(position);
        pos++;
        holder.setUser(user, pos);

    }

    @Override
    public int getItemCount(){
        return usersTop20.size();
    }

}




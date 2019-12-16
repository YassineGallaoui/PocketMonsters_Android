package mobile.computing.project;

import android.app.Activity;
import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

public class ListElement extends RecyclerView.ViewHolder{

    private TextView listUsername;
    private TextView listXP;
    private TextView listLP;
    private TextView listRankingPosition;

    private Activity parentActivity;

    public ListElement(View itemView, Activity parentActivity){
        super(itemView);
        this.parentActivity=parentActivity;
        listUsername= itemView.findViewById(R.id.listUsername);
        listXP=itemView.findViewById(R.id.listXP);
        listLP=itemView.findViewById(R.id.listLP);
        listRankingPosition= itemView.findViewById(R.id.listRankingPosition);

    }

    public void setUser(User user, int i){
        listUsername.setText(user.getUsername());
        listXP.setText(Integer.toString(user.getXP()) + " XP");
        listLP.setText(Integer.toString(user.getLP()) + " LP");
        listRankingPosition.setText(Integer.toString(i));

    }
}

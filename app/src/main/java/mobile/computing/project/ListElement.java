package mobile.computing.project;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

public class ListElement extends RecyclerView.ViewHolder{

    private TextView listUsername;
    private TextView listXP;
    private TextView listLP;
    private TextView listRankingPosition;
    private ImageView listImage;

    private Activity parentActivity;

    public ListElement(View itemView, Activity parentActivity){
        super(itemView);
        this.parentActivity=parentActivity;
        listUsername= itemView.findViewById(R.id.listUsername);
        listXP=itemView.findViewById(R.id.listXP);
        listLP=itemView.findViewById(R.id.listLP);
        listRankingPosition= itemView.findViewById(R.id.listRankingPosition);
        listImage=itemView.findViewById((R.id.listImage));

    }

    public void setUser(User user, int i){
        listUsername.setText(user.getUsername());
        listXP.setText(user.getXP() + " XP");
        listLP.setText(user.getLP() + " LP");
        listRankingPosition.setText(Integer.toString(i));
        String img=user.getImage();

        if (!(img.equals("null"))){
            Bitmap immagine = base64ToBitmap(img);
            listImage.setImageBitmap(immagine);
        }

    }

    private Bitmap base64ToBitmap(String b64){
        byte[] imageAsBytes= Base64.decode(b64.getBytes(), Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(imageAsBytes,0, imageAsBytes.length);
    }

}

package mobile.computing.project;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;


public class fUser extends Fragment {

    public fUser() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment, l'inflater è usato per creare view a runtime.
        //converte un file XML layout in view vere e proprie
        return inflater.inflate(R.layout.fragment_f_user, container, false);

    }

}

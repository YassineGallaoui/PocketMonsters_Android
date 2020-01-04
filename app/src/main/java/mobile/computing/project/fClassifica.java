package mobile.computing.project;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.android.volley.RequestQueue;


public class fClassifica extends Fragment {

    public RequestQueue rankRequesteQueue = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_f_classifica, container, false);
    }
}
package it.unito.di.taass.helpin.profilo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import it.unito.di.taass.helpin.Constants;
import it.unito.di.taass.helpin.oggetti.UtenteFamiglia;
import it.unito.di.taass.helpin.oggetti.UtenteSitter;
import it.unito.di.taass.helpin.R;

public class ProfiloPrivatoActivity extends AppCompatActivity implements PrivatoFamigliaFragment.OnFragmentInteractionListener, PrivatoSitterFragment.OnFragmentInteractionListener{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.frame_layout);

        // Selezione del fragment in base al tipo dell'intent
        int type = getIntent().getIntExtra(Constants.TYPE, -1);
        if (type == Constants.TYPE_SITTER){
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.viewPager, new PrivatoSitterFragment()).commit();
        } else if(type == Constants.TYPE_FAMILY) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.viewPager, new PrivatoFamigliaFragment()).commit();

        }
    }

    @Override
    public void onFragmentInteraction(UtenteFamiglia famiglia) {
    }

    @Override
    public void onFragmentInteraction(UtenteSitter sitter) {

    }

}

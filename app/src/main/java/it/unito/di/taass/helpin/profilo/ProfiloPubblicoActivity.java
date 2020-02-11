package it.unito.di.taass.helpin.profilo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;

import it.unito.di.taass.helpin.Constants;
import it.unito.di.taass.helpin.oggetti.UtenteFamiglia;
import it.unito.di.taass.helpin.oggetti.UtenteSitter;
import it.unito.di.taass.helpin.R;

public class ProfiloPubblicoActivity extends AppCompatActivity implements PubblicoFamigliaFragment.OnFragmentInteractionListener, PubblicoSitterFragment.OnFragmentInteractionListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.frame_layout);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Selezione del fragment in base al tipo dell'intent
        int type = getIntent().getIntExtra(Constants.TYPE, -1);
        if (type == Constants.TYPE_SITTER){
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.viewPager, new PubblicoSitterFragment()).commit();
        } else if(type == Constants.TYPE_FAMILY) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.viewPager, new PubblicoFamigliaFragment()).commit();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case android.R.id.home:
                finish();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onFragmentInteraction(UtenteFamiglia family) {

    }

    @Override
    public void onFragmentInteraction(UtenteSitter sitter) {

    }

}

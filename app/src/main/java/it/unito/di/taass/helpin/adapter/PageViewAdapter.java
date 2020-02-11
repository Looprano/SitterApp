package it.unito.di.taass.helpin.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import it.unito.di.taass.helpin.recensioni.RecensioniRicevuteFragment;
import it.unito.di.taass.helpin.recensioni.RecensioniScritteFragment;


/**
 * Adapter per il tabbed layout
 */

public class PageViewAdapter extends FragmentPagerAdapter {

    private int mNumOfTabs;

    //costruttore
    public PageViewAdapter(FragmentManager fm, int NumOfTabs) {
        super(fm);
        this.mNumOfTabs = NumOfTabs;

    }

    @Override
    public Fragment getItem(int position) {

        Fragment fragment = null;

        // a seconda della posizione della UI, dichiaro un fragment per le due visualizzazioni differenti del tabbed
        switch (position) {
            case 0:
                fragment = new RecensioniScritteFragment();
                break;
            case 1:

                fragment = new RecensioniRicevuteFragment();
                break;

        }


        return fragment;
    }

    //restituisce il numero di tabs presenti nel tabbed layout
    @Override
    public int getCount() {
        return mNumOfTabs;
    }


}

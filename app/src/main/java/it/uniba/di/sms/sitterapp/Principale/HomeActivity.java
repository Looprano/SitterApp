package it.uniba.di.sms.sitterapp.Principale;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import it.uniba.di.sms.sitterapp.Adapter.NoticeAdapter;
import it.uniba.di.sms.sitterapp.Adapter.SitterAdapter;
import it.uniba.di.sms.sitterapp.Appuntamenti.DialogsNoticeDetails;
import it.uniba.di.sms.sitterapp.Constants;
import it.uniba.di.sms.sitterapp.Oggetti.Notice;
import it.uniba.di.sms.sitterapp.Oggetti.UtenteSitter;
import it.uniba.di.sms.sitterapp.Php;
import it.uniba.di.sms.sitterapp.Profilo.ProfiloPubblicoActivity;
import it.uniba.di.sms.sitterapp.R;

public class HomeActivity extends DrawerActivity
        implements NoticeAdapter.NoticeAdapterListener, SitterAdapter.ContactsSitterAdapterListener, DialogFiltro.DialogListener {

    // Vista
    private RecyclerView recyclerView;

    //Items babysitter
    private List<Notice> noticeList;
    private NoticeAdapter noticeAdapter;

    //Items family
    private List<UtenteSitter> sitterList;
    private List<UtenteSitter> filteredSitterList;
    private SitterAdapter sitterAdapter;


    FloatingActionButton cercaSitter;

    //richieste
    private static final String annunci = "ANNUNCI";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //FAB per la ricerca delle baby sitter
        cercaSitter = (FloatingActionButton) findViewById(R.id.cercaSitter);
        if (sessionManager.getSessionType() == Constants.TYPE_FAMILY && cercaSitter.getVisibility() == View.GONE) {
            cercaSitter.show();
            cercaSitter.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //ricerca
                    DialogFiltro dialogFiltro = DialogFiltro.newInstance();
                    dialogFiltro.show(getSupportFragmentManager(),"dialog");
                }
            });
        } else if (sessionManager.getSessionType() == Constants.TYPE_SITTER && cercaSitter.getVisibility() == View.VISIBLE) {
            cercaSitter.hide();
        }

        /*
        DA QUI INIZIA LA PARTE DEL CARICAMENTO DEGLI ANNUNCI
         */
        recyclerView = (RecyclerView) findViewById(R.id.recyclerHome);

        if ((sessionManager.getSessionType() == Constants.TYPE_SITTER)) {

            noticeList = new ArrayList<>();
            noticeAdapter = new NoticeAdapter(HomeActivity.this, noticeList, HomeActivity.this);

            recyclerView.setAdapter(noticeAdapter);

            RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
            recyclerView.setLayoutManager(mLayoutManager);
            recyclerView.setItemAnimator(new DefaultItemAnimator());

            loadNotices();

        } else if (sessionManager.getSessionType() == Constants.TYPE_FAMILY) {

            sitterList = new ArrayList<>();
            sitterAdapter = new SitterAdapter(HomeActivity.this, sitterList, HomeActivity.this);

            recyclerView.setAdapter(sitterAdapter);

            RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
            recyclerView.setLayoutManager(mLayoutManager);
            recyclerView.setItemAnimator(new DefaultItemAnimator());

            loadSitter();
        }
    }

    /**
     * Caricamento degli annunci (per la home delle babysitter)
     */
    private void loadNotices() {

        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        final StringRequest stringRequest = new StringRequest(Request.Method.POST, Php.ANNUNCI,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        progressDialog.dismiss();
                        try {
                            JSONArray notice = new JSONArray(response);
                            for (int i = 0; i < notice.length(); i++) {

                                JSONObject noticeObject = notice.getJSONObject(i);
                                String idAnnuncio = noticeObject.getString("idAnnuncio");
                                String famiglia = noticeObject.getString("usernameFamiglia");
                                String data = noticeObject.getString("data");
                                String oraInizio = noticeObject.getString("oraInizio");
                                String oraFine = noticeObject.getString("oraFine");
                                String descrizione = noticeObject.getString("descrizione");
                                Notice n = new Notice(idAnnuncio, famiglia, data, oraInizio, oraFine, descrizione);
                                noticeList.add(n);
                            }

                            noticeAdapter.notifyDataSetChanged();

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(HomeActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("richiesta",annunci);
                params.put("username", sessionManager.getSessionUsername());
                return params;
            }
        };
        Volley.newRequestQueue(this).add(stringRequest);
        progressDialog.hide();
    }

    /**
     * Caricamento dell'elenco babysitter (per la home della famiglia)
     */
    private void loadSitter() {

        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        StringRequest stringRequest = new StringRequest(Request.Method.GET, Php.ELENCO_SITTER,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        progressDialog.dismiss();

                        try {
                            JSONArray sitter = new JSONArray(response);
                            for (int i = 0; i < sitter.length(); i++) {
                                JSONObject sitterObject = sitter.getJSONObject(i);
                                String username = sitterObject.getString("username");
                                String foto = sitterObject.getString("pathfoto");
                                float rating;
                                if(sitterObject.getString("rating").equals("null")){
                                     rating = 0;
                                }
                                else{rating =(float) sitterObject.getDouble("rating");}
                                UtenteSitter s = new UtenteSitter(username, foto,rating);
                                sitterList.add(s);
                            }

                            sitterAdapter.notifyDataSetChanged();

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(HomeActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
        Volley.newRequestQueue(this).add(stringRequest);
        progressDialog.hide();
    }

    /**
     * @param notice al click su un annuncio visualizza i dettagli
     */
    @Override
    public void onNoticeSelected(Notice notice) {
        DialogsNoticeDetails dialogs = DialogsNoticeDetails.newInstance(notice);
        dialogs.show(getSupportFragmentManager(), "dialog");
    }

    @Override
    public void onSitterSelected(UtenteSitter sitter) {

        Intent detailIntent = new Intent(HomeActivity.this, ProfiloPubblicoActivity.class);
        detailIntent.putExtra(Constants.TYPE, Constants.TYPE_SITTER);
        detailIntent.putExtra("username", sitter.getUsername());
        startActivity(detailIntent);
    }

    @Override
    public void settaFiltro(final ArrayList<Integer> checkedBox, final float rating, final int minLavori) {

        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        StringRequest filtro = new StringRequest(Request.Method.POST, Php.FILTRO, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                progressDialog.dismiss();
                try {
                    JSONArray sitter = new JSONArray(response);
                    for (int i = 0; i < sitter.length(); i++) {
                        JSONObject sitterObject = sitter.getJSONObject(i);
                        String username = sitterObject.getString("username");
                        String foto = sitterObject.getString("pathfoto");
                        float rating;
                        if(sitterObject.getString("rating").equals("null")){
                            rating = 0;
                        }
                        else{rating =(float) sitterObject.getDouble("rating");}
                        UtenteSitter s = new UtenteSitter(username, foto,rating);
                        filteredSitterList.add(s);
                    }

                    sitterAdapter.notifyDataSetChanged();

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                JSONArray JSONChecked = new JSONArray(checkedBox);
                params.put("disponibilita", JSONChecked.toString());
                params.put("rating", Float.toString(rating));
                params.put("minLavori", Integer.toString(minLavori));
                return params;
            }
        };
    }
}
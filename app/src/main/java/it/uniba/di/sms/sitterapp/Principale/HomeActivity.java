package it.uniba.di.sms.sitterapp.Principale;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Toast;

import com.pnikosis.materialishprogress.ProgressWheel;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import it.uniba.di.sms.sitterapp.Adapter.NoticeAdapter;
import it.uniba.di.sms.sitterapp.Adapter.SitterAdapter;
import it.uniba.di.sms.sitterapp.Constants;
import it.uniba.di.sms.sitterapp.Appuntamenti.NoticeDetailActivity;
import it.uniba.di.sms.sitterapp.Oggetti.Notice;
import it.uniba.di.sms.sitterapp.Oggetti.UtenteSitter;
import it.uniba.di.sms.sitterapp.R;

public class HomeActivity extends DrawerActivity
        implements NoticeAdapter.NoticeAdapterListener, SitterAdapter.ContactsSitterAdapterListener {

    // Vista
    private RecyclerView recyclerView;

    //Items babysitter
    private List<Notice> noticeList;
    private Queue<Notice> remainingNoticeList;
    private NoticeAdapter noticeAdapter;

    //Items family
    private List<UtenteSitter> sitterList;
    private Queue<UtenteSitter> remainingSitterList;
    private SitterAdapter sitterAdapter;


    // we will be loading 15 items per page or per load
    // you can change this to fit your specifications.
    // When you change this, there will be no need to update your php page,
    // as php will be ordered what to load and limit by android java
    public static final int LOAD_LIMIT = 5;

    // Url per il caricamento degli items
    private static final String NOTICE_URL = Constants.BASE_URL + "AnnunciFamiglie.php";
    private static final String SITTER_URL = Constants.BASE_URL + "AnnunciSitter.php";

    // we need this variable to lock and unlock loading more
    // e.g we should not load more when volley is already loading,
    // loading will be activated when volley completes loading
    private boolean itShouldLoadMore = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /*
        DA QUI INIZIA LA PARTE DEL CARICAMENTO DEGLI ANNUNCI
         */
        recyclerView = (RecyclerView) findViewById(R.id.recyclerHome);

        if ((sessionManager.getSessionType() == Constants.TYPE_SITTER)) {

            noticeList = new ArrayList<>();
            noticeAdapter = new NoticeAdapter(HomeActivity.this, noticeList, HomeActivity.this);
            remainingNoticeList = new LinkedList<>();

            recyclerView.setAdapter(noticeAdapter);

            RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
            recyclerView.setLayoutManager(mLayoutManager);
            recyclerView.setItemAnimator(new DefaultItemAnimator());

            //caricamento di annunci
            loadNotices();

        } else if (sessionManager.getSessionType() == Constants.TYPE_FAMILY) {

            sitterList = new ArrayList<>();
            sitterAdapter = new SitterAdapter(HomeActivity.this, sitterList, HomeActivity.this);
            remainingSitterList = new LinkedList<>();

            recyclerView.setAdapter(sitterAdapter);

            RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
            recyclerView.setLayoutManager(mLayoutManager);
            recyclerView.setItemAnimator(new DefaultItemAnimator());

            //prova caricamento di annunci
            loadSitter();
        }


        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {

                if (dy > 0) {
                    // Recycle view scrolling downwards...
                    // this if statement detects when user reaches the end of recyclerView, this is only time we should load more
                    if (!recyclerView.canScrollVertically(RecyclerView.FOCUS_DOWN)) {
                        // remember "!" is the same as "== false"
                        // here we are now allowed to load more, but we need to be careful
                        // we must check if itShouldLoadMore variable is true [unlocked]
                        if (itShouldLoadMore) {

                            loadMore();
                        }
                    }
                }
            }
        });
    }


    /**
     * Caricamento degli annunci (per la home delle babysitter)
     */
    private void loadNotices() {

        itShouldLoadMore = false; // lock this guy,(itShouldLoadMore) to make sure,
        // user will not load more when volley is processing another request
        // only load more when  volley is free

        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        StringRequest stringRequest = new StringRequest(Request.Method.GET, NOTICE_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        progressDialog.dismiss();
                        itShouldLoadMore = true;
                        try {
                            JSONArray notice = new JSONArray(response);
                            for (int i = 0; i < notice.length(); i++) {

                                JSONObject noticeObject = notice.getJSONObject(i);
                                String famiglia = noticeObject.getString("usernameFamiglia");
                                String data = noticeObject.getString("data");
                                String oraInizio = noticeObject.getString("oraInizio");
                                String oraFine = noticeObject.getString("oraFine");
                                String descrizione = noticeObject.getString("descrizione");
                                descrizione = (descrizione.length() > 100) ? descrizione.substring(0, 100) + "..." : descrizione;
                                Notice n = new Notice(famiglia, data, oraInizio, oraFine, descrizione);

                                if (i < LOAD_LIMIT) {
                                    noticeList.add(n);
                                } else {
                                    remainingNoticeList.add(n);
                                }
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
        });
        Volley.newRequestQueue(this).add(stringRequest);
        progressDialog.hide();
    }

    /**
     * Caricamento dell'elenco babysitter (per la home della famiglia)
     */
    private void loadSitter() {

        itShouldLoadMore = false; // lock this guy,(itShouldLoadMore) to make sure,
        // user will not load more when volley is processing another request
        // only load more when  volley is free

        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        StringRequest stringRequest = new StringRequest(Request.Method.GET, SITTER_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        progressDialog.dismiss();
                        itShouldLoadMore = true;

                        try {
                            JSONArray sitter = new JSONArray(response);
                            for(int i=0;i<sitter.length();i++){
                                JSONObject sitterObject = sitter.getJSONObject(i);
                                String username = sitterObject.getString("username");
                                String nLavori = sitterObject.getString("numerolavori");
                                String foto = sitterObject.getString("pathfoto");
                                UtenteSitter s = new UtenteSitter(username,nLavori,foto);

                                if(i < LOAD_LIMIT) {
                                    sitterList.add(s);
                                } else {
                                    remainingSitterList.add(s);
                                }
                            }

                            sitterAdapter.notifyDataSetChanged();

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(HomeActivity.this,error.getMessage(),Toast.LENGTH_SHORT).show();
            }
        });
        Volley.newRequestQueue(this).add(stringRequest);
        progressDialog.hide();
    }

    /**
     * Caricamento incrementale degli annunci
     */
    private void loadMore() {

        itShouldLoadMore = false; // lock this until volley completes processing

        // progressWheel is just a loading spinner, please see the content_main.xml
        final ProgressWheel progressWheel = (ProgressWheel) this.findViewById(R.id.progress_wheel);
        progressWheel.setVisibility(View.VISIBLE);

        itShouldLoadMore = true;

        if (sessionManager.getSessionType() == Constants.TYPE_SITTER) {

            if (!remainingNoticeList.isEmpty()) {

                final int remainingNoticeListSize = remainingNoticeList.size();

                for (int i = 0; i < remainingNoticeListSize; ++i) {

                    if (i < LOAD_LIMIT) {
                        noticeList.add(remainingNoticeList.remove());
                    }
                }

                noticeAdapter.notifyDataSetChanged();
            }

        } else if (sessionManager.getSessionType() == Constants.TYPE_FAMILY) {
            if (!remainingSitterList.isEmpty()) {

                final int remainingSitterListSize = remainingSitterList.size();
                for (int i = 0; i < remainingSitterListSize; ++i) {
                    if (i < LOAD_LIMIT) {
                        sitterList.add(remainingSitterList.remove());
                    }
                }
                sitterAdapter.notifyDataSetChanged();
            }
        }

        progressWheel.setVisibility(View.GONE);
    }

    /**
     * @param notice al click su un annuncio visualizza i dettagli
     */
    @Override
    public void onNoticeSelected(Notice notice) {
        Intent detailIntent = new Intent(HomeActivity.this, NoticeDetailActivity.class);
        detailIntent.putExtra(Constants.TYPE, Constants.TYPE_SITTER);
        detailIntent.putExtra("famiglia", notice.getFamily());
        detailIntent.putExtra("data", notice.getDate());
        detailIntent.putExtra("oraInizio", notice.getStart_time());
        detailIntent.putExtra("oraFine", notice.getEnd_time());
        detailIntent.putExtra("descrizione", notice.getDescription());
        startActivity(detailIntent);
    }



    @Override
    public void onSitterSelected(UtenteSitter sitter) {

        //Intent detailIntent = new Intent(HomeActivity.this, SitterDetailActivity.class);
        //detailIntent.putExtra(Constants.TYPE, Constants.TYPE_FAMILY);
        //startActivity(detailIntent);
    }
}
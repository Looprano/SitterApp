package it.unito.di.taass.helpin.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.Calendar;
import java.util.List;

import it.unito.di.taass.helpin.oggetti.Notice;
import it.unito.di.taass.helpin.R;

public class NoticeAdapter extends RecyclerView.Adapter<NoticeAdapter.MyViewHolder> {
    private Context context;
    private List<Notice> noticeList;
    private List<Notice> noticeListFiltered;
    private NoticeAdapterListener listener;
    private final static int TEXT_TO_SHOW = 100;

    class MyViewHolder extends RecyclerView.ViewHolder {
        private TextView family_name, date, start_time, end_time, description, scad;

        MyViewHolder(View view) {
            super(view);
            family_name = (TextView) view.findViewById(R.id.appuntamento_item_username);
            description = (TextView) view.findViewById(R.id.appuntamento_item_detail);
            start_time = (TextView) view.findViewById(R.id.appuntamento_item_start);
            end_time = (TextView) view.findViewById(R.id.appuntamento_item_end);
            date = (TextView) view.findViewById(R.id.appuntamento_item_data);
            scad = (TextView) view.findViewById(R.id.scadutoCard);

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // send selected contact in callback
                    listener.onNoticeSelected(noticeListFiltered.get(getAdapterPosition()));
                }
            });
        }
    }


    //castruttore
    public NoticeAdapter(Context context, List<Notice> noticeList, NoticeAdapterListener listener) {
        this.context = context;
        this.listener = listener;
        this.noticeList = noticeList;
        this.noticeListFiltered = noticeList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_notice, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, final int position) {

        //attribuzione dei dettagli dell'annuncio alle varie View
        final Notice notice = noticeListFiltered.get(position);
        holder.family_name.setText(notice.getFamily());
        holder.date.setText(notice.getDate());
        holder.description.setText((notice.getDescription().length() > TEXT_TO_SHOW ? notice.getDescription().substring(0, TEXT_TO_SHOW) + "..." : notice.getDescription()));
        holder.start_time.setText(notice.getStart_time());
        holder.end_time.setText(notice.getEnd_time());


        //Controllo se l'annuncio è scaduto
        boolean scaduto= annuncioScaduto(noticeListFiltered.get(position));

        if (scaduto) {
            //se l'annuncio è scaduto setto la visibilità a VISIBLE della textView
            if (holder.scad.getVisibility() == View.GONE) {
                holder.scad.setVisibility(View.VISIBLE);
            }
        }



    }

    public boolean annuncioScaduto(Notice notice){
        //trasformo la data di oggi in una stringa in forma AAAA-MM-GG
        Calendar c = Calendar.getInstance();
        int anno = c.get(Calendar.YEAR);
        int mese = c.get(Calendar.MONTH) + 1; //+1 perchè Calendar legge i mesi da 0 a 11
        int giorno = c.get(Calendar.DATE);
        String annoS = String.valueOf(anno);
        String meseS = String.valueOf(mese);
        String giornoS = String.valueOf(giorno);

        //controllo se la data di oggi ha bisogno di uno 0 quando il giorno o il numero è di una sola cifra
        String numero0 = "0";

        String dataOggi = annoS.concat(meseS).concat(giornoS);
        if (meseS.length() == 1) {
            dataOggi = annoS.concat(numero0).concat(meseS).concat(giornoS);
        }
        if (giornoS.length() == 1) {
            dataOggi = annoS.concat(meseS).concat(numero0).concat(giornoS);
        }

        if (meseS.length() == 1 && giornoS.length() == 1) {
            dataOggi = annoS.concat(numero0).concat(meseS).concat(numero0).concat(giornoS);
        }

        //trasformo la data dell'annuncio in una stringa in forma AAAA-MM-GG
        String data = notice.getDate();
        String annoNotice, meseNotice, giornoNotice;
        annoNotice = String.valueOf(data.substring(6, 10));
        meseNotice = String.valueOf(data.substring(3, 5));
        giornoNotice = String.valueOf(data.substring(0, 2));
        String dataNoTrattini = annoNotice.concat(meseNotice).concat(giornoNotice);

        //trasformo le date in formato stringa in un intero
        Integer dataNoticeInt = Integer.valueOf(dataNoTrattini);
        Integer dataOggiInt = Integer.valueOf(dataOggi);

        boolean scaduto = false;

        //confronto le date per sapere se un annuncio è scaduto
        if (dataNoticeInt < dataOggiInt){
            scaduto = true;
        }

        return scaduto;
    }

    //restutuisce il numero degli elementi della lista
    @Override
    public int getItemCount() {
        return noticeListFiltered.size();
    }

    //interfaccia di comunicazione tra adapter e listView
    public interface NoticeAdapterListener {
        void onNoticeSelected(Notice notice);
    }


}

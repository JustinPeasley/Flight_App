package edu.commonwealthu.flight_app;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class Adapter extends RecyclerView.Adapter<Adapter.ViewHolder> {

    private final LayoutInflater layoutInflater;
    private final List<String> data;

    Adapter(Context context, List<String> data){
        this.layoutInflater = LayoutInflater.from(context);
        this.data = data;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        View view = layoutInflater.inflate(R.layout.card, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int i) {

        i=i*3;  //used to access right ith information in the list

        //bind the textview with the data received
        String dep = data.get(i++);
        ViewHolder.textDepart.setText(dep);

        String arr = data.get(i++);
        ViewHolder.textArrival.setText(arr);

        String date = data.get(i++);
        ViewHolder.textDate.setText(date);

    }

    @Override
    public int getItemCount() {
        return data.size()/3;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{

        @SuppressLint("StaticFieldLeak")
        static TextView textDepart;
        @SuppressLint("StaticFieldLeak")
        static TextView textArrival;
        @SuppressLint("StaticFieldLeak")
        static TextView textDate;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textDepart = itemView.findViewById(R.id.departure_icac);
            textArrival= itemView.findViewById(R.id.arrival_icac);
            textDate   = itemView.findViewById(R.id.card_date);
        }
    }
}

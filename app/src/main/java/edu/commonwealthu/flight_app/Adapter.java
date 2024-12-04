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

    /**
     * constructor takes context for layout inflation and data set
     * @param context context to dislpay info in
     * @param data Arraylist of data (Strings)
     */
    Adapter(Context context, List<String> data){
        this.layoutInflater = LayoutInflater.from(context);
        this.data = data;
    }

    /**
     * creates each ViewHolder that holds current view being worked on
     * @param viewGroup The ViewGroup into which the new View will be added after it is bound to
     *               an adapter position.
     * @param viewType The view type of the new View.
     *
     * @return
     */
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        View view = layoutInflater.inflate(R.layout.card, viewGroup, false);
        return new ViewHolder(view);
    }

    /**
     * gets the data from the dataset and assigns it to its appropriate position
     * @param holder The ViewHolder which should be updated to represent the contents of the
     *        item at the given position in the data set.
     * @param i The position of the item within the adapter's data set.
     */
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

    /**
     * scales the data set size to number of cards to display
     * @return numbers of cards to create
     */
    @Override
    public int getItemCount() {
        return data.size()/3;
    }

    /**
     * Used for setting data to the current view holder being constructed (current card)
     */
    public static class ViewHolder extends RecyclerView.ViewHolder{

        static TextView textDepart;
        static TextView textArrival;
        static TextView textDate;

        /**
         * set data directly to its related object on the card
         * @param itemView takes itemView (card)
         */
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textDepart = itemView.findViewById(R.id.departure_icac);
            textArrival= itemView.findViewById(R.id.arrival_icac);
            textDate   = itemView.findViewById(R.id.card_date);
        }
    }
}

package edu.commonwealthu.flight_app;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONException;

import java.util.List;

public class Adapter extends RecyclerView.Adapter<Adapter.ViewHolder> {

    private final LayoutInflater layoutInflater;
    private final List<String> data;
    private static OnItemClickListener listener;

    public interface  OnItemClickListener {
        void onItemClick(int position) throws JSONException;
    }

    /**
     * constructor takes context for layout inflation and data set
     * @param context context to dislpay info in
     * @param data Arraylist of data (Strings)
     */
    public Adapter(Context context, List<String> data, OnItemClickListener listener){
        this.layoutInflater = LayoutInflater.from(context);
        this.data = data;
        Adapter.listener = listener;
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
        Log.d("Adapter", "onBindViewHolder:");
        i=i*4;  //used to access right ith information in the list

        //bind the textview with the data received
        String fnum = data.get(i++);
        ViewHolder.textFnum.setText(fnum);

        String dep = data.get(i++);
        ViewHolder.textDepart.setText(dep);

        String arr = data.get(i++);
        ViewHolder.textArrival.setText(arr);

        String date = data.get(i++);
        ViewHolder.textDate.setText(date);


        // Add a click animation for when specific cardview is selected
        holder.itemView.setOnClickListener(v -> {
            clickAnimation(holder.itemView);
            int position = holder.getAdapterPosition();
            if (position != RecyclerView.NO_POSITION) {
                try {
                    listener.onItemClick(position);
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    /**
     * scales the data set size to number of cards to display
     * @return numbers of cards to create
     */
    @Override
    public int getItemCount() {
        return data.size()/4;
    }

    private void clickAnimation(View itemView) {
        ScaleAnimation scaleAnimation = new ScaleAnimation(
                1.0f, 1.05f,  // Scale from 100% to 105% in X
                1.0f, 1.05f,  // Scale from 100% to 105% in Y
                Animation.RELATIVE_TO_SELF, 0.5f,  // Pivot at center X
                Animation.RELATIVE_TO_SELF, 0.5f   // Pivot at center Y
        );
        scaleAnimation.setDuration(150); // Duration in milliseconds
        scaleAnimation.setRepeatMode(Animation.REVERSE);
        scaleAnimation.setRepeatCount(1); // Reverse animation after scaling
        itemView.startAnimation(scaleAnimation);
    }


    /**
     * Used for setting data to the current view holder being constructed (current card)
     */
    public static class ViewHolder extends RecyclerView.ViewHolder{

        static TextView textFnum;
        static TextView textDepart;
        static TextView textArrival;
        static TextView textDate;

        /**
         * set data directly to its related object on the card
         * @param itemView takes itemView (card)
         */
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textFnum   = itemView.findViewById(R.id.card_fnum);
            textDepart = itemView.findViewById(R.id.departure_icac);
            textArrival= itemView.findViewById(R.id.arrival_icac);
            textDate   = itemView.findViewById(R.id.card_date);

            itemView.setOnClickListener(v ->  {
                Log.d("TAG", "ViewHolder: I'M IN ADAPTER");
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    try {
                        listener.onItemClick(position);
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                }
            });
        }
    }

    public void deleteItem(int position) {
        data.remove(position+3);
        data.remove(position+2);
        data.remove(position+1);
        data.remove(position);
        notifyItemRangeRemoved(position,position+4);
    }
}

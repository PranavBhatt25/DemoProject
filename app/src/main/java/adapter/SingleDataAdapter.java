package adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import holders.AllDataHolder;
import holders.SingleDataHolder;
import interfaces.OnItemClickListener;
import models.AllDataClass;
import models.SingleDataClass;
import project.demo.com.demoproject.R;
import utils.ConnectionDetector;


/**
 * Created by WPA2 on 8/18/2017.
 */

public class SingleDataAdapter extends RecyclerView.Adapter<SingleDataHolder> {

    Typeface typefaceRegular;
    OnItemClickListener listener;
    ConnectionDetector mConnectionDetector;
    private Context context;
    private LayoutInflater infalter;
    private ArrayList<SingleDataClass> allDataClasses;
    private View view;

    public SingleDataAdapter(Context context, ArrayList<SingleDataClass> arrayList) {
        this.context = context;
        this.listener = listener;
        infalter = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        allDataClasses = arrayList;

        mConnectionDetector = new ConnectionDetector(context);
        typefaceRegular = Typeface.createFromAsset(context.getAssets(), "fonts/Montserrat-Regular.ttf");
    }

    @Override
    public SingleDataHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        view = infalter.inflate(R.layout.adapter_item_display_all_data, parent, false);

        SingleDataHolder singleDataHolder = new SingleDataHolder(view);
        return singleDataHolder;
    }

    @Override
    public void onBindViewHolder(SingleDataHolder holder, int position) {
        final int pos = position;
        SingleDataClass singleDataClass = allDataClasses.get(position);

        final String mID = singleDataClass.getId();
        String mName = singleDataClass.getName();
        String mDescription = singleDataClass.getDescription();
        final String mCreatedDate = singleDataClass.getCreated_at();

        if (mName != null && !mName.equals("")) {
            holder.tv_name.setText(mName);
            holder.tv_name.setTypeface(typefaceRegular);
        }

        if (mDescription != null && !mDescription.equals("")) {
            holder.tv_description.setText(mDescription);
            holder.tv_description.setTypeface(typefaceRegular);
        }
    }

    @Override
    public int getItemCount() {
        return allDataClasses.size();
    }
}

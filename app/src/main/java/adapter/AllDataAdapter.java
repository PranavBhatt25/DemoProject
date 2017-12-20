package adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import activities.SingleDataDisplayActivity;
import holders.AllDataHolder;
import interfaces.OnItemClickListener;
import models.AllDataClass;
import project.demo.com.demoproject.R;
import utils.ConnectionDetector;


/**
 * Created by WPA2 on 8/18/2017.
 */

public class AllDataAdapter extends RecyclerView.Adapter<AllDataHolder> {

    Typeface typefaceRegular;
    OnItemClickListener listener;
    ConnectionDetector mConnectionDetector;
    private Context context;
    private LayoutInflater infalter;
    private ArrayList<AllDataClass> allDataClasses;
    private View view;

    public AllDataAdapter(Context context, ArrayList<AllDataClass> arrayList) {
        this.context = context;
        this.listener = listener;
        infalter = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        allDataClasses = arrayList;

        mConnectionDetector = new ConnectionDetector(context);
        typefaceRegular = Typeface.createFromAsset(context.getAssets(), "fonts/Montserrat-Regular.ttf");
    }

    @Override
    public AllDataHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        view = infalter.inflate(R.layout.adapter_item_display_all_data, parent, false);

        AllDataHolder allDataHolder = new AllDataHolder(view);
        return allDataHolder;
    }

    @Override
    public void onBindViewHolder(AllDataHolder holder, int position) {
        final int pos = position;
        AllDataClass allDataClass = allDataClasses.get(position);

        final String mID = allDataClass.getId();
        final String mName = allDataClass.getName();
        final String mDescription = allDataClass.getDescription();
        final String mCreatedDate = allDataClass.getCreated_at();

        if (mName != null && !mName.equals("")) {
            holder.tv_name.setText(mName);
            holder.tv_name.setTypeface(typefaceRegular);
        }

        if (mDescription != null && !mDescription.equals("")) {
            holder.tv_description.setText(mDescription);
            holder.tv_description.setTypeface(typefaceRegular);
        }

        holder.ll_main_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, SingleDataDisplayActivity.class);
                intent.putExtra("mID", mID);
                intent.putExtra("mName", mName);
                intent.putExtra("mDescription", mDescription);
                intent.putExtra("mcreatedDate", mCreatedDate);
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return allDataClasses.size();
    }
}

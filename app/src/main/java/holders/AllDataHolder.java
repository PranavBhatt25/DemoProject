package holders;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import interfaces.OnItemClickListener;
import models.AllDataClass;
import project.demo.com.demoproject.R;

/**
 * Created by WPA2 on 7/29/2017.
 */

public class AllDataHolder extends RecyclerView.ViewHolder {

    public TextView tv_name, tv_description;
    public LinearLayout ll_main_layout;

    public AllDataHolder(View itemView) {
        super(itemView);

        ll_main_layout = (LinearLayout) itemView.findViewById(R.id.ll_main_layout);
        tv_name = (TextView) itemView.findViewById(R.id.tv_name);
        tv_description = (TextView) itemView.findViewById(R.id.tv_description);
    }
}

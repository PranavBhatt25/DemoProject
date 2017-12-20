package activities;

import android.app.Activity;
import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.NetworkResponse;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import adapter.SingleDataAdapter;
import models.SingleDataClass;
import project.demo.com.demoproject.R;
import utils.ApplicationClass;
import utils.Common;
import utils.ConnectionDetector;
import utils.Constant;
import utils.CrashReportHandler;
import utils.ServiceApi;

/**
 * Created by Pranav on 19/12/2017.
 */

public class SingleDataDisplayActivity extends Activity {

    private ConnectionDetector mConnectionDetector;
    private Context context;
    private Activity activity;
    private static final String TAG = "SingleDataActivity";
    private Typeface typefaceBold, typefaceRegular;
    private Toolbar toolbar;
    private TextView toolbar_title = null;
    private ArrayList<SingleDataClass> singleDataClassArrayList = new ArrayList<>();
    SingleDataAdapter singleDataAdapter;
    private static RecyclerView mRecyclerView = null;
    private TextView tvNoDataFound = null;
    public String mId = "", mName = "", mDescription = "", mcreatedDate = "";
    public TextView tv_name, tv_description;
    ImageView iv_toolbar_left = null;
    RelativeLayout rr_back = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        overridePendingTransition(R.anim.slid_in_right, R.anim.slid_out_left);
        setContentView(R.layout.activity_single_data_display);
        CrashReportHandler.attach(this);
        context = this;
        activity = this;
        mConnectionDetector = new ConnectionDetector(context);

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            mId = bundle.getString("mID");
            mName = bundle.getString("mName");
            mDescription = bundle.getString("mDescription");
            mcreatedDate = bundle.getString("mcreatedDate");
        }

        init();

//     i'm set a data in textview because Single.php not working API

//        try {
//            if (mConnectionDetector.isConnectingToInternet()) {
//                callApiForGetSingleData(mId);  // GET SINGLE DATA FROM CALL THIS API
//            } else {
//                Toast.makeText(context, getString(R.string.please_check_internet), Toast.LENGTH_SHORT).show();
//            }
//        } catch (Exception e) {
//            e.getStackTrace();
//        }
    }

    private void init() {
        try {
            typefaceBold = Typeface.createFromAsset(getAssets(), "fonts/Montserrat-Bold.ttf");
            typefaceRegular = Typeface.createFromAsset(getAssets(), "fonts/Montserrat-Regular.ttf");

            toolbar = (Toolbar) findViewById(R.id.toolbar);
            toolbar_title = (TextView) findViewById(R.id.toolbar_title);
            iv_toolbar_left = (ImageView) findViewById(R.id.iv_toolbar_left);
            rr_back = (RelativeLayout) findViewById(R.id.rr_back);
            rr_back.setVisibility(View.VISIBLE);
            toolbar_title.setText(getString(R.string.single_data_display));
            toolbar_title.setTypeface(typefaceRegular);

            mRecyclerView = (RecyclerView) findViewById(R.id.rv_all_data);
            mRecyclerView.setLayoutManager(new LinearLayoutManager(context));

            tvNoDataFound = (TextView) findViewById(R.id.tv_no_data_found);

            tv_name = (TextView) findViewById(R.id.tv_name);
            tv_description = (TextView) findViewById(R.id.tv_description);

            tv_name.setTypeface(typefaceRegular);
            tv_description.setTypeface(typefaceRegular);

            tv_name.setText(mName);
            tv_description.setText(mDescription);


            iv_toolbar_left.setImageResource(R.mipmap.back_icon);
            rr_back.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    finish();
                }
            });

        } catch (Exception e) {
            e.getStackTrace();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    /**
     * <b>Description</b> - Call API for get all data
     */

    private void callApiForGetSingleData(String mId) {

        Common.ProgressDialogShow(context, Constant.MESSAGE.PROGRESS_PLEASE_WAIT_MSG);
        Map<String, String> params = new HashMap<String, String>();

        params.put(ServiceApi.WEB_SERVICE_KEY.ID, mId);

        JsonObjectRequest request = new JsonObjectRequest(ServiceApi.URL.GET_SINGLE_DATA, new JSONObject(params),
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            parseJsonAppVersion(response);
                        } catch (Exception e) {
                            e.printStackTrace();
                            Common.ProgressDialogDismiss();
                        }
                    }
                },
                new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (error.networkResponse != null) {
                            int statusCode = error.networkResponse.statusCode;
                            NetworkResponse response = error.networkResponse;

                            Log.d("testerror", "" + statusCode + " " + response.data);
//                            Toast.makeText(context, Constant.MESSAGE.CONNECTION_TIMEOUT, Toast.LENGTH_LONG).show();
                            Common.displayCustomToast(activity, Constant.MESSAGE.CONNECTION_TIMEOUT, "short");
                        }
                        Common.ProgressDialogDismiss();
                    }
                }) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<String, String>();
                headers.put("User-agent", "Mozilla/5.0 (TV; rv:44.0) Gecko/44.0 Firefox/44.0");
                return headers;
            }
        };


        Common.setVolleyConnectionTimeout(request);
        ApplicationClass.getInstance().getRequestQueue().add(request);
    }

    /**
     * <b>Description</b> - Get back response for Ad ID API
     *
     * @param jsonObject - Pass API response
     */
    private void parseJsonAppVersion(JSONObject jsonObject) {
        try {
            Common.ProgressDialogDismiss();
            Log.i("get response", "get response" + jsonObject);
            if (jsonObject.toString().contains(Constant.JSON_KEY.ERROR_CODE)) {
                String status = jsonObject.getString(Constant.JSON_KEY.ERROR_CODE);
                String Message = jsonObject.getString(Constant.JSON_KEY.MESSAGE);

                if (status.equals("1")) {
                    Toast.makeText(context, Message, Toast.LENGTH_LONG).show();
                    JSONArray arr = jsonObject.getJSONArray("brand_list");

                    singleDataClassArrayList = new ArrayList<>();
                    for (int i = 0; i < arr.length(); i++) {
                        String id = arr.getJSONObject(i).getString(Constant.JSON_KEY.ID);
                        String name = arr.getJSONObject(i).getString(Constant.JSON_KEY.NAME);
                        String description = arr.getJSONObject(i).getString(Constant.JSON_KEY.DESCRIPTION);
                        String createdDate = arr.getJSONObject(i).getString(Constant.JSON_KEY.CREATED_AT);

                        SingleDataClass singleDataClass = new SingleDataClass();
                        singleDataClass.setId(id);
                        singleDataClass.setName(name);
                        singleDataClass.setDescription(description);
                        singleDataClass.setCreated_at(createdDate);
                        singleDataClassArrayList.add(singleDataClass);
                    }

                    if (singleDataClassArrayList.size() > 0) {
                        mRecyclerView.setVisibility(View.VISIBLE);
                        tvNoDataFound.setVisibility(View.GONE);
                        singleDataAdapter = new SingleDataAdapter(context, singleDataClassArrayList);
                        mRecyclerView.setAdapter(singleDataAdapter);
                    } else {
                        tvNoDataFound.setVisibility(View.VISIBLE);
                        mRecyclerView.setVisibility(View.GONE);
                    }


                    //                   long userInfo = DBConstant.dbHelper.insertUserInfo(DBConstant.sqliteDatabase, signUpDetailsClass);

                } else {
                    Toast.makeText(context, Message, Toast.LENGTH_LONG).show();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
    @Override
    protected void onPause() {
        super.onPause();
        overridePendingTransition(R.anim.slid_in_right, R.anim.slid_out_left);
    }
}


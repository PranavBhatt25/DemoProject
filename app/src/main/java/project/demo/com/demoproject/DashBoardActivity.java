package project.demo.com.demoproject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.NetworkResponse;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.getbase.floatingactionbutton.FloatingActionsMenu;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import adapter.AllDataAdapter;
import database.DBConstant;
import models.AllDataClass;
import utils.ApplicationClass;
import utils.Common;
import utils.ConnectionDetector;
import utils.Constant;
import utils.CrashReportHandler;
import utils.MultipartRequestSort;
import utils.ServiceApi;

public class DashBoardActivity extends Activity implements View.OnClickListener {
    private static final String TAG = "DashBoardActivity";
    public static Cursor cursor;
    private static RecyclerView mRecyclerView = null;
    ArrayList<AllDataClass> dataArraylistForSync = new ArrayList<>();
    AllDataAdapter allDataAdapter;
    com.getbase.floatingactionbutton.FloatingActionsMenu multiple_actions;
    com.getbase.floatingactionbutton.FloatingActionButton btn_sync, btn_insert_record;
    RelativeLayout view_blur;
    private ConnectionDetector mConnectionDetector;
    private Context context;
    private Activity activity;
    private Typeface typefaceBold, typefaceRegular;
    private Toolbar toolbar;
    private TextView toolbar_title = null;
    private ArrayList<AllDataClass> allDataClassArrayList = new ArrayList<>();
    private TextView tvNoDataFound = null;
    private boolean isSyncApiCall = false;

    public static int getScreenWidth(Activity activity) {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

        return displayMetrics.widthPixels;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        overridePendingTransition(R.anim.slid_in_right, R.anim.slid_out_left);
        setContentView(R.layout.activity_dashboard);
        CrashReportHandler.attach(this);
        context = this;
        activity = this;
        mConnectionDetector = new ConnectionDetector(context);
        init();
        onClickListener();


        try {
            if (mConnectionDetector.isConnectingToInternet()) {
                callApiForGetAllData();  // GET ALL DATA FROM CALL THIS API
            } else {
                Toast.makeText(context, getString(R.string.please_check_internet), Toast.LENGTH_SHORT).show();
                getAllDataList(DBConstant.dbHelper.getAllData(DBConstant.sqliteDatabase, ""));
            }
        } catch (Exception e) {
            e.getStackTrace();
        }
    }

    private void onClickListener() {
        btn_sync.setOnClickListener(this);
        btn_insert_record.setOnClickListener(this);
        multiple_actions.setOnClickListener(this);
    }

    private void init() {
        try {
            typefaceBold = Typeface.createFromAsset(getAssets(), "fonts/Montserrat-Bold.ttf");
            typefaceRegular = Typeface.createFromAsset(getAssets(), "fonts/Montserrat-Regular.ttf");

            toolbar = (Toolbar) findViewById(R.id.toolbar);
            toolbar_title = (TextView) findViewById(R.id.toolbar_title);
            toolbar_title.setText(getString(R.string.dashboard));
            toolbar_title.setTypeface(typefaceRegular);

            view_blur = (RelativeLayout) findViewById(R.id.view_blur);
            multiple_actions = (FloatingActionsMenu) findViewById(R.id.multiple_actions);
            btn_sync = (com.getbase.floatingactionbutton.FloatingActionButton) findViewById(R.id.btn_sync);
            btn_insert_record = (com.getbase.floatingactionbutton.FloatingActionButton) findViewById(R.id.btn_insert_record);
            mRecyclerView = (RecyclerView) findViewById(R.id.rv_all_data);
            mRecyclerView.setLayoutManager(new LinearLayoutManager(context));

            tvNoDataFound = (TextView) findViewById(R.id.tv_no_data_found);
        } catch (Exception e) {
            e.getStackTrace();
        }

        view_blur.setVisibility(View.GONE);

        multiple_actions.setOnFloatingActionsMenuUpdateListener(new FloatingActionsMenu.OnFloatingActionsMenuUpdateListener() {
            @Override
            public void onMenuExpanded() {
                view_blur.setVisibility(View.VISIBLE);

            }

            @Override
            public void onMenuCollapsed() {
                view_blur.setVisibility(View.GONE);

            }
        });
    }

    @Override
    public void onBackPressed() {
        dailog();
    }

    public void dailog() {
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        finish();
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        dialog.dismiss();
                        break;
                }
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(getString(R.string.app_name));
        builder.setMessage(getString(R.string.are_you_sre_to_exit)).setPositiveButton(getString(R.string.yes), dialogClickListener)
                .setNegativeButton(getString(R.string.no), dialogClickListener).show();
    }

    /**
     * <b>Description</b> - Call API for get all data
     */

    private void callApiForGetAllData() {
        if (!isSyncApiCall) {
            Common.ProgressDialogShow(context, Constant.MESSAGE.PROGRESS_PLEASE_WAIT_MSG);
        }
        Map<String, String> params = new HashMap<String, String>();

        JsonObjectRequest request = new JsonObjectRequest(ServiceApi.URL.GET_FETCH_ALL_DATA, new JSONObject(params),
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            parseJsonGetAllData(response);
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
    private void parseJsonGetAllData(JSONObject jsonObject) {
        try {
            Common.ProgressDialogDismiss();
            Log.i("get response", "get response" + jsonObject);
            if (jsonObject.toString().contains(Constant.JSON_KEY.ERROR_CODE)) {
                String status = jsonObject.getString(Constant.JSON_KEY.ERROR_CODE);
                String Message = jsonObject.getString(Constant.JSON_KEY.MESSAGE);

                if (status.equals("1")) {
                    Toast.makeText(context, Message, Toast.LENGTH_LONG).show();

                    JSONArray arr = jsonObject.getJSONArray("brand_list");
                    allDataClassArrayList = new ArrayList<>();
                    for (int i = 0; i < arr.length(); i++) {
                        String id = arr.getJSONObject(i).getString(Constant.JSON_KEY.ID);
                        String name = arr.getJSONObject(i).getString(Constant.JSON_KEY.NAME);
                        String description = arr.getJSONObject(i).getString(Constant.JSON_KEY.DESCRIPTION);
                        String createdDate = arr.getJSONObject(i).getString(Constant.JSON_KEY.CREATED_AT);

                        AllDataClass allDataClass = new AllDataClass();
                        allDataClass.setId(id);
                        allDataClass.setName(name);
                        allDataClass.setDescription(description);
                        allDataClass.setCreated_at(createdDate);
                        allDataClass.setIs_Updated("1");
                        allDataClassArrayList.add(allDataClass);

                        long userInfo = DBConstant.dbHelper.insertUserInfo(DBConstant.sqliteDatabase, allDataClass);
                    }

                    getAllDataList(DBConstant.dbHelper.getAllData(DBConstant.sqliteDatabase, ""));
                    isSyncApiCall = false;

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

    @Override
    public void onClick(View view) {
        if (view.equals(btn_insert_record)) {
            if (multiple_actions != null && multiple_actions.isExpanded()) {
                multiple_actions.collapse();
            }
            insertDataDialog();    // INSERT DATA DIALOG
        } else if (view.equals(btn_sync)) {
            if (multiple_actions != null && multiple_actions.isExpanded()) {
                multiple_actions.collapse();
            }

            try {
                if (mConnectionDetector.isConnectingToInternet()) {
                    //fetch data from databse for sync data
                    dataArraylistForSync = new ArrayList<>();
                    dataArraylistForSync = DBConstant.dbHelper.getDataForSync(DBConstant.sqliteDatabase);

                    if (dataArraylistForSync.size() > 0) {
                        callApiForInsertAndSyncData();   // Insert DATA API AND SYNC DATA
                    } else {
                        Toast.makeText(context, "All data is Already Sync ,Please add some more data to sync", Toast.LENGTH_LONG).show();
                    }

                } else {
                    Toast.makeText(context, getString(R.string.please_check_internet), Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                e.getStackTrace();
            }
        }
    }

    // Insert Data Dialog
    public void insertDataDialog() {
        final Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_insert_record);

        try {
            TextView tv_dialog_title_main = (TextView) dialog.findViewById(R.id.tv_dialog_title_main);

            final EditText et_dialog_insert_name = (EditText) dialog.findViewById(R.id.et_dialog_insert_name);
            final EditText et_dialog_insert_description = (EditText) dialog.findViewById(R.id.et_dialog_insert_description);

            et_dialog_insert_name.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
            et_dialog_insert_description.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);

            Button btn_dialog_insert_record = (Button) dialog.findViewById(R.id.btn_dialog_insert_record);
            Button btn_dialog_cancel = (Button) dialog.findViewById(R.id.btn_dialog_cancel);
            dialog.getWindow().setLayout((int) (getScreenWidth(DashBoardActivity.this)), ViewGroup.LayoutParams.WRAP_CONTENT);
            dialog.show();

            tv_dialog_title_main.setTypeface(typefaceRegular);
            et_dialog_insert_name.setTypeface(typefaceRegular);
            et_dialog_insert_description.setTypeface(typefaceRegular);
            btn_dialog_insert_record.setTypeface(typefaceRegular);
            btn_dialog_cancel.setTypeface(typefaceRegular);

            ImageView iv_close_icon = (ImageView) dialog.findViewById(R.id.iv_close_icon);
            iv_close_icon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dialog.dismiss();
                }
            });
            btn_dialog_cancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dialog.dismiss();
                }
            });

            btn_dialog_insert_record.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String insertedTextName = et_dialog_insert_name.getText().toString();
                    String insertedTextDescription = et_dialog_insert_description.getText().toString();

                    if (!insertedTextName.equals("null") && !insertedTextName.equals("")) {
                        if (!insertedTextDescription.equals("null") && !insertedTextDescription.equals("")) {
                            AllDataClass allDataClass = new AllDataClass();
                            allDataClass.setId("");
                            allDataClass.setName(insertedTextName);
                            allDataClass.setDescription(insertedTextDescription);
                            allDataClass.setCreated_at("");
                            allDataClass.setIs_Updated("0");
                            allDataClassArrayList.add(allDataClass);

                            long userInfo = DBConstant.dbHelper.insertUserInfo(DBConstant.sqliteDatabase, allDataClass);
                            Toast.makeText(context, context.getString(R.string.recored_inserted_sucessfully), Toast.LENGTH_SHORT).show();
                            getAllDataList(DBConstant.dbHelper.getAllData(DBConstant.sqliteDatabase, ""));

                            dialog.dismiss();

                        } else {
                            Toast.makeText(context, context.getString(R.string.please_enter_description), Toast.LENGTH_SHORT).show();
                        }

                    } else {
                        Toast.makeText(context, context.getString(R.string.please_enter_name), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (multiple_actions != null && multiple_actions.isExpanded()) {
            multiple_actions.collapse();
        }

    }

    public void getAllDataList(Cursor query) {
        cursor = query;
        if (cursor != null) {
            if (cursor.getCount() > 0) {
                if (cursor.moveToFirst()) {
                    allDataClassArrayList = new ArrayList<>();
                    do {
                        String id = cursor.getString(cursor.getColumnIndex(DBConstant.TBL_USER.ID));
                        String name = cursor.getString(cursor.getColumnIndex(DBConstant.TBL_USER.NAME));
                        String description = cursor.getString(cursor.getColumnIndex(DBConstant.TBL_USER.DESCRIPTION));
                        String createdDate = cursor.getString(cursor.getColumnIndex(DBConstant.TBL_USER.CREATED_AT));
                        String is_updated = cursor.getString(cursor.getColumnIndex(DBConstant.TBL_USER.IS_UPDATED));

                        AllDataClass allDataClass = new AllDataClass();
                        allDataClass.setId(id);
                        allDataClass.setName(name);
                        allDataClass.setDescription(description);
                        allDataClass.setCreated_at(createdDate);
                        allDataClass.setIs_Updated(is_updated);
                        allDataClassArrayList.add(allDataClass);

                    } while (cursor.moveToNext());
                }

                if (allDataClassArrayList.size() > 0) {
                    mRecyclerView.setVisibility(View.VISIBLE);
                    tvNoDataFound.setVisibility(View.GONE);
                    allDataAdapter = new AllDataAdapter(context, allDataClassArrayList);
                    mRecyclerView.setAdapter(allDataAdapter);
                } else {
                    tvNoDataFound.setVisibility(View.VISIBLE);
                    mRecyclerView.setVisibility(View.GONE);
                }
            }
        }
        if (!cursor.isClosed()) {
            cursor.close();
        }
    }

    /**
     * <b>Description</b> - Call API for Insert Data and Sync
     */

    private void callApiForInsertAndSyncData() {

        Common.ProgressDialogShow(context, Constant.MESSAGE.PROGRESS_PLEASE_WAIT_MSG);
        Map<String, String> params = new HashMap<String, String>();
        JSONArray jsonArrayRequest = new JSONArray();
        for (int i = 0; i < dataArraylistForSync.size(); i++) {
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("name", dataArraylistForSync.get(i).getName());
                jsonObject.put("description", dataArraylistForSync.get(i).getDescription());

                jsonArrayRequest.put(jsonObject);
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        JSONObject Obj = new JSONObject();
        try {
            Obj.put("brand", jsonArrayRequest);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        params.put("data", Obj.toString());
        Log.e("request", Obj.toString());

        Response.ErrorListener errorListener = new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                if (error.networkResponse != null) {
                    int statusCode = error.networkResponse.statusCode;
                    NetworkResponse response = error.networkResponse;

                    Log.d("testerror", "" + statusCode + " " + response.data);
                    Common.displayCustomToast(activity, Constant.MESSAGE.CONNECTION_TIMEOUT, "long");
                }
                Common.ProgressDialogDismiss();
            }
        };

        Response.Listener listener = new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    parseJsonInsertData(response);
                } catch (Exception e) {
                    e.printStackTrace();
                    Common.ProgressDialogDismiss();
                }
            }
        };
        MultipartRequestSort multipartRequestSort = new MultipartRequestSort(ServiceApi.URL.INSERT_DATA, errorListener, listener, params);
        Common.setVolleyConnectionTimeout(multipartRequestSort);
        ApplicationClass.getInstance().getRequestQueue().add(multipartRequestSort);

    }

    /**
     * <b>Description</b> - Get back response for Sync Data
     *
     * @param jsonObject - Pass API response
     */
    private void parseJsonInsertData(JSONObject jsonObject) {
        try {
            Common.ProgressDialogDismiss();
            Log.i("get response", "get response" + jsonObject);
            if (jsonObject.toString().contains(Constant.JSON_KEY.ERROR_CODE)) {
                String status = jsonObject.getString(Constant.JSON_KEY.ERROR_CODE);
                String Message = jsonObject.getString(Constant.JSON_KEY.MESSAGE);

                if (status.equals("1")) {
                    Toast.makeText(context, Message, Toast.LENGTH_LONG).show();

                    //call db method for update record that are sync
                    DBConstant.dbHelper.updateRecordsForSync(DBConstant.sqliteDatabase);

                    isSyncApiCall = true;
                    callApiForGetAllData();  // GET ALL DATA FROM CALL THIS API
                } else {
                    Toast.makeText(context, Message, Toast.LENGTH_LONG).show();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

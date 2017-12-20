package activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.view.WindowManager;
import android.widget.TextView;

import database.DBConstant;
import project.demo.com.demoproject.DashBoardActivity;
import project.demo.com.demoproject.R;
import utils.ConnectionDetector;
import utils.CrashReportHandler;

/**
 * Created by Pranav on 19/12/2017.
 */

public class SplashActivity extends Activity {

    // Splash screen timer
    private static int SPLASH_TIME_OUT = 3000;
    private Context context;
    private static final String TAG = "SplashActivity";
    private Typeface typefaceBold;
    private TextView tvProjectTitle;
    private ConnectionDetector mConnectionDetector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        overridePendingTransition(R.anim.slid_in_right, R.anim.slid_out_left);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        setContentView(R.layout.activity_splash);
        CrashReportHandler.attach(this);
        mConnectionDetector = new ConnectionDetector(context);
        context = this;
        DBConstant.openDatabase(this);

        init();
        closeSplashScreen();

    }

    private void init() {
        typefaceBold = Typeface.createFromAsset(getAssets(), "fonts/Montserrat-Bold.ttf");

        tvProjectTitle = (TextView) findViewById(R.id.tv_project_title);
        tvProjectTitle.setTypeface(typefaceBold);

    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void closeSplashScreen() {
        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                // int LoginStatus = AppPreference.getLoginPref(context, AppPreference.PREF_LOGIN, AppPreference.PREF_KEY.LOGIN);

                Intent i = new Intent(SplashActivity.this, DashBoardActivity.class);
                startActivity(i);
                finish();
            }
        }, SPLASH_TIME_OUT);
    }
}


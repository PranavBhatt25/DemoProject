package utils;

import android.app.Activity;
import android.app.Application;
import android.app.ListFragment;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.StrictMode;
import android.support.multidex.MultiDex;
import android.text.TextUtils;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;

import database.DBConstant;
import project.demo.com.demoproject.R;


public class ApplicationClass extends Application {
    public static final String TAG = ApplicationClass.class
            .getSimpleName();
    private static DisplayImageOptions displayImageOptions;
    private static ImageLoaderConfiguration config;
    private static ApplicationClass sInstance;
    public Handler mHandler;
    private Activity currentClass = null;
    private RequestQueue mRequestQueue;
    private ListFragment currentFragment = null;

    /**
     * Intitilize Image Loader
     *
     * @param context
     */
    public static void initImageLoader(Context context) {
        if (config == null) {
            config = new ImageLoaderConfiguration.Builder(context)
                    .threadPriority(Thread.NORM_PRIORITY - 2)
                    .denyCacheImageMultipleSizesInMemory()
                    .diskCacheFileNameGenerator(new Md5FileNameGenerator())
                    .diskCacheSize(100 * 1024 * 1024) // 50 Mb

                    .tasksProcessingOrder(QueueProcessingType.LIFO)
                    .writeDebugLogs() // Remove for release app
                    .build();
        }
        // Initialize ImageLoader with configuration.
        ImageLoader.getInstance().init(config);
    }

    /**
     * Using singleton pattern to get product detail pattern
     *
     * @param context
     * @return options to display product image
     */

    public static DisplayImageOptions getProductImageDisplayOption(Context context) {

        if (displayImageOptions == null) {
            displayImageOptions = new DisplayImageOptions.Builder()
                    .showImageForEmptyUri(R.mipmap.ic_launcher)
                    //.showImageOnLoading(R.drawable.small_placeholder)
                    .cacheInMemory(true)
                    .cacheOnDisk(true)
                    .bitmapConfig(Bitmap.Config.RGB_565)
                    .build();
            return displayImageOptions;
        } else {
            return displayImageOptions;
        }
    }

    public static synchronized ApplicationClass getInstance() {
        return sInstance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mRequestQueue = Volley.newRequestQueue(this);
        DBConstant.openDatabase(this);
        sInstance = this;
        initImageLoader(getApplicationContext());

        Log.d(TAG, "Setting up StrictMode policy checking.");
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                .detectAll()
                .penaltyLog()
                .build());

        StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                .detectAll()
                .penaltyLog()
                .build());
    }

    public RequestQueue getRequestQueue() {
        if (mRequestQueue == null) {
            mRequestQueue = Volley.newRequestQueue(getApplicationContext());
        }

        return mRequestQueue;
    }

    public <T> void addToRequestQueue(Request<T> req, String tag) {
        // set the default tag if tag is empty
        req.setTag(TextUtils.isEmpty(tag) ? TAG : tag);
        getRequestQueue().add(req);
    }

    public <T> void addToRequestQueue(Request<T> req) {
        req.setTag(TAG);
        getRequestQueue().add(req);
    }

    public void cancelPendingRequests(Object tag) {
        if (mRequestQueue != null) {
            mRequestQueue.cancelAll(tag);
        }
    }


    @Override
    public void onLowMemory() {
        super.onLowMemory();

        System.gc();

    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    public ListFragment getCurrentFragment() {
        return currentFragment;
    }

    public void setCurrentFragment(ListFragment currentFragment) {
        this.currentFragment = currentFragment;
    }
}

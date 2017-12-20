package utils;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.StateListDrawable;
import android.media.ExifInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.RelativeSizeSpan;
import android.text.style.SuperscriptSpan;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.RetryPolicy;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import project.demo.com.demoproject.R;


/**
 * Created by Pranav on 19/12/2017
 */
public class Common {

    public final static char[] hexArray = "0123456789abcdef".toCharArray();
    public static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 123;
    public static AlertDialog authProgressDialog;

    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        long factor = (long) Math.pow(10, places);
        value = value * factor;
        long tmp = Math.round(value);
        return (double) tmp / factor;
    }

    public static void halfNavigationDrawerOfScreen(Context context, View view) {

        Resources resources = context.getResources();
        float width = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 240, resources.getDisplayMetrics());
        DrawerLayout.LayoutParams params = (DrawerLayout.LayoutParams) view.getLayoutParams();
        params.width = (int) (width);
        params.height = DrawerLayout.LayoutParams.MATCH_PARENT;
        view.setLayoutParams(params);
    }

    public static String hashKey(String strValue) {
        String HMACKey = "";

        try {
            if (strValue.length() > 1000)
                strValue = strValue.substring(0, 1000);
            HMACKey = Common.hmacSha1(strValue, Constant.API_KEY);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        }
        return HMACKey;
    }

    public static int getOrientation(Context context, Uri photoUri) {
    /* it's on the external media. */
        Cursor cursor = context.getContentResolver().query(photoUri,
                new String[]{MediaStore.Images.ImageColumns.ORIENTATION}, null, null, null);

        if (cursor.getCount() != 1) {
            return -1;
        }

        cursor.moveToFirst();
        return cursor.getInt(0);
    }

    public static Bitmap getCorrectlyOrientedImage(Context context, Uri photoUri) throws IOException {
        InputStream is = context.getContentResolver().openInputStream(photoUri);
        BitmapFactory.Options dbo = new BitmapFactory.Options();
        dbo.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(is, null, dbo);
        is.close();

        int rotatedWidth, rotatedHeight;
        int orientation = getOrientation(context, photoUri);

        if (orientation == 90 || orientation == 270) {
            rotatedWidth = dbo.outHeight;
            rotatedHeight = dbo.outWidth;
        } else {
            rotatedWidth = dbo.outWidth;
            rotatedHeight = dbo.outHeight;
        }

        Bitmap srcBitmap;
        int MAX_IMAGE_WIDTH = 1024;
        int MAX_IMAGE_HEIGHT = 1024;
        is = context.getContentResolver().openInputStream(photoUri);
        if (rotatedWidth > MAX_IMAGE_WIDTH || rotatedHeight > MAX_IMAGE_HEIGHT) {
            float widthRatio = ((float) rotatedWidth) / ((float) MAX_IMAGE_WIDTH);
            float heightRatio = ((float) rotatedHeight) / ((float) MAX_IMAGE_HEIGHT);
            float maxRatio = Math.max(widthRatio, heightRatio);

            // Create the bitmap from file
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = (int) maxRatio;
            srcBitmap = BitmapFactory.decodeStream(is, null, options);
        } else {
            srcBitmap = BitmapFactory.decodeStream(is);
        }
        is.close();

    /*
     * if the orientation is not 0 (or -1, which means we don't know), we
     * have to do a rotation.
     */
        if (orientation > 0) {
            Matrix matrix = new Matrix();
            matrix.postRotate(orientation);

            srcBitmap = Bitmap.createBitmap(srcBitmap, 0, 0, srcBitmap.getWidth(),
                    srcBitmap.getHeight(), matrix, true);
        }

        return srcBitmap;
    }

    /**
     * @param context
     * @param view    Set awesome font
     */
    public static void setAwesomeFont(Context context, View view) {
        Typeface font = Typeface.createFromAsset(context.getAssets(), "fonts/fontawesome.ttf");
        if (view instanceof TextView) {
            ((TextView) view).setTypeface(font);
        } else if (view instanceof EditText) {
            ((EditText) view).setTypeface(font);
        }
    }

    /**
     * <b>Description</b> - Connection time out for calling API
     *
     * @param request
     */
    public static void setVolleyConnectionTimeout(JsonObjectRequest request) {
        int socketTimeout = 100000;//1 Meinute - change to what you want
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        request.setRetryPolicy(policy);
    }

    public static void setVolleyConnectionTimeout(JsonArrayRequest request) {
        int socketTimeout = 100000;//1 Meinute - change to what you want
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        request.setRetryPolicy(policy);
    }

    public static void setVolleyConnectionTimeout(StringRequest request) {
        int socketTimeout = 100000;//1 Meinute - change to what you want
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        request.setRetryPolicy(policy);
    }

    public static void setVolleyConnectionTimeout(MultipartRequestSort request) {
        int socketTimeout = 100000;//1 Meinute - change to what you want
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        request.setRetryPolicy(policy);
    }

    /**
     * <b>Description</> Dismiss Progress Dialog
     */
    public static void ProgressDialogDismiss() {
        if (authProgressDialog != null && authProgressDialog.isShowing()) {
            authProgressDialog.dismiss();
        }
    }


    /**
     * @param textview set underline to text within TextView
     */
    public static void setUnderLineToTextView(TextView textview) {
        textview.setPaintFlags(textview.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
    }

    /**
     * @param context
     * @param message
     * @return
     * @author Krunal
     * @description use to check internet newtwork connection if network
     * connection not available than alert for open network
     * settings
     */
    public static boolean isOnline(final Context context, boolean message) {
        try {
            ConnectivityManager mConnectivityManager = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo netInfo = mConnectivityManager.getActiveNetworkInfo();
            if (netInfo != null) {
                if (netInfo.isConnectedOrConnecting()) {
                    return true;
                }
            }

            if (message) {
                //    Common.makeToastMessage(context, context.getResources().getString(R.string.msg_internet_not_available), Toast.LENGTH_LONG).show();
                return false;
            }
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }


    // custom dialog
    public static void ProgressDialogShow(Context context, String message) {
        Typeface tfShruti = Typeface.createFromAsset(context.getAssets(), "fonts/Montserrat-Regular.ttf");
        if (authProgressDialog != null && authProgressDialog.isShowing()) {
            authProgressDialog.dismiss();
        }
//        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context);
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(new ContextThemeWrapper(context, R.style.AppTheme));
        final View dialogView = LayoutInflater.from(context).inflate(R.layout.custom_progress_dailog_layout, null);
        dialogBuilder.setView(dialogView);
        dialogBuilder.setCancelable(false);
        TextView tv_message = (TextView) dialogView.findViewById(R.id.tv_message);
        tv_message.setText(message);
        tv_message.setTypeface(tfShruti);
        authProgressDialog = dialogBuilder.create();
        authProgressDialog.show();

    }

    /**
     * @return TimeStamp of current date
     * @Description getCurrentTimesamp method use to get current date UNIX
     * timestamp
     */
    public static String getCurrentTimesamp() {
        try {
            long timestamp = System.currentTimeMillis() / 1000L;
            return String.valueOf(timestamp);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void setEffraLightFont(Context context, View view) {
        Typeface font = Typeface.createFromAsset(context.getAssets(), "fonts/shruti.ttf");
        if (view instanceof TextView) {
            ((TextView) view).setTypeface(font);
        } else if (view instanceof EditText) {
            ((EditText) view).setTypeface(font);
        } else if (view instanceof Button) {
            ((Button) view).setTypeface(font);
        }
    }


    /**
     * Return list of files from path. <FileName, FilePath>
     *
     * @param path - The path to directory with images
     * @return Files name and path all files in a directory, that have ext = "jpeg", "jpg","png", "bmp", "gif"
     */
    public static List<String> getListOfFiles(String path, final String view_pdf) {
        File files = new File(path);
        final List<String> exts;
        if (view_pdf.equals("true")) {
//            exts = Arrays.asList("jpeg", "jpg","png", "pdf");
            exts = Arrays.asList("pdf", "PDF", "txt", "TXT", "ODT", "docx", "DOC", "XLS", "XLSX", "xlsx", "PPT", "PPS", "PPTX", "pptx", "ppt");
        } else {
            exts = Arrays.asList("jpeg", "jpg",
                    "png");
        }

        FileFilter filter = new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                String ext;
                String path = pathname.getPath();
                ext = path.substring(path.lastIndexOf(".") + 1);
                return exts.contains(ext);
            }
        };

        final File[] filesFound = files.listFiles(filter);
        List<String> list = new ArrayList<String>();
        if (filesFound != null && filesFound.length > 0) {
            for (File file : filesFound) {
                list.add(file.getName());
                file.length();
            }
        }

        return list;
    }


    public static String getFileExt(String fileName) {
        return fileName.substring(fileName.lastIndexOf(".") + 1, fileName.length());
    }

    // this method will return file size
    public static String readableFileSize(long size) {
        if (size <= 0)
            return "0";
        final String[] units = new String[]{"B", "KB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        return new DecimalFormat("#,##0.#").format(size / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    }

    /**
     * @return TimeStamp of current date
     * @Description getCurrentTimesamp method use to get current date UNIX timestamp
     */
    public static long getCurrentTimesampLong() {
        try {
            long timestamp = System.currentTimeMillis() / 1000L;
            return timestamp;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    public static String changeDateFormat(String inputDate, String inputPattern, String outputPattern) {

        SimpleDateFormat inputFormat = new SimpleDateFormat(inputPattern);
//        inputFormat.setTimeZone(TimeZone.getTimeZone("PST"));

        SimpleDateFormat outputFormat = new SimpleDateFormat(outputPattern);

        Date date = null;
        String str = null;

        try {
            date = inputFormat.parse(inputDate);
            str = outputFormat.format(date);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return str;
    }

    /**
     * @param timeStamp
     * @param format
     * @return String (Date based on format)
     * @Description TimestempToDate method use to convert Unix TimeStamp to
     * specified date format from Constant.DATE_FORMAT class
     */
    public static final String TimestempToDate(String timeStamp, String format) {
        try {
            SimpleDateFormat mSimpleDateFormat = new SimpleDateFormat(format, Locale.getDefault());
            TimeZone mTimeZone = TimeZone.getDefault();
            mSimpleDateFormat.setTimeZone(mTimeZone);
            return mSimpleDateFormat.format(new Date(Long.parseLong(timeStamp) * 1000));
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static int getScreenWidth(Activity activity) {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

        return displayMetrics.widthPixels;
    }

    public static int getScreenHeight(Activity activity) {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

        return displayMetrics.heightPixels;
    }

    public static float dpToPixel(Activity activity, int dpi) {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

        return displayMetrics.density * dpi;

    }

    public static float pixelToDpi(Activity activity, int pixel) {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

        return pixel / displayMetrics.density;

    }

    // Decodes image and scales it to reduce memory consumption
    public static Bitmap decodeFile(File f) {
        try {
            // Decode image size
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(new FileInputStream(f), null, o);

            // The new size we want to scale to
            final int REQUIRED_SIZE = 100;

            // Find the correct scale value. It should be the power of 2.
            int scale = 1;
            while (o.outWidth / scale / 2 >= REQUIRED_SIZE && o.outHeight / scale / 2 >= REQUIRED_SIZE) {
                scale *= 2;
            }

            // Decode with inSampleSize
            BitmapFactory.Options o2 = new BitmapFactory.Options();
            o2.inSampleSize = scale;
            return BitmapFactory.decodeStream(new FileInputStream(f), null, o2);
        } catch (FileNotFoundException e) {
        }
        return null;
    }

    /**
     * <b>Description</b> - convert Base65Encode to String
     *
     * @param file
     * @return
     */
    public static String getBase64EnocdeString(File file) {
        try {
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            FileInputStream fis = new FileInputStream(file);
            byte[] bytes = new byte[1024];

            int n;
            while (-1 != (n = fis.read(bytes)))
                output.write(bytes, 0, n);

            byte[] audioBytes = output.toByteArray();

            return Base64.encodeToString(audioBytes, Base64.DEFAULT);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static boolean saveFile(File file, Bitmap bitmap) {
        if (bitmap != null) {
            File parentFile = new File(file.getParent());
            if (!parentFile.exists()) {
                parentFile.mkdir();
            }

            if (file.exists()) {
                file.delete();
            }
            try {
                FileOutputStream out = new FileOutputStream(file);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
                out.flush();
                out.close();
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
        return false;
    }

    /**
     * @param text
     * @param filename
     * @author Krunal
     * @description user to store string into text file
     */
    public static void saveTextFileToSD(String text, String path, String filename) {
        try {
            PrintWriter mPrintWriter = new PrintWriter(new FileWriter(new File(path, filename)));
            mPrintWriter.print(text);
            mPrintWriter.close();
            Log.i("SDCARD", "String Store In File");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String readTextFileFromAssets(Context context, String fileName) {
        BufferedReader reader = null;
        StringBuffer sbText = new StringBuffer("");
        try {
            reader = new BufferedReader(
                    new InputStreamReader(context.getAssets().open(fileName)));

            // do reading, usually loop until end of file reading
            String mLine = reader.readLine();
            while (mLine != null) {
                //process line
                sbText.append(mLine);
                mLine = reader.readLine();
            }
        } catch (IOException e) {
            //log the exception
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    //log the exception
                }
            }
        }
        return sbText.toString();
    }

    public static Toast makeToastMessage(Context context, String message, int duration) {
        Toast toast = Toast.makeText(context, message, duration);
        toast.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL, 0, 50);
        return toast;
    }

    public static StateListDrawable createButtonSelector(Context context, int defaultDrawable, int clickDrawable) {
        StateListDrawable stateListDrawable = new StateListDrawable();
        stateListDrawable.addState(new int[]{android.R.attr.state_pressed},
                context.getResources().getDrawable(clickDrawable));
        stateListDrawable.addState(new int[]{}, context.getResources().getDrawable(defaultDrawable));
        return stateListDrawable;
    }

    /**
     * @param context
     * @param fileName This method use to create file directory if file is created
     *                 then it return true, if not then return false.
     * @return Boolean
     */
    public static Boolean createFileDirectory(Context context, String fileName) {
        // String path = context.getFilesDir().toString();
        String path = Environment.getExternalStorageDirectory().toString();
        Log.d("Catabase Path", path);
        File file = new File(path, fileName);

        if (!file.exists())
            return file.mkdir();
        else
            return true;
    }

    /**
     * <b>Description</b> - get back to devise default timezone
     *
     * @return
     */
    public static final TimeZone getDefaultTimeZone() {
        Calendar cal = Calendar.getInstance();
        return cal.getTimeZone();
    }

    /***
     * <b>Description</b> - use to convert date to timestamp
     *
     * @param date
     * @param format
     * @return
     */
    public static final long dateToTimestamp(String date, String format) {
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat(
                    format);

            /*TimeZone utcZone = TimeZone.getDefault();*/
            dateFormat.setTimeZone(getDefaultTimeZone());

            Date parsedDate = dateFormat.parse(date);

            Timestamp timestamp = new Timestamp(
                    parsedDate.getTime());
            return timestamp.getTime() / 1000;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

     /**
     * @param startDate Timestamp
     * @param endDate   Timestamp
     * @return duration difference between startdate to enddate in the format of
     * 2 minutes ago, 4 days ago, 3 months ago, 1 year ago
     */
    public static String getDateTimeDurationDiffInStyle(long startDate, long endDate) {
        try {
            long diff = endDate - startDate;

            long diffSeconds = (diff * 1000) / 1000 % 60;
            long diffMinutes = (diff * 1000) / (60 * 1000) % 60;
            long diffHours = (diff * 1000) / (60 * 60 * 1000);
            int diffInDays = (int) ((diff * 1000) / (1000 * 60 * 60 * 24));
            int diffMonths = getMonthDiff(Long.valueOf(startDate), Long.valueOf(endDate));
            int month = 0;
            if (diffMonths < 0)
                month = (diffMonths - 1) + 12;

            int diffYears = getYearDiff(startDate, endDate);

//            Log.d("diffSeconds diffMinutes diffHours diffMonths diffYears",
//                    diffSeconds + " " + diffMinutes + " " + diffHours + " "
//                            + diffInDays + " " + diffMonths + " " + diffYears);

            if (diffYears > 0 && diffMonths > 0)
                return diffYears + " year" + ((diffYears == 1) ? "" : "s") + " ago";
            else if (diffMonths > 0 || month > 0)//(diffYears == 1 && diffMonths < 0 && diffMonths > -11))
            {
                //diffMonths = (diffYears == 1 && diffMonths < 0) ? diffMonths + 12 : diffMonths;
                diffMonths = (month > 0) ? month : diffMonths;
                return diffMonths + " month" + ((diffMonths == 1) ? "" : "s") + " ago";
            } else if (diffInDays > 0)// || ((diffMonths + 12) == 1))
                return diffInDays + " day" + ((diffInDays == 1) ? "" : "s") + " ago";
            else if (diffHours > 0)
                return diffHours + " hour" + ((diffHours == 1) ? "" : "s") + " ago";
            else if (diffMinutes > 0)
                return diffMinutes + " minute" + ((diffMinutes == 1) ? "" : "s") + " ago";
            else if (diffSeconds > 0)
                return diffSeconds + " second" + ((diffSeconds == 1) ? "" : "s") + " ago";
            else
                return "now";
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * @param startDate timestamp
     * @param endDate   timestamp
     * @return month difference between startDate to endDate
     */
    public static int getMonthDiff(long startDate, long endDate) {
        try {
            int startDateMonth = Integer.parseInt(Common.TimestampToDate(startDate, "MM"));
            int endDateMonth = Integer.parseInt(Common.TimestampToDate(endDate, "MM"));

            return endDateMonth - startDateMonth;
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * @param timeStamp
     * @param format
     * @return String (Date based on format)
     * @Description TimestempToDate method use to convert Unix TimeStamp to specified date format from
     * Constant.DATE_FORMAT class
     */
    public static final String TimestampToDate(long timeStamp, String format) {
        try {
            SimpleDateFormat mSimpleDateFormat = new SimpleDateFormat(format, Locale.getDefault());
            TimeZone mTimeZone = TimeZone.getDefault();
            mSimpleDateFormat.setTimeZone(mTimeZone);
            return mSimpleDateFormat.format(new Date(timeStamp * 1000));
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * @param startDate timestamp
     * @param endDate   timestamp
     * @return year difference between startDate to endDate
     */
    public static int getYearDiff(long startDate, long endDate) {
        try {
            int startDateYear = Integer.parseInt(Common.TimestampToDate(startDate, "yyyy"));
            int endDateYear = Integer.parseInt(Common.TimestampToDate(endDate, "yyyy"));

            return endDateYear - startDateYear;
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return 0;
        }
    }

    //    celsius, fahrenheit
    public static int convertCelsiusToFahrenheit(int celsius) {
        return (celsius * 9 / 5) + 32;
    }

    public static int convertFahrenheitToCelsius(int fahrenheit) {
        return (fahrenheit - 32) * 5 / 9;
    }

    /**
     * @return string
     * <p/>
     * Rinkesh
     * @Description getting the device id
     */

    public static String getDeviceId(Context context) {
        String deviceId = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);

        return deviceId;
    }

    /**
     * @param text
     * @param start
     * @param end
     * @return string
     * @Description superscript text
     */
    public static SpannableStringBuilder setSpannableSuperscript(String text, int start, int end) {
        SpannableStringBuilder cs = new SpannableStringBuilder(text);
        cs.setSpan(new SuperscriptSpan(), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        cs.setSpan(new RelativeSizeSpan(0.30f), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        return cs;
    }

    public static SpannableStringBuilder setSpannableSuperscriptDate(String text, int start, int end) {
        SpannableStringBuilder cs = new SpannableStringBuilder(text);
        cs.setSpan(new SuperscriptSpan(), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        cs.setSpan(new RelativeSizeSpan(0.40f), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        return cs;
    }

    public static void showLongToast(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
        // Toast toast = Toast.makeText(context, message, Toast.LENGTH_LONG);
        // toast.setGravity(Gravity.CENTER, 0, 0);

    }

    /**
     * <b>Description</b> - hide soft keyboard
     *
     * @param context
     * @param view
     */
    public static void hideKeyboard(Context context, View view) {
        InputMethodManager inputMethodManager = (InputMethodManager) context.getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_IMPLICIT_ONLY);
        inputMethodManager.hideSoftInputFromWindow(view.getApplicationWindowToken(), 0);
    }

    /**
     * @param monthNumber Month Number starts with 0. For <b>January</b> it is <b>0</b> and for <b>December</b> it is <b>11</b>.
     * @return
     */
    public static int getDaysInMonthInPresentYear(int monthNumber) {
        int days = 0;
        if (monthNumber >= 0 && monthNumber < 12) {
            try {
                Calendar calendar = Calendar.getInstance();
                int date = 1;
                int year = calendar.get(Calendar.YEAR);
                calendar.set(year, monthNumber, date);
                days = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
            } catch (Exception e) {
                if (e != null)
                    e.printStackTrace();
            }
        }
        return days;
    }

    public static void displayAlert(Context context, String respMsg) {
        try {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setCancelable(false);
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    dialog.dismiss();

                }
            });
            builder.setTitle("" + "Job Sheet");
            builder.setMessage(respMsg);
            builder.create().show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static int convertDpToPixel(int dp, Context context) {
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        int px = dp * ((int) metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT);
        return px;
    }

    /////// SECURITY FUNCTION
    // HMAC SHA-1 FUNCTION
    public static String hmacSha1(String value, String key) throws UnsupportedEncodingException,
            NoSuchAlgorithmException, InvalidKeyException {
        String type = "HmacSHA1";
        SecretKeySpec secret = new SecretKeySpec(key.getBytes(), type);
        Mac mac = Mac.getInstance(type);
        mac.init(secret);
        byte[] bytes = mac.doFinal(value.getBytes());
        return bytesToHex(bytes);
    }

    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        int v;
        for (int j = 0; j < bytes.length; j++) {
            v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    public final static boolean isValidEmail(CharSequence target) {
        if (target == null) {
            return false;
        } else {
            return android.util.Patterns.EMAIL_ADDRESS.matcher(target)
                    .matches();
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public static boolean checkPermission(final Context context) {
        int currentAPIVersion = Build.VERSION.SDK_INT;
        if (currentAPIVersion >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale((Activity) context, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    android.support.v7.app.AlertDialog.Builder alertBuilder = new android.support.v7.app.AlertDialog.Builder(context);
                    alertBuilder.setCancelable(true);
                    alertBuilder.setTitle("Permission necessary");
                    alertBuilder.setMessage("External storage permission is necessary");
                    alertBuilder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
                        }
                    });
                    android.support.v7.app.AlertDialog alert = alertBuilder.create();
                    alert.show();

                } else {
                    ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
                }
                return false;
            } else {
                return true;
            }
        } else {
            return true;
        }
    }

    public static DisplayImageOptions getDisplayImageOptions(Context context, int defaultImg) {
        DisplayImageOptions options = new DisplayImageOptions.Builder().showImageOnLoading(defaultImg)
                .resetViewBeforeLoading(true).showImageForEmptyUri(defaultImg).showImageOnFail(defaultImg)
                .cacheInMemory(true).cacheOnDisk(true).considerExifParams(true).bitmapConfig(Bitmap.Config.RGB_565)
                .considerExifParams(true).build();

        return options;
    }

    public static void showToast(Context context, String message) {
        Toast toast = Toast.makeText(context, message, Toast.LENGTH_SHORT);
        LinearLayout toastLayout = (LinearLayout) toast.getView();
        TextView toastTV = (TextView) toastLayout.getChildAt(0);
        toastTV.setTextSize(20);
        toast.show();
    }

    /**
     * <b>Description</>  display image in rounded shape
     *
     * @param context
     * @param defaultImg - pass id for imagenview
     * @param radius     - pass radius
     * @return - return DisplayImageOptions object
     */
    public static DisplayImageOptions getRoundedCornerDisplayImageOptions(Context context, int defaultImg, int radius) {
        DisplayImageOptions options = new DisplayImageOptions.Builder().showImageOnLoading(defaultImg)
                .resetViewBeforeLoading(true).showImageForEmptyUri(defaultImg).showImageOnFail(defaultImg)
                .displayer(new RoundedBitmapDisplayer(radius))
                .cacheInMemory(true).cacheOnDisk(true).considerExifParams(true).bitmapConfig(Bitmap.Config.RGB_565)
                .build();

        return options;
    }

    public static void displayCustomToast(Activity activity, String message, String length) {
        Typeface tfShruti = Typeface.createFromAsset(activity.getAssets(), "fonts/shruti.ttf");

        LayoutInflater inflater = activity.getLayoutInflater();

        View layout = inflater.inflate(R.layout.custom_layout,
                (ViewGroup) activity.findViewById(R.id.custom_toast_layout_id));
        // set a message
        TextView text = (TextView) layout.findViewById(R.id.tv_toast);
        text.setText(message);
        text.setTypeface(tfShruti);

        // Toast...
        Toast toast = new Toast(activity.getApplicationContext());
        toast.setGravity(Gravity.BOTTOM, 0, 0);
//        toast.setMargin(0,10);
//        toast.setGravity(Gravity.TOP | Gravity.LEFT, 40, 60);
//        toast.setDuration(Toast.LENGTH_LONG);

        if (length.equalsIgnoreCase("short")) {
            toast.setDuration(Toast.LENGTH_SHORT);
        } else {
            toast.setDuration(Toast.LENGTH_LONG);
        }
        toast.setView(layout);
        toast.show();
    }

    public static String getMimeType(String url) {
        String type = null;
        String extension = MimeTypeMap.getFileExtensionFromUrl(url);
        if (extension != null) {
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
        }
        return type;
    }

    public static byte[] getBytes(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
        int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];

        int len = 0;
        while ((len = inputStream.read(buffer)) != -1) {
            byteBuffer.write(buffer, 0, len);
        }
        return byteBuffer.toByteArray();
    }

    /**
     * Rotate an image if required.
     *
     * @param img           The image bitmap
     * @param selectedImage Image URI
     * @return The resulted Bitmap after manipulation
     */
    public static Bitmap rotateImageIfRequired(Context mContext, Bitmap img, Uri selectedImage) throws IOException {

        InputStream input = mContext.getContentResolver().openInputStream(selectedImage);
        ExifInterface ei;
        if (Build.VERSION.SDK_INT > 23)
            ei = new ExifInterface(input);
        else
            ei = new ExifInterface(selectedImage.getPath());

        int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                return rotateImage(img, 90);
            case ExifInterface.ORIENTATION_ROTATE_180:
                return rotateImage(img, 180);
            case ExifInterface.ORIENTATION_ROTATE_270:
                return rotateImage(img, 270);
            default:
                return img;
        }
    }

    private static Bitmap rotateImage(Bitmap img, int degree) {
        Matrix matrix = new Matrix();
        matrix.postRotate(degree);
        Bitmap rotatedImg = Bitmap.createBitmap(img, 0, 0, img.getWidth(), img.getHeight(), matrix, true);
        img.recycle();
        return rotatedImg;
    }

    public static String getAppDir() {
        String appDir = Common.getSDPath();
        appDir += "/" + "KhetiVadi";
        File file = new File(appDir);
        if (!file.exists()) {
            file.mkdir();
        }
        appDir += "/" + "videoCompress";
        file = new File(appDir);
        if (!file.exists()) {
            file.mkdir();
        }
        return appDir;
    }

    public static String getSDPath() {
        File sdDir;
        boolean sdCardExist = Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED); //判断sd卡是否存在
        if (sdCardExist) {
            sdDir = Environment.getExternalStorageDirectory(); //Get the root directory
            return sdDir.toString();
        } else {
            return null;
        }
    }

    /**
     * This function will take an URL as input and return the file name.
     * <p>Examples :</p>
     * <ul>
     * <li>http://example.com/a/b/c/test.txt -> test.txt</li>
     * <li>http://example.com/ -> an empty string </li>
     * <li>http://example.com/test.txt?param=value -> test.txt</li>
     * <li>http://example.com/test.txt#anchor -> test.txt</li>
     * </ul>
     *
     * @param url The input URL
     * @return The URL file name
     */
    public static String getFileNameFromUrl(URL url) {

        String urlString = url.getFile();

        return urlString.substring(urlString.lastIndexOf('/') + 1).split("\\?")[0].split("#")[0];
    }

    public static String getFileNameFromUri(Uri url) {

        String urlString = url.getPath();

        return urlString.substring(urlString.lastIndexOf('/') + 1).split("\\?")[0].split("#")[0];
    }

    // to get fileName from string path
    public static String getFileName(String path) {
        String myPath = path;
        return myPath.substring(myPath.lastIndexOf("/") + 1);
    }

    public static String getAppDirImage() {
        String appDir = Common.getSDPath();
        appDir += "/" + "KhetiVadi";
        File file = new File(appDir);
        if (!file.exists()) {
            file.mkdir();
        }
        appDir += "/" + "Media";
        file = new File(appDir);
        if (!file.exists()) {
            file.mkdir();
        }
        return appDir;
    }

    public static String formatFileSize(long size) {
        String hrSize = null;

        double b = size;
        double k = size / 1024.0;
        double m = ((size / 1024.0) / 1024.0);
        double g = (((size / 1024.0) / 1024.0) / 1024.0);
        double t = ((((size / 1024.0) / 1024.0) / 1024.0) / 1024.0);

        DecimalFormat dec = new DecimalFormat("0.0");

        if (t > 1) {
            hrSize = dec.format(t).concat(" TB");
        } else if (g > 1) {
            hrSize = dec.format(g).concat(" GB");
        } else if (m > 1) {
            hrSize = dec.format(m).concat(" MB");
        } else if (k > 1) {
            dec = new DecimalFormat("0");
            hrSize = dec.format(k).concat(" KB");
        } else {
            hrSize = dec.format(b).concat(" Bytes");
        }

        return hrSize;
    }

    /**
     * This method converts device specific pixels to density independent pixels.
     *
     * @param px      A value in px (pixels) unit. Which we need to convert into db
     * @param context Context to get resources and device specific display metrics
     * @return A float value to represent dp equivalent to px value
     */
    public static float convertPixelsToDp(float px, Context context) {
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        float dp = px / ((float) metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT);
        return dp;
    }

    /**
     * This method converts dp unit to equivalent pixels, depending on device density.
     *
     * @param dp      A value in dp (density independent pixels) unit. Which we need to convert into pixels
     * @param context Context to get resources and device specific display metrics
     * @return A float value to represent px equivalent to dp depending on device density
     */
    public static float convertDpToPixel(float dp, Context context) {
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        float px = dp * ((float) metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT);
        return px;
    }

    public static String getRealPathFromURI(Uri contentUri, Context context) {
        String[] proj = {MediaStore.Images.Media.DATA};
        CursorLoader loader = new CursorLoader(context, contentUri, proj, null, null, null);
        Cursor cursor = loader.loadInBackground();
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        String result = cursor.getString(column_index);
        cursor.close();
        return result;
    }

    /**
     * to get device screen height and width
     */
    public static DisplayMetrics getDeviceScreenHeightWidth(Context context) {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int height = displayMetrics.heightPixels;
        int width = displayMetrics.widthPixels;

        return displayMetrics;

    }

    public static Uri getImageContentUri(Context context, File imageFile) {
        String filePath = imageFile.getAbsolutePath();
        Cursor cursor = context.getContentResolver().query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                new String[]{MediaStore.Images.Media._ID},
                MediaStore.Images.Media.DATA + "=? ",
                new String[]{filePath}, null);
        if (cursor != null && cursor.moveToFirst()) {
            int id = cursor.getInt(cursor
                    .getColumnIndex(MediaStore.MediaColumns._ID));
            Uri baseUri = Uri.parse("content://media/external/images/media");
            return Uri.withAppendedPath(baseUri, "" + id);
        } else {
            if (imageFile.exists()) {
                ContentValues values = new ContentValues();
                values.put(MediaStore.Images.Media.DATA, filePath);
                return context.getContentResolver().insert(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            } else {
                return null;
            }
        }
    }

    /**
     * to delete all files from shareImage folder which are created for sharing purpose.
     */
    public static void deleteFilesFromShareDirectory() {
        String root = Environment.getExternalStorageDirectory().toString();
        File myDir = new File(root + "/shareImage");
        myDir.mkdirs();
        for (File file : myDir.listFiles())
            if (!file.isDirectory())
                file.delete();
    }

    /**
     * @param context
     * @param message
     * @return true if wifi connected or false
     */
    public boolean isWifiConnected(Context context, boolean message) {
        ConnectivityManager connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

        if (mWifi.isConnected()) {
            return true;
        } else {
            if (message) {
                // Common.makeToastMessage(context, context.getResources().getString(R.string.msg_internet_not_available), Toast.LENGTH_SHORT).show();
            }
        }

        return false;
    }

    // Decodes image and scales it to reduce memory consumption
    public Bitmap decodeBitmapByteArray(byte[] data) {
        try {
            // Decode image size
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
            BitmapFactory.decodeByteArray(data, 0, data.length, o);// (new
            // FileInputStream(f),
            // null, o);

            // The new size we want to scale to
            final int REQUIRED_SIZE = 100;

            // Find the correct scale value. It should be the power of 2.
            int scale = 1;
            while (o.outWidth / scale / 2 >= REQUIRED_SIZE && o.outHeight / scale / 2 >= REQUIRED_SIZE) {
                scale *= 2;
            }

            // Decode with inSampleSize
            BitmapFactory.Options o2 = new BitmapFactory.Options();
            o2.inSampleSize = scale;
            return BitmapFactory.decodeByteArray(data, 0, data.length, o2);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * @param bitmap
     * @return return byte[] if there is no error then return null Convert
     * bitmap object to byte[]
     */
    public byte[] convertBitmapToByteArray(Bitmap bitmap) {
        try {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
            return stream.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * @param context
     * @param assetsFile
     * @param destination
     * @param fileName    Copy assets file to storage directory
     */
    public void copyAssetsFileToFile(Context context, String assetsFile, String destination, String fileName) {
        AssetManager assetManager = context.getAssets();
        InputStream in = null;
        OutputStream out = null;
        try {
            File destFile = new File(destination);
            boolean isDirCreated = false;
            if (!(isDirCreated = destFile.exists())) {
                isDirCreated = destFile.mkdir();
            }

            if (isDirCreated) {
                in = assetManager.open(assetsFile);
                File outFile = new File(destination, fileName);
                if (!outFile.exists()) {
                    out = new FileOutputStream(outFile);
                    copyFile(in, out);
                }
            }
        } catch (IOException e) {
            Log.e("tag", "Failed to copy asset file: " + fileName, e);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    // NOOP
                }
            }
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    // NOOP
                }
            }
        }
    }

    /**
     * @param in
     * @param out
     * @throws IOException
     */
    public void copyFile(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int read;
        while ((read = in.read(buffer)) != -1) {
            out.write(buffer, 0, read);
        }
    }

}

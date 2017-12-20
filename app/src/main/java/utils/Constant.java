package utils;


import java.util.ArrayList;

/**
 * Created by Pranav on 19/12/2017.
 */
public class Constant {

    public static String API_KEY = "";

    public static final class MESSAGE {
        public static final String PROGRESS_PLEASE_WAIT_MSG = "Please wait...";
        public static final String SUCCESS = "Sucess";

        public static final String PROGRESS_AUTHENTICATION_MSG = "Authenticating...";
        public static final String INTERNET_NOT_AVAILABLE = "Internet Not Available";
        public static final String WIFI_NOT_CONNECTED = "Wifi Not Connected";
        public static final String CONNECTION_TIMEOUT = "Connection timeout";

    }

    public static final class JSON_KEY {

        public static String BRAND = "brand";
        public static String NAME = "name";
        public static String DESCRIPTION = "description";
        public static String ERROR_CODE = "error_code";
        public static String MESSAGE = "message";
        public static String BRAND_LIST = "brand_list";
        public static String ID = "id";
        public static String CREATED_AT = "created_at";
    }
}

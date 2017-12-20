package utils;

/**
 * Created by Pranav on 19/12/2017.
 */
public class ServiceApi {

    public static final class URL {


        public static final String BASE_URL = "http://appsdata2.cloudapp.net/demo/androidApi";

        public static final String GET_FETCH_ALL_DATA = BASE_URL + "/list.php";
        public static final String INSERT_DATA = BASE_URL + "/insert.php";
        public static final String GET_SINGLE_DATA = BASE_URL + "/single.php";
    }

    public static final class WEB_SERVICE_KEY {
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

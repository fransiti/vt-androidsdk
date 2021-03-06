package id.co.veritrans.sdk.example.utils;

/**
 * Created by shivam on 10/29/15.
 */
public class Constants {

    public static final String VT_CLIENT_KEY = "VT-client-Lre_JFh5klhfGefF";
    /*public static final String VT_SERVER_KEY = "VT-server-pVDm2d9BT3TIbDls_UEmt8wE";*/

    //public static final String GOOGLE_API_KEY="AIzaSyCNipOVxBARjFmPHzik42QN3zai4iMJhMg";
    public static final String SENT_TOKEN_TO_SERVER = "sentTokenToServer";
    public static final String REGISTRATION_COMPLETE = "registrationComplete";
    public static final String GCM_REGISTERED_TOKEN = "gcm_registered_token";


    public static final String PAYMENT_STATUS = "payment_status";


    /**
     * merchant server end point when application is running in debug mode.
     */
    public static final String BASE_URL_MERCHANT_FOR_DEBUG = "https://hangout.betas.in/veritrans/api/";
    /**
     * merchant server end point when application is running in release version.
     */
    public static final String BASE_URL_MERCHANT_FOR_RELEASE = "https://hangout.betas.in/veritrans/api/";



    public static final String CHECK_STATUS = "https://hangout.betas" +
            ".in/veritrans/api/paymentstatus";
    public static final String BEFORE_PAYMENT_ERROR = "https://hangout.betas" +
            ".in/veritrans/api/unfinish";
    public static final String USER_CANCEL = "https://hangout.betas" +
            ".in/veritrans/api/error";

    public static final int AMOUNT = 360000;
}

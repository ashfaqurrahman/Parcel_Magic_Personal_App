package com.airposted.bitoronbd.utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

/*import com.airposted.latest.ui.model.CartCountModel;
import com.airposted.latest.ui.model.SettingsModel;
import com.airposted.latest.ui.model.User;*/

import java.net.URI;
import java.net.URISyntaxException;
import java.text.DecimalFormat;
import java.util.Objects;

public class AppHelper {
    public static final String DETAILS_ID_KEY = "DETAILS_ID_KEY";
    public static final String DETAILS_KEY = "DETAILS_KEY";
    public static final String DETAILS_KEY1 = "DETAILS_KEY1";
    public static final String SHIPPING_KEY = "SHIPPING_KEY";
    public static final String TOTAL_KEY = "TOTAL_KEY";
    public static final String SUB_TOTAL_KEY = "SUB_TOTAL_KEY";
    public static final String DISCOUNT_KEY = "DISCOUNT_KEY";
    public static final String SETTINGS_DATA = "SETTINGS_DATA";
    public static final String OPEN_SCREEN_LOAD = "OPEN_SCREEN_LOAD";
    public static final String BUY_NOW = "Buy_NOW";
    public static final String BUY_NOW_PRODUCT = "BUY_NOW_PRODUCT";
    public static boolean isSearch = false;
    public static String tempClassName = "";
    public static final double airposted_lat = 23.786474;
    public static final double airposted_lng = 90.403455;
    public static final String latitude = "latitude";
    public static final String longitude = "longitude";
    public static String webviewTitle = "";
    public static final String TERMS = "https://airposted.com/terms";
    public static final String AIRPOSTED_LIVE_WEB = "https://airposted.com";
    public static final String PEIVACY_POLICY = "https://airposted.com/privacy-policy";
    public static final String TEMP_API = "https://airposted-temp.firebaseio.com/status.json";

    public static final int paging_limit = 12;
    public static final int raf_paging_limit = 150;
    public static final int MAX_ITEM_COUNT = 5;
    public static boolean isPhoneVerify = false;

    public static boolean isValidEmailAddress(String email) {
        String ePattern = "^[a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]+@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\])|(([a-zA-Z\\-0-9]+\\.)+[a-zA-Z]{2,}))$";
        java.util.regex.Pattern p = java.util.regex.Pattern.compile(ePattern);
        java.util.regex.Matcher m = p.matcher(email);
        return m.matches();
    }

    //public static int CURRENT_AIRPOSTED_TYPE = AirPostedType.SHOP.getValue();
    public static final String SEARCH_KEY = "SEARCH_KEY";
    //public static SettingsModel settingsModel;
    public static String countryCode = "BD";
    public static String currency = "$";
    public static final String LAST_CURRENCY = "LAST_CURRENCY";
    public static double currentCurrencyConversionRate = 1;

    public static void sentToBrowser(Context context, String url) {
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(url));
        context.startActivity(i);
    }

    //public static MutableLiveData<CartCountModel> cartCount = new MutableLiveData<>();
    public static int notficationCount = 0;

    public static String convertDecimal(double amount) {
        DecimalFormat dtime = new DecimalFormat("0.00");
        return dtime.format(amount);
    }

    public static long userId;
    public static boolean isEaringFragment = false;

    public static String urlShorter(String fullUrl) {
        String url = "";
        String finalUrl = fullUrl.trim();
        if (finalUrl != null) {
            URI uri = null;
            try {
                uri = new URI(finalUrl);
                url = Objects.requireNonNull(uri).getHost();
                if (url != null) {
                    url = url.startsWith("www.") ? url.substring(4) : url;
                }
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
        }

        return url;

    }

    /*public static boolean isPhoneVerify(User user) {
        if (user == null) {
            return false;
        } else {
            //Log.e("aaaaa", String.valueOf(!TextUtils.isEmpty(user.getPhone_verified()) && !user.getPhone_verified().equalsIgnoreCase("0") && !TextUtils.isEmpty(user.getContact())));
            return !TextUtils.isEmpty(user.getPhone_verified()) && !user.getPhone_verified().equalsIgnoreCase("0") && !TextUtils.isEmpty(user.getContact());
        }
    }*/

    public static String zeroRemove(String phoneNo) {
        if (phoneNo.startsWith("0")) {
            phoneNo = phoneNo.substring(1);
        }
        if (phoneNo.startsWith("880")) {
            phoneNo = phoneNo.substring(3);
        }
        return phoneNo;
    }

    public static boolean backButtonControl = false;
    public static final String isMenuSelected = "isMenuSelected";

    /*public static String getCountryPhone() {
        if (com.airposted.latest.ui.utils.AppHelper.countryCode.equalsIgnoreCase("BD")) {
            return "+880";
        } else if (com.airposted.latest.ui.utils.AppHelper.countryCode.equalsIgnoreCase("IN")) {
            return "+91";
        } else {
            return "+1";
        }
    }

    public static double getTravelerCommisionRate(double price){
        List<String> slots = Arrays.asList(com.airposted.latest.ui.utils.AppHelper.settingsModel.getAvg_traveler_commission_slot().split("\\s*,\\s*"));
        List<String> rates= Arrays.asList(com.airposted.latest.ui.utils.AppHelper.settingsModel.getAvg_traveler_commission_rate().split("\\s*,\\s*"));
        int pos=0;

        double currencyWisePrice=price;

        if (countryCode.equalsIgnoreCase("US")){
            currencyWisePrice=price*Double.parseDouble(com.airposted.latest.ui.utils.AppHelper.settingsModel.getConversionRateUsBd());
        }else if (countryCode.equalsIgnoreCase("IN")){
            currencyWisePrice=price*Double.parseDouble(com.airposted.latest.ui.utils.AppHelper.settingsModel.getConversionRateInBd());
        }

        for (int i=0; i<slots.size(); i++){
            double slot=Double.parseDouble(slots.get(i));
            if (slot<=currencyWisePrice){
                pos=i;
            }
        }
        return Double.parseDouble(rates.get(pos))/100;
    }*/

    public static String getAppVersionName(){
//        return BuildConfig.VERSION_NAME;
        return "1.5";
    }

}

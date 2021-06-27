package com.airposted.bohon.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import java.net.URI
import java.net.URISyntaxException
import java.text.DecimalFormat
import java.util.*
import java.util.regex.Pattern

/*import com.airposted.latest.ui.model.CartCountModel;
import com.airposted.latest.ui.model.SettingsModel;
import com.airposted.latest.ui.model.User;*/   object AppHelper {
    const val DETAILS_ID_KEY = "DETAILS_ID_KEY"
    const val DETAILS_KEY = "DETAILS_KEY"
    const val DETAILS_KEY1 = "DETAILS_KEY1"
    const val SHIPPING_KEY = "SHIPPING_KEY"
    const val TOTAL_KEY = "TOTAL_KEY"
    const val SUB_TOTAL_KEY = "SUB_TOTAL_KEY"
    const val DISCOUNT_KEY = "DISCOUNT_KEY"
    const val SETTINGS_DATA = "SETTINGS_DATA"
    const val OPEN_SCREEN_LOAD = "OPEN_SCREEN_LOAD"
    const val BUY_NOW = "Buy_NOW"
    const val BUY_NOW_PRODUCT = "BUY_NOW_PRODUCT"
    var isSearch = false
    var tempClassName = ""
    const val airposted_lat = 23.786474
    const val airposted_lng = 90.403455
    const val latitude = "latitude"
    const val longitude = "longitude"
    var webviewTitle = ""
    const val TERMS = "https://airposted.com/terms"
    const val AIRPOSTED_LIVE_WEB = "https://airposted.com"
    const val PEIVACY_POLICY = "https://airposted.com/privacy-policy"
    const val TEMP_API = "https://airposted-temp.firebaseio.com/status.json"
    const val paging_limit = 12
    const val raf_paging_limit = 150
    const val MAX_ITEM_COUNT = 5
    var isPhoneVerify = false
    fun isValidEmailAddress(email: String?): Boolean {
        val ePattern =
            "^[a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]+@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\])|(([a-zA-Z\\-0-9]+\\.)+[a-zA-Z]{2,}))$"
        val p = Pattern.compile(ePattern)
        val m = p.matcher(email)
        return m.matches()
    }

    //public static int CURRENT_AIRPOSTED_TYPE = AirPostedType.SHOP.getValue();
    const val SEARCH_KEY = "SEARCH_KEY"

    //public static SettingsModel settingsModel;
    var countryCode = "BD"
    var currency = "$"
    const val LAST_CURRENCY = "LAST_CURRENCY"
    var currentCurrencyConversionRate = 1.0
    fun sentToBrowser(context: Context, url: String?) {
        val i = Intent(Intent.ACTION_VIEW)
        i.data = Uri.parse(url)
        context.startActivity(i)
    }

    //public static MutableLiveData<CartCountModel> cartCount = new MutableLiveData<>();
    var notficationCount = 0
    fun convertDecimal(amount: Double): String {
        val dtime = DecimalFormat("0.00")
        return dtime.format(amount)
    }

    var userId: Long = 0
    var isEaringFragment = false
    fun urlShorter(fullUrl: String): String? {
        var url: String? = ""
        val finalUrl = fullUrl.trim { it <= ' ' }
        if (finalUrl != null) {
            var uri: URI? = null
            try {
                uri = URI(finalUrl)
                url = Objects.requireNonNull(uri).host
                if (url != null) {
                    url = if (url.startsWith("www.")) url.substring(4) else url
                }
            } catch (e: URISyntaxException) {
                e.printStackTrace()
            }
        }
        return url
    }

    fun zeroRemove(phoneNo: String): String {
        var phoneNo = phoneNo
        if (phoneNo.startsWith("0")) {
            phoneNo = phoneNo.substring(1)
        }
        if (phoneNo.startsWith("880")) {
            phoneNo = phoneNo.substring(3)
        }
        return phoneNo
    }

    var backButtonControl = false
    const val isMenuSelected = "isMenuSelected"

    //        return BuildConfig.VERSION_NAME;
    val appVersionName: String
        get() =//        return BuildConfig.VERSION_NAME;
            "1.5"

    const val help = "<b>How it works/ Help</b>" +
            "<br><br><b><u>Step 1</u></b><br>" +
            "<b>Send anything straightaway!</b><br>" +
            "Need to send something urgently? Use Airposted Parcel to send anything across the city in less than 24 hours. <br>" +
            "<br>" +

            "<b><u>Step 2</u></b><br>" +
            "<b>Parcel products & documents</b><br>" +
            "Choose from different parcel categories through our App & send it instantly.<br>" +
            "<br>" +

            "<b><u>Step 3</u></b><br>" +
            "<b>Hassle-free pickup</b><br>" +
            "Just provide us your pickup location; our delivery team will collect the parcel shortly thereafter. It’s absolutely hassle-free. <br>" +
            "<br>" +

            "<b><u>Step 4</u></b><br>" +
            "<b>Your parcel, our concern.</b><br>" +
            "Our properly vetted bike riders will handle your parcel efficiently by ensuring 100% safety. <br>" +
            "<br><br><b>FAQ</b>" +
            "<br><br><b>1. How do I request for a delivery?</b>" +
            "<br>- To request a delivery, you will have to select Send Parcel from the App. Then, you will have to select your pickup and drop-off location, fill in detailed information about the receiver of the item and select any one of the predefined product categories which is closest to the item you are sending. Then you can review information before you request for a delivery pickup, after which Airposted Parcel can find the nearest deliverer around you and send them your way." +
            "<br><br><b>2. How do I contact a deliverer? </b>" +
            "<br>- After accepting your request, you will see the deliverers’ name, picture and user rating appear on your phone screen, along with a phone icon. Press the phone icon beside the contact number to call the deliverer. " +
            "<br><br><b>3. How do I cancel a request?</b>" +
            "<br>- You can cancel the request up until the start of your delivery. To do this, you have to press “Cancel Request” " +
            "<br><br><b>4. How do I track my delivery?</b>" +
            "<br>- There are three stages in tracking your delivery. After the deliverer accepts your requests, your phone screen will show “Request Accepted”. The delivery status will change to “In Transit” the moment the deliverer starts the request. When the deliverer has successfully delivered your item, the status will change to “Delivered”. All throughout, your phone screen will display a phone icon to call the deliverer. Press on the phone icon to call the deliverer if you wish to know detailed location of your deliverer." +
            "<br><br><b>5. How do I know how much to pay?</b>" +
            "<br>- After you choose your pick-up, drop-off location, receiver information and product category, your phone screen will display Estimated charge that you have to pay the deliverer while handing them your item during pickup."

    const val termsConditions = "1. Airposted does not deliver fragile, liquid or perishable items, if you provide such, Airposted will not be liable for any damage issue. Moreover, respective customer will have to compensate if the damaged product damages any other product." +
            "<br><br>2. Airposted does not deliver gold, arms, currency, or anything prohibited by the prevailing post office Act of Bangladesh. If any such prohibited product is found in any parcel of the customer will be held responsible." +
            "<br><br>3. If you have any dispute over any delivered parcel, you must raise it within 3 days of the delivery. Airposted does not preserve proof of delivery for more than 3 days." +
            "<br><br>4. If any issue arises due to wrong entry or information provided from the end of customer, Airposted will not take any responsibility. " +
            "<br><br>5. You shall not intentionally or unintentionally cause or attempt to cause damage to the third party transportation provider."
}
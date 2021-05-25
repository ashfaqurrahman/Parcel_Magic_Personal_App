package com.airposted.bitoronbd.model

import java.io.Serializable

class SetParcel: Serializable {
    var recp_name = ""
    var recp_phone = ""
//    var recp_city = ""
//    var recp_zone = ""
//    var recp_area = ""
    var recp_address = ""
//    var pick_city = ""
//    var pick_zone = ""
//    var pick_area = ""
    var pic_name = ""
    var pic_phone = ""
    var pick_address = ""
    var personal_order_type = 0
    var sender_latitude = 0.0
    var sender_longitude = 0.0
    var receiver_latitude = 0.0
    var receiver_longitude = 0.0
    var distance = 0.0
    var item_type = 0
    var item_qty = ""
    var who_will_pay = 0
    var ssl_transaction_id = ""
    var invoice_no = ""
    var delivery_charge = 0.0
}
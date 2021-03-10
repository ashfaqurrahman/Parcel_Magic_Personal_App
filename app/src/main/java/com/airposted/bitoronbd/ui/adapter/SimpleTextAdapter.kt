package com.airposted.bitoronbd.ui.adapter

import android.content.Context
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import androidx.core.app.ActivityCompat
import com.airposted.bitoronbd.R
import com.airposted.bitoronbd.ui.data.MenuItemData
import com.airposted.bitoronbd.ui.widget.CursorWheelLayout.CycleWheelAdapter

class SimpleTextAdapter @JvmOverloads constructor(
    private val mContext: Context,
    menuItemDatas: List<MenuItemData>?,
    gravity: Int = Gravity.CENTER
) : CycleWheelAdapter() {
    private val mMenuItemDatas: List<MenuItemData>? = menuItemDatas
    private val mLayoutInflater: LayoutInflater = LayoutInflater.from(mContext)
    private val mGravity: Int = gravity
    override fun getCount(): Int {
        return mMenuItemDatas?.size ?: 0
    }

    override fun getView(parent: View, position: Int): View {
        val item = getItem(position)
        val root = mLayoutInflater.inflate(R.layout.wheel_menu_item, null, false)
        val textView = root.findViewById<View>(R.id.wheel_menu_item_tv) as TextView
        textView.visibility = View.VISIBLE
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10f)
        textView.text = item.mTitle
        if (textView.layoutParams is FrameLayout.LayoutParams) {
            (textView.layoutParams as FrameLayout.LayoutParams).gravity = mGravity
        }
        if (position == INDEX_SPEC) {
            textView.setTextColor(ActivityCompat.getColor(mContext, R.color.red))
        }
        return root
    }

    override fun getItem(position: Int): MenuItemData {
        return mMenuItemDatas!![position]
    }

    companion object {
        const val INDEX_SPEC = 9
    }

}
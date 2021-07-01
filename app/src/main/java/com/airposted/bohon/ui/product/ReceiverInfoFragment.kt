package com.airposted.bohon.ui.product

import android.Manifest
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.GridLayoutManager
import com.airposted.bohon.R
import com.airposted.bohon.databinding.FragmentReceiverInfoBinding
import com.airposted.bohon.ui.main.CommunicatorFragmentInterface
import com.airposted.bohon.utils.DividerDecorator
import com.airposted.bohon.utils.hideKeyboard
import com.airposted.bohon.utils.snackbar
import com.airposted.bohon.utils.textWatcher
import com.nabinbhandari.android.permissions.PermissionHandler
import com.nabinbhandari.android.permissions.Permissions
import java.lang.Boolean


class ReceiverInfoFragment : Fragment(), NumberClickListener {
    private lateinit var binding: FragmentReceiverInfoBinding
    var communicatorFragmentInterface: CommunicatorFragmentInterface? = null
    var dataAdapter: NumberListRecyclerViewAdapter? = null
    var listView: androidx.recyclerview.widget.RecyclerView? = null
    private lateinit var receiverName: String
    private lateinit var orderDialog: Dialog

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentReceiverInfoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        bindUI()
    }

    private fun bindUI() {
        var phoneNumber = ""
        communicatorFragmentInterface = context as CommunicatorFragmentInterface
        binding.next.setOnClickListener {
            hideKeyboard(requireActivity())
            if (binding.receiverName.text.isNotEmpty()) {
                val phone = binding.receiverPhone.text.toString()
                if (phone.startsWith("0")) {
                    if (phone.length == 10) {
                        binding.rootLayout.snackbar("Please provide a correct number")
                    } else {
                        phoneNumber = "+880" + phone.substring(1)
                        val fragment = ReceiverAddressFragment()
                        val bundle = Bundle()
                        bundle.putString("receiver_name", binding.receiverName.text.toString())
                        bundle.putString("receiver_phone", phoneNumber)
                        bundle.putInt("parcel_quantity", requireArguments().getInt("parcel_quantity"))
                        bundle.putInt("delivery_type", requireArguments().getInt("delivery_type"))
                        bundle.putInt("parcel_type", requireArguments().getInt("parcel_type"))
                        fragment.arguments = bundle
                        communicatorFragmentInterface!!.addContentFragment(fragment, true)
                    }
                } else if (phone.startsWith("1")) {

                    phoneNumber = "+880$phone"
                    val fragment = ReceiverAddressFragment()
                    val bundle = Bundle()
                    bundle.putString("receiver_name", binding.receiverName.text.toString())
                    bundle.putString("receiver_phone", phoneNumber)
                    bundle.putInt("parcel_quantity", requireArguments().getInt("parcel_quantity"))
                    bundle.putInt("delivery_type", requireArguments().getInt("delivery_type"))
                    bundle.putInt("parcel_type", requireArguments().getInt("parcel_type"))
                    fragment.arguments = bundle
                    communicatorFragmentInterface!!.addContentFragment(fragment, true)

                } else {
                    binding.rootLayout.snackbar("Please provide a correct number")
                }
            } else {
                binding.rootLayout.snackbar("Please enter receiver name")
            }
        }

        binding.back.setOnClickListener {
            hideKeyboard(requireActivity())
            requireActivity().onBackPressed()
        }

        textWatcher(requireActivity(), 9, binding.receiverPhone, binding.next)

        binding.contacts.setOnClickListener {
            requestContactPermission()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            val contactData: Uri? = data?.data
            val c: Cursor =
                requireActivity().contentResolver.query(contactData!!, null, null, null, null)!!
            if (c.moveToFirst()) {
                var phoneNumber = ""
                var emailAddress = ""
                val phonesList = ArrayList<String>()
                val name = c.getString(c.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME))
                val contactId = c.getString(c.getColumnIndex(ContactsContract.Contacts._ID))
                var hasPhone =
                    c.getString(c.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))
                hasPhone = if (hasPhone.equals("1", ignoreCase = true)) "true" else "false"
                if (Boolean.parseBoolean(hasPhone)) {
                    val phones: Cursor = requireActivity().contentResolver.query(
                        ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                        null,
                        ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + contactId,
                        null,
                        null
                    )!!
                    while (phones.moveToNext()) {
                        phoneNumber =
                            phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))

                        phonesList.add(phoneNumber)
                    }
                    phones.close()
                }

                // Find Email Addresses
                val emails: Cursor = requireActivity().contentResolver.query(
                    ContactsContract.CommonDataKinds.Email.CONTENT_URI,
                    null,
                    ContactsContract.CommonDataKinds.Email.CONTACT_ID + " = " + contactId,
                    null,
                    null
                )!!
                while (emails.moveToNext()) {
                    emailAddress =
                        emails.getString(emails.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA))
                }
                emails.close()

                if (phonesList.size > 1) {
                    openWhatToSendDialog(name, phonesList)
                } else {
                    binding.receiverName.setText(name)
                    binding.receiverPhone.setText(
                        when {
                            phoneNumber.startsWith("0") -> {
                                phoneNumber.substring(1).replace("-", "")
                            }
                            phoneNumber.startsWith("+") -> {
                                phoneNumber.substring(4).replace("-", "")
                            }
                            else -> {
                                phoneNumber
                            }
                        }
                    )
                }
            }
            c.close()
        }
    }

    private fun openWhatToSendDialog(name: String, phoneList: ArrayList<String>) {
        orderDialog = Dialog(requireContext())
        orderDialog.setContentView(R.layout.what_to_send_dialog)

        val nameTv = orderDialog.findViewById(R.id.name) as TextView
        listView = orderDialog.findViewById(R.id.lstContacts)

        receiverName = name

        nameTv.text = name
        dataAdapter = NumberListRecyclerViewAdapter(phoneList, this)

        listView.apply {
            this!!.setHasFixedSize(true)
            DefaultItemAnimator()
            adapter = dataAdapter
            layoutManager = GridLayoutManager(
                requireActivity(),
                1
            )
            addItemDecoration(DividerDecorator(requireActivity()))
        }

        //getContacts()

        orderDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        orderDialog.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        orderDialog.window?.attributes?.windowAnimations = R.style.DialogAnimation_2
        orderDialog.window?.attributes?.gravity = Gravity.CENTER
        orderDialog.setCancelable(true)
        orderDialog.show()
    }

    private fun getContacts() {

        val intent = Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI)
        startActivityForResult(intent, 1)

        /*val contentResolver: ContentResolver = requireActivity().contentResolver
            var contactId: String? = null
            var displayName: String? = null
            contactsInfoList = ArrayList<ContactsInfo>()
            val cursor: Cursor = requireActivity().contentResolver.query(
                ContactsContract.Contacts.CONTENT_URI,
                null,
                null,
                null,
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC"
            )!!
            if (cursor.count > 0) {
                while (cursor.moveToNext()) {
                    val hasPhoneNumber =
                        cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))
                            .toInt()
                    if (hasPhoneNumber > 0) {
                        val contactsInfo = ContactsInfo()
                        contactId =
                            cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID))
                        displayName =
                            cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME))
                        contactsInfo.contactId = contactId
                        contactsInfo.displayName = displayName
                        val phoneCursor: Cursor = requireActivity().contentResolver.query(
                            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                            null,
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                            arrayOf(contactId),
                            null
                        )!!
                        if (phoneCursor.moveToNext()) {
                            val phoneNumber =
                                phoneCursor.getString(phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
                            contactsInfo.phoneNumber = phoneNumber
                        }
                        phoneCursor.close()
                        (contactsInfoList as ArrayList<ContactsInfo>).add(contactsInfo)
                    }
                }
            }
            cursor.close()
            dataAdapter = MyCustomAdapter(requireActivity(), R.layout.contact_info, contactsInfoList)
            listView?.adapter = dataAdapter*/
    }

    fun requestContactPermission() {

        Permissions.check(
            requireActivity(),
            Manifest.permission.READ_CONTACTS,
            null,
            object : PermissionHandler() {
                override fun onGranted() {
                    getContacts()
                }

                override fun onDenied(
                    context: Context?,
                    deniedPermissions: java.util.ArrayList<String>?
                ) {
                    super.onDenied(context, deniedPermissions)
                }

                override fun onBlocked(
                    context: Context?,
                    blockedList: java.util.ArrayList<String>?
                ): kotlin.Boolean {
                    return super.onBlocked(context, blockedList)
                }
            })
    }

    override fun onItemClick(number: String) {
        binding.receiverName.setText(receiverName)
        binding.receiverPhone.setText(
            when {
                number.startsWith("0") -> {
                    number.substring(1).replace("-", "")
                }
                number.startsWith("+") -> {
                    number.substring(4).replace("-", "")
                }
                else -> {
                    number
                }
            }
        )
        orderDialog.dismiss()
    }
}
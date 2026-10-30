package com.example.data.repository

import android.content.Context
import android.net.Uri
import android.provider.ContactsContract

data class ContactItem(
    val name: String,
    val phoneNumber: String,
    val photoUri: String? = null
)

class ContactsHelper(private val context: Context) {

    private val nameCache = mutableMapOf<String, String>()

    fun resolveContactName(phoneNumber: String): String {
        val cleanNumber = phoneNumber.replace(Regex("[^0-9+]"), "")
        if (cleanNumber.isBlank()) return phoneNumber

        nameCache[cleanNumber]?.let { return it }

        return try {
            val uri = Uri.withAppendedPath(
                ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
                Uri.encode(phoneNumber)
            )
            val projection = arrayOf(ContactsContract.PhoneLookup.DISPLAY_NAME)
            val cursor = context.contentResolver.query(uri, projection, null, null, null)
            var resolvedName: String? = null
            cursor?.use {
                if (it.moveToFirst()) {
                    val nameIndex = it.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME)
                    if (nameIndex != -1) {
                        resolvedName = it.getString(nameIndex)
                    }
                }
            }
            val result = resolvedName ?: phoneNumber
            nameCache[cleanNumber] = result
            result
        } catch (e: Exception) {
            phoneNumber
        }
    }

    fun searchContacts(query: String): List<ContactItem> {
        val results = mutableListOf<ContactItem>()
        return try {
            val projection = arrayOf(
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.Phone.NUMBER,
                ContactsContract.CommonDataKinds.Phone.PHOTO_URI
            )
            val selection = if (query.isNotBlank()) {
                "${ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME} LIKE ? OR ${ContactsContract.CommonDataKinds.Phone.NUMBER} LIKE ?"
            } else null

            val selectionArgs = if (query.isNotBlank()) {
                arrayOf("%$query%", "%$query%")
            } else null

            val cursor = context.contentResolver.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                projection,
                selection,
                selectionArgs,
                "${ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME} ASC LIMIT 30"
            )

            cursor?.use {
                val nameIdx = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
                val numberIdx = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                val photoIdx = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_URI)

                while (it.moveToNext()) {
                    val name = if (nameIdx != -1) it.getString(nameIdx) ?: "" else ""
                    val number = if (numberIdx != -1) it.getString(numberIdx) ?: "" else ""
                    val photo = if (photoIdx != -1) it.getString(photoIdx) else null
                    if (number.isNotBlank()) {
                        results.add(ContactItem(name, number, photo))
                    }
                }
            }
            results
        } catch (e: Exception) {
            emptyList()
        }
    }
}

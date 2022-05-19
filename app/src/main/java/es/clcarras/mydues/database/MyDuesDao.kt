package es.clcarras.mydues.database

import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import es.clcarras.mydues.constants.COL_USERS
import es.clcarras.mydues.constants.COL_USER_DUES
import es.clcarras.mydues.model.MyDues

class MyDuesDao {

    private val userDues = Firebase.firestore
        .collection(COL_USERS)
        .document(Firebase.auth.currentUser!!.email!!)
        .collection(COL_USER_DUES)

    fun getMyDues() = userDues.get()

    fun newDues(myDues: MyDues) = userDues.add(myDues.toMap())

    fun updateDues(myDues: MyDues) = userDues.document(myDues.id!!).update(myDues.toMap())

    fun deleteDues(myDues: MyDues) {
        userDues.document(myDues.id!!).delete()
    }

}
package es.clcarras.mydues.database

import com.google.android.gms.tasks.Task
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import es.clcarras.mydues.constants.COLLECTION_USERS
import es.clcarras.mydues.constants.COLLECTION_USER_DUES
import es.clcarras.mydues.model.MyDues

class MyDuesDao {

    private val userDues = Firebase.firestore
        .collection(COLLECTION_USERS)
        .document(Firebase.auth.currentUser!!.email!!)
        .collection(COLLECTION_USER_DUES)

    fun getAllDocs(): Task<QuerySnapshot> {
        return userDues.get()
    }

    fun createDoc(myDues: MyDues): Task<DocumentReference> {
        return userDues.add(myDues.toMap())
    }

    fun updateDoc(myDues: MyDues): Task<Void> {
        return userDues.document(myDues.id!!).update(myDues.toMap())
    }

    fun deleteDoc(myDues: MyDues) {
        userDues.document(myDues.id!!).delete()
    }

}
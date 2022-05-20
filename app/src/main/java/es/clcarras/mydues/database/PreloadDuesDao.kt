package es.clcarras.mydues.database

import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import es.clcarras.mydues.constants.COL_PRELOAD_DUES
import es.clcarras.mydues.constants.VAL_PACKAGE

class PreloadDuesDao {

    private val preloadDuesRef = Firebase.firestore
        .collection(COL_PRELOAD_DUES)

    fun getAllPreloadDues(): Task<QuerySnapshot> {
        return preloadDuesRef.get()
    }

    fun getPreloadDueByPackage(pkg: String): Task<QuerySnapshot> {
        return preloadDuesRef.whereEqualTo(VAL_PACKAGE, pkg).get()
    }

}
package es.clcarras.mydues.database

import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import es.clcarras.mydues.constants.COL_USERS
import es.clcarras.mydues.constants.COL_USER_WORKERS
import es.clcarras.mydues.model.Worker

class WorkerDao {

    private val userWorkers = Firebase.firestore
        .collection(COL_USERS)
        .document(Firebase.auth.currentUser!!.email!!)
        .collection(COL_USER_WORKERS)

    fun getMyWorkers() = userWorkers.get()

    fun newWorker(worker: Worker) = userWorkers.add(worker.toMap())

    fun updateWorker(worker: Worker) = userWorkers.document(worker.id!!).update(worker.toMap())

    fun deleteWorkerByUUID(notificationUUID: String) {
        getMyWorkers().addOnSuccessListener { col ->
            for (doc in col)
                if (doc.toObject(Worker::class.java).uuid == notificationUUID)
                    doc.reference.delete()
        }
    }

}
package es.clcarras.mydues.database

import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import es.clcarras.mydues.constants.COL_USERS
import es.clcarras.mydues.constants.COL_USER_WORKERS
import es.clcarras.mydues.model.Worker

/**
 * Clase de acceso a la colección de datos de los workers
 */
class WorkerDao {

    // Referencia a la colección de los workers del usuario
    private val userWorkers = Firebase.firestore
        .collection(COL_USERS)
        .document(Firebase.auth.currentUser!!.email!!)
        .collection(COL_USER_WORKERS)

    /**
     * Función que devuelve un Task para obtener la colección completa
     */
    fun getMyWorkers() = userWorkers.get()

    /**
     * Función para añadir un nuevo worker y que devuelve un Task para saber si se ha agregado o no
     */
    fun newWorker(worker: Worker) = userWorkers.add(worker.toMap())

    /**
     * Función para actualizar un worker y que devuelve un Task para saber si se ha actualizado
     */
    fun updateWorker(worker: Worker) = userWorkers.document(worker.id!!).update(worker.toMap())

    /**
     * Función para eliminar un worker por su UUID
     */
    fun deleteWorkerByUUID(notificationUUID: String) {
        getMyWorkers().addOnSuccessListener { col ->
            for (doc in col)
                if (doc.toObject(Worker::class.java).uuid == notificationUUID)
                    doc.reference.delete()
        }
    }

}
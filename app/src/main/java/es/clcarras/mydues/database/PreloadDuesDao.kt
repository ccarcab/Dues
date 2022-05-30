package es.clcarras.mydues.database

import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import es.clcarras.mydues.constants.COL_PRELOAD_DUES
import es.clcarras.mydues.constants.VAL_PACKAGE

/**
 * Clase de acceso a la colección de cuotas precargadas
 */
class PreloadDuesDao {

    // Referencia a la colección de cuotas
    private val preloadDuesRef = Firebase.firestore
        .collection(COL_PRELOAD_DUES)

    /**
     * Función que devuelve un Task para obtener la colección de cuotas completa
     */
    fun getAllPreloadDues() = preloadDuesRef.get()

    /**
     * Función para obtener una cuota precargada por su nombre de paquete que devuelve un Task
     * para saber si existe o no
     */
    fun getPreloadDueByPackage(pkg: String) = preloadDuesRef.whereEqualTo(VAL_PACKAGE, pkg).get()


}
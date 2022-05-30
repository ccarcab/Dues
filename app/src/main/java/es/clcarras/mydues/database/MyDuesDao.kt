package es.clcarras.mydues.database

import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import es.clcarras.mydues.constants.COL_USERS
import es.clcarras.mydues.constants.COL_USER_DUES
import es.clcarras.mydues.model.MyDues

/**
 * Clase de acceso a la colección de cuotas de usuario
 */
class MyDuesDao {

    // Referencia a la colección del usuario logueado
    private val userDues = Firebase.firestore
        .collection(COL_USERS)
        .document(Firebase.auth.currentUser!!.email!!)
        .collection(COL_USER_DUES)

    /**
     * Función que devuelve un Task para obtener la colección de cuotas de usuario
     */
    fun getMyDues() = userDues.get()

    /**
     * Función para añadir una nueva cuota y que devuelve un Task para saber si se ha agregado o no
     */
    fun newDues(myDues: MyDues) = userDues.add(myDues.toMap())

    /**
     * Función para actualizar una cuota y que devuelve un Task para saber si se ha actualizado
     */
    fun updateDues(myDues: MyDues) = userDues.document(myDues.id!!).update(myDues.toMap())

    /**
     * Función que borra una cuota
     */
    fun deleteDues(myDues: MyDues) {
        userDues.document(myDues.id!!).delete()
    }

}
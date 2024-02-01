package com.utad.mymenu.firebase.storage

import android.net.Uri
import android.util.Log
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await

// Para subir fotos a FireBase
object FirebaseStorageManager {

    // Accedo a la referencia de firebse
    private val reference = FirebaseStorage.getInstance().reference

    // Funcion para guardar la imagen me devuelve un string con la URL para poder cargar la img
    // Ruta de carpeta uri
    suspend fun uploadPhoto(uri: Uri): String? {
        // Le cambio las / por _ , para que no me cree muchas subcarpetas
        val imageName = "$uri".replace("/", "_")

        // Creamos un nodo para guardar el apartado de imagenes (child)
        // Dentro creamos otro nodo con la imagen (child)
        val imageReference = reference.child("meal").child(imageName)

        // Le pasamos la ruta del archivo que queremos subir
        val response = imageReference.putFile(uri).continueWithTask { task ->

            // Comprobamos si se pudo subir el archivo
            if (!task.isSuccessful) {
                Log.e("FireBaseStorageManager", "${task.exception}")
                task.exception?.let { throw it }
            }
            // Se guarda el archivo
            imageReference.downloadUrl
        }.addOnCompleteListener { task ->
            Log.e("FireBaseStorageManager", "${task.result.toString()}")
        }
        response.await()

        // Commprobamos que la tarea ha ido bien para guardarla en la BD
        if (response.isSuccessful) {
            return "${response.result}"
        } else {
            return null
        }
    }


}
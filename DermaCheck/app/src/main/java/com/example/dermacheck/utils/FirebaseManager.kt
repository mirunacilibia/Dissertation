package com.example.dermacheck.utils

import android.content.Context
import android.net.Uri
import com.example.dermacheck.data.models.Lesion
import com.example.dermacheck.data.models.SkinCondition
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.util.*

class FirebaseManager(private val context: Context) {

    private val firestore = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()
    private val auth = FirebaseAuth.getInstance()

    // --- Authentication Helpers ---
    fun isUserLoggedIn(): Boolean = auth.currentUser != null
    fun getUserEmail(): String? = auth.currentUser?.email
    fun logout() = auth.signOut()

    fun sendPasswordResetEmail(
        email: String,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        if (email.isEmpty()) {
            onFailure("Email is required")
            return
        }

        auth.sendPasswordResetEmail(email)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it.message ?: "Unknown error") }
    }

    // --- Profile Fetching ---
    fun getUserProfileWithImage(
        onSuccess: (name: String, email: String, age: Int, gender: String, profileImageUrl: String?) -> Unit,
        onFailure: (String) -> Unit
    ) {
        val uid = auth.currentUser?.uid ?: return onFailure("User not authenticated")
        firestore.collection("users").document(uid).get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    val name = doc.getString("name") ?: "Unknown"
                    val email = doc.getString("email") ?: ""
                    val age = (doc.getLong("age") ?: -1).toInt()
                    val gender = doc.getString("gender") ?: ""
                    val image = doc.getString("profileImage")
                    onSuccess(name, email, age, gender, image)
                } else onFailure("User document not found")
            }
            .addOnFailureListener { onFailure("Failed to load profile: ${it.message}") }
    }

    // --- Count Lesions for Current User ---
    fun countUserLesions(
        onSuccess: (count: Int) -> Unit,
        onFailure: (String) -> Unit
    ) {
        val email = auth.currentUser?.email ?: return onFailure("User email not found")
        firestore.collection("lesions")
            .whereEqualTo("userEmail", email)
            .get()
            .addOnSuccessListener { docs -> onSuccess(docs.size()) }
            .addOnFailureListener { onFailure("Failed to count lesions: ${it.message}") }
    }

    // --- Fetch Lesions for Current User ---
    fun getUserLesions(
        onSuccess: (List<Lesion>) -> Unit,
        onFailure: (String) -> Unit
    ) {
        val email = auth.currentUser?.email
        if (email == null) {
            onFailure("User not authenticated")
            return
        }

        firestore.collection("lesions")
            .whereEqualTo("userEmail", email)
            .get()
            .addOnSuccessListener { snapshot ->
                val lesions = snapshot.documents.mapNotNull { it.toObject(Lesion::class.java) }
                onSuccess(lesions)
            }
            .addOnFailureListener {
                onFailure("Failed to fetch lesions: ${it.message}")
            }
    }

    fun deleteLesion(
        userEmail: String,
        createdAt: Timestamp?,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        if (createdAt == null) {
            onFailure("CreatedAt is null")
            return
        }

        firestore.collection("lesions")
            .whereEqualTo("userEmail", userEmail)
            .whereEqualTo("createdAt", createdAt)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    val docId = documents.documents[0].id
                    firestore.collection("lesions").document(docId)
                        .delete()
                        .addOnSuccessListener { onSuccess() }
                        .addOnFailureListener { onFailure("Failed to delete lesion") }
                } else {
                    onFailure("Lesion not found")
                }
            }
            .addOnFailureListener {
                onFailure("Error finding lesion: ${it.message}")
            }
    }

    fun getAllSkinConditions(
        onSuccess: (List<SkinCondition>) -> Unit,
        onFailure: (String) -> Unit
    ) {
        firestore.collection("skinConditions")
            .orderBy("name") // 🔁 Sort by the 'name' field
            .get()
            .addOnSuccessListener { result ->
                val conditions = result.toObjects(SkinCondition::class.java)
                onSuccess(conditions)
            }
            .addOnFailureListener {
                onFailure("Error fetching skin conditions: ${it.message}")
            }
    }

    fun uploadProfileImage(imageUri: Uri, onResult: (String?) -> Unit) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid == null) {
            onResult(null)
            return
        }

        val storageRef = FirebaseStorage.getInstance()
            .reference
            .child("profile_pics/$uid/profile_image.jpg")

        storageRef.putFile(imageUri)
            .addOnSuccessListener {
                storageRef.downloadUrl
                    .addOnSuccessListener { uri -> onResult(uri.toString()) }
                    .addOnFailureListener { onResult(null) }
            }
            .addOnFailureListener { onResult(null) }
    }

    fun updateUserProfile(updatedFields: Map<String, Any>, onResult: (Boolean) -> Unit) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid == null) {
            onResult(false)
            return
        }

        FirebaseFirestore.getInstance()
            .collection("users")
            .document(uid)
            .update(updatedFields)
            .addOnSuccessListener { onResult(true) }
            .addOnFailureListener { onResult(false) }
    }

    fun getBodyLocations(
        onSuccess: (Map<String, String>, Map<String, String>) -> Unit,
        onFailure: (String) -> Unit
    ) {
        firestore.collection("bodyLocations").get()
            .addOnSuccessListener { snapshot ->
                val bodyLocationMap = mutableMapOf<String, String>()
                val bodyLocationReverseMap = mutableMapOf<String, String>()
                for (doc in snapshot) {
                    val id = doc.id
                    val value = doc.getString("label") ?: id
                    bodyLocationMap[id] = value
                    bodyLocationReverseMap[value] = id
                }
                onSuccess(bodyLocationMap, bodyLocationReverseMap)
            }
            .addOnFailureListener {
                onFailure("Failed to load body locations: ${it.message}")
            }
    }

    // --- Lesion Save ---
    fun saveLesion(
        imageUri: Uri,
        predictedClass: String,
        selectedBodyRegionId: String,
        existingLesionId: String?,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        val timestamp = Timestamp.now()
        val userEmail = auth.currentUser?.email ?: return onFailure("No authenticated user.")
        val safeEmail = userEmail.replace("[^A-Za-z0-9]".toRegex(), "_")
        val lesionImageRef = storage.reference
            .child("lesion_images/$safeEmail/lesion_${System.currentTimeMillis()}.jpg")

        lesionImageRef.putFile(imageUri)
            .addOnSuccessListener {
                lesionImageRef.downloadUrl.addOnSuccessListener { downloadUrl ->
                    val imageUrl = downloadUrl.toString()
                    val historyItem = mapOf(
                        "date" to timestamp,
                        "diagnosis" to predictedClass,
                        "imageUrl" to imageUrl
                    )

                    if (existingLesionId != null) {
                        val docRef = firestore.collection("lesions").document(existingLesionId)
                        docRef.update(
                            "history", FieldValue.arrayUnion(historyItem),
                            "currentDiagnosis", predictedClass,
                            "status", getStatusFromDiagnosis(predictedClass),
                            "lastUpdated", timestamp
                        ).addOnSuccessListener { onSuccess() }
                            .addOnFailureListener { onFailure("Failed to update lesion.") }
                    } else {
                        val newDocRef = firestore.collection("lesions").document()
                        val lesionData = mapOf(
                            "id" to newDocRef.id,
                            "bodyLocationId" to selectedBodyRegionId,
                            "createdAt" to timestamp,
                            "lastUpdated" to timestamp,
                            "currentDiagnosis" to predictedClass,
                            "history" to listOf(historyItem),
                            "status" to getStatusFromDiagnosis(predictedClass),
                            "userEmail" to userEmail
                        )
                        newDocRef.set(lesionData)
                            .addOnSuccessListener { onSuccess() }
                            .addOnFailureListener { onFailure("Error saving lesion.") }
                    }
                }
            }
            .addOnFailureListener { e ->
                onFailure("Image upload failed: ${e.message}")
            }
    }

    private fun getStatusFromDiagnosis(diagnosis: String): String {
        return when (diagnosis.lowercase(Locale.getDefault())) {
            "melanoma", "melanoma metastasis", "squamous cell carcinoma", "basal cell carcinoma" -> "danger"
            "nevus", "scar", "vascular lesion", "other" -> "safe"
            else -> "check"
        }
    }
}

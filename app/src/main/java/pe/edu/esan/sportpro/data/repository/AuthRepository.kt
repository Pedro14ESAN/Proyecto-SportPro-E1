package pe.edu.esan.sportpro.data.repository

import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import pe.edu.esan.sportpro.data.model.User

class AuthRepository {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    // Registrar usuario en Firebase Authentication
    // y crear su perfil en Firestore.
    fun registerUser(
        fullName: String,
        email: String,
        password: String,
        role: String,
        onResult: (Result<User>) -> Unit
    ) {

        if (role == User.ROLE_ADMIN) {
            onResult(
                Result.failure(
                    Exception("El rol Administrador no está disponible para registro público")
                )
            )
            return
        }

        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { authResult ->

                val uid = authResult.user?.uid

                if (uid == null) {
                    onResult(
                        Result.failure(
                            Exception("No se pudo obtener el UID del usuario")
                        )
                    )
                    return@addOnSuccessListener
                }

                val currentDate = Timestamp.now()

                val user = User(
                    uid = uid,
                    fullName = fullName,
                    email = email,
                    role = role,
                    academyId = "",
                    photoUrl = "",
                    active = true,
                    createdAt = currentDate,
                    updatedAt = currentDate
                )

                firestore
                    .collection("users")
                    .document(uid)
                    .set(user)
                    .addOnSuccessListener {
                        onResult(Result.success(user))
                    }
                    .addOnFailureListener { exception ->

                        // Si Firestore falla, eliminamos la cuenta recién creada
                        // para no dejar un usuario incompleto.
                        auth.currentUser?.delete()
                            ?.addOnCompleteListener {
                                auth.signOut()
                                onResult(Result.failure(exception))
                            }
                            ?: run {
                                auth.signOut()
                                onResult(Result.failure(exception))
                            }
                    }
            }
            .addOnFailureListener { exception ->
                onResult(Result.failure(exception))
            }
    }

    // Iniciar sesión.
    fun loginUser(
        email: String,
        password: String,
        onResult: (Result<FirebaseUser>) -> Unit
    ) {

        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener { authResult ->

                val firebaseUser = authResult.user

                if (firebaseUser != null) {
                    onResult(Result.success(firebaseUser))
                } else {
                    onResult(
                        Result.failure(
                            Exception("No se pudo iniciar sesión")
                        )
                    )
                }
            }
            .addOnFailureListener { exception ->
                onResult(Result.failure(exception))
            }
    }

    // Obtener información del usuario desde Firestore.
    fun getUserProfile(
        uid: String,
        onResult: (Result<User>) -> Unit
    ) {

        firestore
            .collection("users")
            .document(uid)
            .get()
            .addOnSuccessListener { document ->

                val user = document.toObject(User::class.java)

                if (user != null) {
                    onResult(Result.success(user))
                } else {
                    onResult(
                        Result.failure(
                            Exception("No se encontró el perfil del usuario")
                        )
                    )
                }
            }
            .addOnFailureListener { exception ->
                onResult(Result.failure(exception))
            }
    }

    // Cerrar sesión.
    fun logout() {
        auth.signOut()
    }

    // Saber si existe una sesión iniciada.
    fun isUserLoggedIn(): Boolean {
        return auth.currentUser != null
    }

    // Obtener UID del usuario conectado.
    fun getCurrentUserId(): String? {
        return auth.currentUser?.uid
    }
}
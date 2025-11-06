package com.chronoplan.di

import com.chronoplan.data.repository.FirestoreChronoRepository
import com.chronoplan.domain.usecase.ChronoUseCase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

object AppContainer {

    private val firestore by lazy { FirebaseFirestore.getInstance() }
    private val storage by lazy { FirebaseStorage.getInstance() }
    private val auth by lazy { FirebaseAuth.getInstance() }

    private val chronoRepo by lazy {
        FirestoreChronoRepository(
            firestore = firestore,
            storage = storage,
            auth = auth
        )
    }

    // ✅ Ganti semua usecase ke satu pintu
    val chronoUseCase by lazy { ChronoUseCase(chronoRepo) }
}

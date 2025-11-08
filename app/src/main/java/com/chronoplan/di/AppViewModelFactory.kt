package com.chronoplan.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.chronoplan.data.repository.FirestoreChronoRepository
import com.chronoplan.domain.usecase.AchievementUseCase
import com.chronoplan.domain.usecase.ChronoUseCase
import com.chronoplan.ui.agenda.AgendaViewModel
import com.chronoplan.ui.akun.AkunViewModel
import com.chronoplan.ui.note.NoteViewModel
import com.chronoplan.ui.user.signin.SignInViewModel
import com.chronoplan.ui.user.signup.SignUpViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.chronoplan.ui.home.HomeViewModel

@Suppress("UNCHECKED_CAST")
class AppViewModelFactory : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val repo = FirestoreChronoRepository(
            firestore = FirebaseFirestore.getInstance(),
            storage = FirebaseStorage.getInstance(),
            auth = FirebaseAuth.getInstance()
        )
        val useCase = ChronoUseCase(repo)
        val achievementUseCase = AchievementUseCase()

        return when {
            modelClass.isAssignableFrom(HomeViewModel::class.java) ->
                HomeViewModel(useCase) as T
            modelClass.isAssignableFrom(AkunViewModel::class.java) ->
                AkunViewModel(useCase, achievementUseCase) as T
            modelClass.isAssignableFrom(AgendaViewModel::class.java) ->
                AgendaViewModel(useCase) as T
            modelClass.isAssignableFrom(NoteViewModel::class.java) ->
                NoteViewModel(useCase) as T
            modelClass.isAssignableFrom(SignInViewModel::class.java) ->
                SignInViewModel(useCase) as T
            modelClass.isAssignableFrom(SignUpViewModel::class.java) ->
                SignUpViewModel(useCase) as T
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
package dev.mijey.popularloanbooks.ui

import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.savedstate.SavedStateRegistryOwner
import dev.mijey.popularloanbooks.data.LibdataRepository

class PopularLoanBooksViewModelFactory(
    owner: SavedStateRegistryOwner,
    private val repository: LibdataRepository
) : AbstractSavedStateViewModelFactory(owner, null) {

    override fun <T : ViewModel?> create(
        key: String,
        modelClass: Class<T>,
        handle: SavedStateHandle
    ): T {
        if (modelClass.isAssignableFrom(PopularLoanBooksViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PopularLoanBooksViewModel(repository, handle) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

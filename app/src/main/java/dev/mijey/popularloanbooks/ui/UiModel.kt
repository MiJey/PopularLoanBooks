package dev.mijey.popularloanbooks.ui

import dev.mijey.popularloanbooks.model.Book

sealed class UiModel {
    data class BookItem(val book: Book) : UiModel()
    data class SeparatorItem(val roundedRank: Int) : UiModel()
}

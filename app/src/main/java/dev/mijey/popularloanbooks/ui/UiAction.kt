package dev.mijey.popularloanbooks.ui

sealed class UiAction {
    data class Request(val pageIndex: Int) : UiAction()
    data class Scroll(val currentPageIndex: Int) : UiAction()
}

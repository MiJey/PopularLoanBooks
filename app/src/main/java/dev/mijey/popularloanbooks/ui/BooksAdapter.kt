package dev.mijey.popularloanbooks.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import dev.mijey.popularloanbooks.R
import dev.mijey.popularloanbooks.databinding.BookViewItemBinding
import dev.mijey.popularloanbooks.databinding.SeparatorViewItemBinding

class BooksAdapter : PagingDataAdapter<UiModel, ViewHolder>(UIMODEL_COMPARATOR) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        if (viewType == R.layout.book_view_item) {
            val binding =
                BookViewItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return BookViewHolder(binding)
        } else {
            val binding =
                SeparatorViewItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return SeparatorViewHolder(binding)
        }

    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is UiModel.BookItem -> R.layout.book_view_item
            is UiModel.SeparatorItem -> R.layout.separator_view_item
            null -> throw UnsupportedOperationException("Unknown view")
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val uiModel = getItem(position)
        uiModel.let {
            when (uiModel) {
                is UiModel.BookItem -> (holder as BookViewHolder).bind(uiModel.book)
                is UiModel.SeparatorItem -> (holder as SeparatorViewHolder).bind(uiModel.roundedRank)
            }
        }
    }

    companion object {
        private val UIMODEL_COMPARATOR = object : DiffUtil.ItemCallback<UiModel>() {
            override fun areItemsTheSame(oldItem: UiModel, newItem: UiModel): Boolean {
                return (oldItem is UiModel.BookItem && newItem is UiModel.BookItem &&
                        oldItem.book.bookName == newItem.book.bookName) ||
                        (oldItem is UiModel.SeparatorItem && newItem is UiModel.SeparatorItem &&
                                oldItem.roundedRank == newItem.roundedRank)
            }

            override fun areContentsTheSame(oldItem: UiModel, newItem: UiModel): Boolean =
                oldItem == newItem
        }
    }
}

package dev.mijey.popularloanbooks.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import dev.mijey.popularloanbooks.databinding.BookViewItemBinding
import dev.mijey.popularloanbooks.model.Book

class BooksAdapter : PagingDataAdapter<Book, BooksAdapter.BookViewHolder>(BOOK_COMPARATOR) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookViewHolder {
        val binding =
            BookViewItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return BookViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BookViewHolder, position: Int) {
        holder.bind(getItem(position) ?: return)
    }

    companion object {
        private val BOOK_COMPARATOR = object : DiffUtil.ItemCallback<Book>() {
            override fun areItemsTheSame(oldItem: Book, newItem: Book): Boolean =
                oldItem.bookName == newItem.bookName

            override fun areContentsTheSame(oldItem: Book, newItem: Book): Boolean =
                oldItem == newItem
        }
    }

    inner class BookViewHolder(private val binding: BookViewItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            binding.item.setOnClickListener {
                // TODO 상세 내용 화면
            }
        }

        fun bind(book: Book) {
            binding.book = book
        }
    }
}

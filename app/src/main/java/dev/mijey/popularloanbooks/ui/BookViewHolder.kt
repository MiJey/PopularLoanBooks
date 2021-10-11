package dev.mijey.popularloanbooks.ui

import androidx.recyclerview.widget.RecyclerView
import dev.mijey.popularloanbooks.databinding.BookViewItemBinding
import dev.mijey.popularloanbooks.model.Book

class BookViewHolder(private val binding: BookViewItemBinding) :
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

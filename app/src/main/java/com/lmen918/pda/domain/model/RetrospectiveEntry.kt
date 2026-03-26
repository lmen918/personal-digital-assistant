package com.lmen918.pda.domain.model

data class RetrospectiveEntry(
    val text: String,
    val category: Category
) {
    enum class Category { POSITIVE, MEDIAN, NEGATIVE }
}

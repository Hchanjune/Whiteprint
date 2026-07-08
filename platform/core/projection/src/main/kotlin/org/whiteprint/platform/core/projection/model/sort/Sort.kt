package org.whiteprint.platform.core.projection.model.sort

data class Sort(val orders: List<SortOrder>) {
    companion object {
        fun by(field: String, direction: SortDirection = SortDirection.ASC) =
            Sort(listOf(SortOrder(field, direction)))
    }
}

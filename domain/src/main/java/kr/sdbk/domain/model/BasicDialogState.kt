package kr.sdbk.domain.model

data class BasicDialogState<T>(
    val isVisible: Boolean = false,
    val data: T? = null
)
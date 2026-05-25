package uz.apprica.plannote.domain.model

data class Note(
    val id: Long = 0,
    val title: String,
    val content: String,
    val color: Int = 0,
    val isPinned: Boolean = false,
    val isArchived: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val reminderAt: Long? = null
) {
    val preview: String
        get() = content.take(120).trimEnd()

    val isEmpty: Boolean
        get() = title.isBlank() && content.isBlank()
}

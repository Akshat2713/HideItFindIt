package ak.project.hideitfindit

data class MediaFolder(
    val folderName: String,
    val mediaType: MediaType,
    val files: List<String> = emptyList() // Store file paths or URIs
)

enum class MediaType {
    IMAGE, VIDEO
}

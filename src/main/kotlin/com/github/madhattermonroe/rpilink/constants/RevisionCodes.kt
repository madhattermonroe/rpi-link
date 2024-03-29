package com.github.madhattermonroe.rpilink.constants

enum class RevisionCodes(
    val releaseQuart: String?,
    val releaseYear: Int?,
    val model: String?,
    val pcbRevision: String?,
    val memoryMb: Int?
) {
    Beta("Q1", 2012, "B (Beta)", null, 256);
}
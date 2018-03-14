package com.barger.sofichallenge

data class SearchResult(val status: Int,
                        val success: Boolean,
                        val data: List<Image>)


data class Image(val id: String,
                 val title: String?,
                 val description: String?,
                 val link: String,
                 val is_album: Boolean?,
                 val images: List<Image>?)


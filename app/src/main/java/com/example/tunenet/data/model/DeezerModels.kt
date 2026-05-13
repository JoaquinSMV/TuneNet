package com.example.tunenet.data.model

import com.google.gson.annotations.SerializedName

data class DeezerSearchResponse(
    @SerializedName("data") val data: List<DeezerTrack>
)

data class DeezerTrack(
    @SerializedName("id") val id: Long,
    @SerializedName("title") val title: String,
    @SerializedName("artist") val artist: DeezerArtist,
    @SerializedName("album") val album: DeezerAlbum,
    @SerializedName("preview") val preview: String,
    @SerializedName("duration") val duration: Int
)

data class DeezerArtist(
    @SerializedName("name") val name: String
)

data class DeezerAlbum(
    @SerializedName("title") val title: String,
    @SerializedName("cover_medium") val coverMedium: String
)

package com.afri.deteksibawang

data class Penyakit(
    val nama: String,
    val deskripsi: String,
    val gambar: Int,
    var isExpanded: Boolean = false
)
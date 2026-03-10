package org.delcom.entities

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class Recipe(
    var id: String = UUID.randomUUID().toString(),
    var userId: String,
    var title: String,
    var description: String,
    var ingredients: String,
    var steps: String,
    var category: String = "makanan", // makanan, minuman, snack, dessert
    var difficulty: String = "mudah",  // mudah, sedang, sulit
    var cookingTime: Int = 0,          // dalam menit
    var servings: Int = 1,
    var cover: String?,

    @Contextual
    val createdAt: Instant = Clock.System.now(),
    @Contextual
    var updatedAt: Instant = Clock.System.now(),
)

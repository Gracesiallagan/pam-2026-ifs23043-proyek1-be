package org.delcom.data

import kotlinx.datetime.Clock
import kotlinx.serialization.Serializable
import org.delcom.entities.Recipe

@Serializable
data class RecipeRequest(
    var userId: String = "",
    var title: String = "",
    var description: String = "",
    var ingredients: String = "",
    var steps: String = "",
    var category: String = "makanan",
    var difficulty: String = "mudah",
    var cookingTime: Int = 0,
    var servings: Int = 1,
    var cover: String? = null,
) {
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "userId" to userId,
            "title" to title,
            "description" to description,
            "ingredients" to ingredients,
            "steps" to steps,
            "category" to category,
            "difficulty" to difficulty,
            "cookingTime" to cookingTime,
            "servings" to servings,
            "cover" to cover,
        )
    }

    fun toEntity(): Recipe {
        return Recipe(
            userId = userId,
            title = title,
            description = description,
            ingredients = ingredients,
            steps = steps,
            category = category,
            difficulty = difficulty,
            cookingTime = cookingTime,
            servings = servings,
            cover = cover,
            updatedAt = Clock.System.now()
        )
    }
}

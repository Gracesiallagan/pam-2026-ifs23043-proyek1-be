package org.delcom.repositories

import org.delcom.entities.Recipe

interface IRecipeRepository {
    suspend fun getAll(
        userId: String,
        search: String,
        category: String? = null,
        difficulty: String? = null,
        page: Int = 1,
        perPage: Int = 10
    ): List<Recipe>

    suspend fun getById(recipeId: String): Recipe?
    suspend fun create(recipe: Recipe): String
    suspend fun update(userId: String, recipeId: String, newRecipe: Recipe): Boolean
    suspend fun delete(userId: String, recipeId: String): Boolean
    suspend fun getStats(userId: String): Map<String, Long>
}

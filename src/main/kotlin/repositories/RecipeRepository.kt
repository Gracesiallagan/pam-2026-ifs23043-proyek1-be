package org.delcom.repositories

import org.delcom.dao.RecipeDAO
import org.delcom.entities.Recipe
import org.delcom.helpers.suspendTransaction
import org.delcom.helpers.recipeDAOToModel
import org.delcom.tables.RecipeTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.like
import java.util.UUID

class RecipeRepository : IRecipeRepository {

    override suspend fun getAll(
        userId: String,
        search: String,
        category: String?,
        difficulty: String?,
        page: Int,
        perPage: Int
    ): List<Recipe> = suspendTransaction {
        val userUuid = UUID.fromString(userId)
        val offset = ((page - 1) * perPage).toLong()

        RecipeDAO.find {
            var op: Op<Boolean> = RecipeTable.userId eq userUuid

            if (search.isNotBlank()) {
                op = op and (RecipeTable.title.lowerCase() like "%${search.lowercase()}%")
            }

            if (!category.isNullOrBlank()) {
                op = op and (RecipeTable.category eq category)
            }

            if (!difficulty.isNullOrBlank()) {
                op = op and (RecipeTable.difficulty eq difficulty)
            }

            op
        }.orderBy(
            if (search.isNotBlank()) RecipeTable.title to SortOrder.ASC
            else RecipeTable.createdAt to SortOrder.DESC
        )
            .limit(perPage)
            .offset(offset)
            .map(::recipeDAOToModel)
    }

    override suspend fun getById(recipeId: String): Recipe? = suspendTransaction {
        RecipeDAO
            .find { RecipeTable.id eq UUID.fromString(recipeId) }
            .limit(1)
            .map(::recipeDAOToModel)
            .firstOrNull()
    }

    override suspend fun create(recipe: Recipe): String = suspendTransaction {
        val dao = RecipeDAO.new {
            userId = UUID.fromString(recipe.userId)
            title = recipe.title
            description = recipe.description
            ingredients = recipe.ingredients
            steps = recipe.steps
            category = recipe.category
            difficulty = recipe.difficulty
            cookingTime = recipe.cookingTime
            servings = recipe.servings
            cover = recipe.cover
            createdAt = recipe.createdAt
            updatedAt = recipe.updatedAt
        }
        dao.id.value.toString()
    }

    override suspend fun update(userId: String, recipeId: String, newRecipe: Recipe): Boolean = suspendTransaction {
        val dao = RecipeDAO
            .find {
                (RecipeTable.id eq UUID.fromString(recipeId)) and
                        (RecipeTable.userId eq UUID.fromString(userId))
            }
            .limit(1)
            .firstOrNull()

        if (dao != null) {
            dao.title = newRecipe.title
            dao.description = newRecipe.description
            dao.ingredients = newRecipe.ingredients
            dao.steps = newRecipe.steps
            dao.category = newRecipe.category
            dao.difficulty = newRecipe.difficulty
            dao.cookingTime = newRecipe.cookingTime
            dao.servings = newRecipe.servings
            dao.cover = newRecipe.cover
            dao.updatedAt = newRecipe.updatedAt
            true
        } else {
            false
        }
    }

    override suspend fun delete(userId: String, recipeId: String): Boolean = suspendTransaction {
        val rowsDeleted = RecipeTable.deleteWhere {
            (RecipeTable.id eq UUID.fromString(recipeId)) and
                    (RecipeTable.userId eq UUID.fromString(userId))
        }
        rowsDeleted >= 1
    }

    override suspend fun getStats(userId: String): Map<String, Long> = suspendTransaction {
        val userUuid = UUID.fromString(userId)

        val total = RecipeDAO.find { RecipeTable.userId eq userUuid }.count()
        val makanan = RecipeDAO.find { (RecipeTable.userId eq userUuid) and (RecipeTable.category eq "makanan") }.count()
        val minuman = RecipeDAO.find { (RecipeTable.userId eq userUuid) and (RecipeTable.category eq "minuman") }.count()
        val snack = RecipeDAO.find { (RecipeTable.userId eq userUuid) and (RecipeTable.category eq "snack") }.count()
        val dessert = RecipeDAO.find { (RecipeTable.userId eq userUuid) and (RecipeTable.category eq "dessert") }.count()

        mapOf(
            "total" to total,
            "makanan" to makanan,
            "minuman" to minuman,
            "snack" to snack,
            "dessert" to dessert,
        )
    }
}

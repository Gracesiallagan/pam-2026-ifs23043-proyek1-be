package org.delcom.helpers

import kotlinx.coroutines.Dispatchers
import org.delcom.dao.RecipeDAO
import org.delcom.dao.RefreshTokenDAO
import org.delcom.dao.UserDAO
import org.delcom.entities.Recipe
import org.delcom.entities.RefreshToken
import org.delcom.entities.User
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

suspend fun <T> suspendTransaction(block: Transaction.() -> T): T =
    newSuspendedTransaction(Dispatchers.IO, statement = block)

fun userDAOToModel(dao: UserDAO) = User(
    dao.id.value.toString(),
    dao.name,
    dao.username,
    dao.password,
    dao.photo,
    dao.bio,
    dao.createdAt,
    dao.updatedAt
)

fun refreshTokenDAOToModel(dao: RefreshTokenDAO) = RefreshToken(
    dao.id.value.toString(),
    dao.userId.toString(),
    dao.refreshToken,
    dao.authToken,
    dao.createdAt,
)

fun recipeDAOToModel(dao: RecipeDAO) = Recipe(
    id = dao.id.value.toString(),
    userId = dao.userId.toString(),
    title = dao.title,
    description = dao.description,
    ingredients = dao.ingredients,
    steps = dao.steps,
    category = dao.category,
    difficulty = dao.difficulty,
    cookingTime = dao.cookingTime,
    servings = dao.servings,
    cover = dao.cover,
    createdAt = dao.createdAt,
    updatedAt = dao.updatedAt
)

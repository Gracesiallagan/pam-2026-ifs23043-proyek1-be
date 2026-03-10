package org.delcom.tables

import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp

object RecipeTable : UUIDTable("recipes") {
    val userId = uuid("user_id")
    val title = varchar("title", 150)
    val description = text("description")
    val ingredients = text("ingredients")
    val steps = text("steps")
    val category = varchar("category", 20).default("makanan") // makanan, minuman, snack, dessert
    val difficulty = varchar("difficulty", 10).default("mudah") // mudah, sedang, sulit
    val cookingTime = integer("cooking_time").default(0)
    val servings = integer("servings").default(1)
    val cover = text("cover").nullable()
    val createdAt = timestamp("created_at")
    val updatedAt = timestamp("updated_at")
}

package org.delcom.dao

import org.delcom.tables.RecipeTable
import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.id.EntityID
import java.util.UUID

class RecipeDAO(id: EntityID<UUID>) : Entity<UUID>(id) {
    companion object : EntityClass<UUID, RecipeDAO>(RecipeTable)

    var userId by RecipeTable.userId
    var title by RecipeTable.title
    var description by RecipeTable.description
    var ingredients by RecipeTable.ingredients
    var steps by RecipeTable.steps
    var category by RecipeTable.category
    var difficulty by RecipeTable.difficulty
    var cookingTime by RecipeTable.cookingTime
    var servings by RecipeTable.servings
    var cover by RecipeTable.cover
    var createdAt by RecipeTable.createdAt
    var updatedAt by RecipeTable.updatedAt
}

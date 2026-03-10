package org.delcom.services

import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.util.cio.*
import io.ktor.utils.io.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.delcom.data.AppException
import org.delcom.data.DataResponse
import org.delcom.data.RecipeRequest
import org.delcom.helpers.ServiceHelper
import org.delcom.helpers.ValidatorHelper
import org.delcom.repositories.IRecipeRepository
import org.delcom.repositories.IUserRepository
import java.io.File
import java.util.UUID

class RecipeService(
    private val userRepo: IUserRepository,
    private val recipeRepo: IRecipeRepository
) {

    // Mengambil semua resep milik user yang login
    suspend fun getAll(call: ApplicationCall) {
        val user = ServiceHelper.getAuthUser(call, userRepo)

        val search = call.request.queryParameters["search"] ?: ""
        val category = call.request.queryParameters["category"]
        val difficulty = call.request.queryParameters["difficulty"]
        val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 1
        val perPage = call.request.queryParameters["perPage"]?.toIntOrNull() ?: 10

        val recipes = recipeRepo.getAll(user.id, search, category, difficulty, page, perPage)

        val response = DataResponse(
            "success",
            "Berhasil mengambil daftar resep",
            mapOf(Pair("recipes", recipes))
        )
        call.respond(response)
    }

    // Mengambil detail resep berdasarkan ID
    suspend fun getById(call: ApplicationCall) {
        val recipeId = call.parameters["id"]
            ?: throw AppException(400, "ID resep tidak valid!")

        val user = ServiceHelper.getAuthUser(call, userRepo)

        val recipe = recipeRepo.getById(recipeId)
        if (recipe == null || recipe.userId != user.id) {
            throw AppException(404, "Data resep tidak ditemukan!")
        }

        val response = DataResponse(
            "success",
            "Berhasil mengambil data resep",
            mapOf(Pair("recipe", recipe))
        )
        call.respond(response)
    }

    // Menambahkan resep baru
    suspend fun post(call: ApplicationCall) {
        val user = ServiceHelper.getAuthUser(call, userRepo)

        val request = call.receive<RecipeRequest>()
        request.userId = user.id

        val validator = ValidatorHelper(request.toMap())
        validator.required("title", "Judul resep tidak boleh kosong")
        validator.required("description", "Deskripsi tidak boleh kosong")
        validator.required("ingredients", "Bahan-bahan tidak boleh kosong")
        validator.required("steps", "Langkah memasak tidak boleh kosong")
        validator.validate()

        val recipeId = recipeRepo.create(request.toEntity())

        val response = DataResponse(
            "success",
            "Berhasil menambahkan resep baru",
            mapOf(Pair("recipeId", recipeId))
        )
        call.respond(response)
    }

    // Mengubah data resep
    suspend fun put(call: ApplicationCall) {
        val recipeId = call.parameters["id"]
            ?: throw AppException(400, "ID resep tidak valid!")

        val user = ServiceHelper.getAuthUser(call, userRepo)

        val request = call.receive<RecipeRequest>()
        request.userId = user.id

        val validator = ValidatorHelper(request.toMap())
        validator.required("title", "Judul resep tidak boleh kosong")
        validator.required("description", "Deskripsi tidak boleh kosong")
        validator.required("ingredients", "Bahan-bahan tidak boleh kosong")
        validator.required("steps", "Langkah memasak tidak boleh kosong")
        validator.validate()

        val oldRecipe = recipeRepo.getById(recipeId)
        if (oldRecipe == null || oldRecipe.userId != user.id) {
            throw AppException(404, "Data resep tidak ditemukan!")
        }
        request.cover = oldRecipe.cover

        val isUpdated = recipeRepo.update(user.id, recipeId, request.toEntity())
        if (!isUpdated) {
            throw AppException(400, "Gagal memperbarui data resep!")
        }

        val response = DataResponse(
            "success",
            "Berhasil mengubah data resep",
            null
        )
        call.respond(response)
    }

    // Menghapus resep
    suspend fun delete(call: ApplicationCall) {
        val recipeId = call.parameters["id"]
            ?: throw AppException(400, "ID resep tidak valid!")

        val user = ServiceHelper.getAuthUser(call, userRepo)

        val oldRecipe = recipeRepo.getById(recipeId)
        if (oldRecipe == null || oldRecipe.userId != user.id) {
            throw AppException(404, "Data resep tidak ditemukan!")
        }

        val isDeleted = recipeRepo.delete(user.id, recipeId)
        if (!isDeleted) {
            throw AppException(400, "Gagal menghapus resep!")
        }

        if (oldRecipe.cover != null) {
            val oldFile = File(oldRecipe.cover!!)
            if (oldFile.exists()) oldFile.delete()
        }

        val response = DataResponse(
            "success",
            "Berhasil menghapus resep",
            null
        )
        call.respond(response)
    }

    // Upload cover resep
    suspend fun putCover(call: ApplicationCall) {
        val recipeId = call.parameters["id"]
            ?: throw AppException(400, "ID resep tidak valid!")

        val user = ServiceHelper.getAuthUser(call, userRepo)

        val request = RecipeRequest()
        request.userId = user.id

        val multipartData = call.receiveMultipart(formFieldLimit = 1024 * 1024 * 5)
        multipartData.forEachPart { part ->
            when (part) {
                is PartData.FileItem -> {
                    val ext = part.originalFileName
                        ?.substringAfterLast('.', "")
                        ?.let { if (it.isNotEmpty()) ".$it" else "" }
                        ?: ""

                    val fileName = UUID.randomUUID().toString() + ext
                    val filePath = "uploads/recipes/$fileName"

                    withContext(Dispatchers.IO) {
                        val file = File(filePath)
                        file.parentFile.mkdirs()
                        part.provider().copyAndClose(file.writeChannel())
                        request.cover = filePath
                    }
                }
                else -> {}
            }
            part.dispose()
        }

        if (request.cover == null) {
            throw AppException(400, "Cover resep tidak tersedia!")
        }

        val newFile = File(request.cover!!)
        if (!newFile.exists()) {
            throw AppException(400, "Cover resep gagal diunggah!")
        }

        val oldRecipe = recipeRepo.getById(recipeId)
        if (oldRecipe == null || oldRecipe.userId != user.id) {
            throw AppException(404, "Data resep tidak ditemukan!")
        }

        request.title = oldRecipe.title
        request.description = oldRecipe.description
        request.ingredients = oldRecipe.ingredients
        request.steps = oldRecipe.steps
        request.category = oldRecipe.category
        request.difficulty = oldRecipe.difficulty
        request.cookingTime = oldRecipe.cookingTime
        request.servings = oldRecipe.servings

        val isUpdated = recipeRepo.update(user.id, recipeId, request.toEntity())
        if (!isUpdated) {
            throw AppException(400, "Gagal memperbarui cover resep!")
        }

        if (oldRecipe.cover != null) {
            val oldFile = File(oldRecipe.cover!!)
            if (oldFile.exists()) oldFile.delete()
        }

        val response = DataResponse(
            "success",
            "Berhasil mengubah cover resep",
            null
        )
        call.respond(response)
    }

    // Mengambil gambar cover resep
    suspend fun getCover(call: ApplicationCall) {
        val recipeId = call.parameters["id"]
            ?: throw AppException(400, "ID resep tidak valid!")

        val recipe = recipeRepo.getById(recipeId)
            ?: return call.respond(HttpStatusCode.NotFound)

        if (recipe.cover == null) {
            throw AppException(404, "Resep belum memiliki cover")
        }

        val file = File(recipe.cover!!)
        if (!file.exists()) {
            throw AppException(404, "Cover resep tidak tersedia")
        }

        call.respondFile(file)
    }

    // Mengambil statistik resep
    suspend fun getStats(call: ApplicationCall) {
        val user = ServiceHelper.getAuthUser(call, userRepo)
        val stats = recipeRepo.getStats(user.id)

        val response = DataResponse(
            "success",
            "Berhasil mengambil statistik resep",
            stats
        )
        call.respond(response)
    }
}

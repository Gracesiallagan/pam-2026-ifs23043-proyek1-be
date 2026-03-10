package org.delcom.module

import org.delcom.repositories.*
import org.delcom.services.AuthService
import org.delcom.services.RecipeService
import org.delcom.services.UserService
import org.koin.dsl.module

fun appModule(jwtSecret: String) = module {
    // User Repository
    single<IUserRepository> {
        UserRepository()
    }

    // User Service
    single {
        UserService(get(), get())
    }

    // Refresh Token Repository
    single<IRefreshTokenRepository> {
        RefreshTokenRepository()
    }

    // Auth Service
    single {
        AuthService(jwtSecret, get(), get())
    }

    // Recipe Repository
    single<IRecipeRepository> {
        RecipeRepository()
    }

    // Recipe Service
    single {
        RecipeService(get(), get())
    }
}

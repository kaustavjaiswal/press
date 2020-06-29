package me.saket.press.shared.sync

import io.ktor.client.HttpClient
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.features.json.serializer.KotlinxSerializer
import io.ktor.client.features.logging.LogLevel
import io.ktor.client.features.logging.Logger
import io.ktor.client.features.logging.Logging
import io.ktor.client.features.logging.SIMPLE
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration.Companion.Stable
import me.saket.kgit.RealGit
import me.saket.press.shared.di.koin
import me.saket.press.shared.sync.git.DeviceInfo
import me.saket.press.shared.sync.git.GitHostAuthPresenter
import me.saket.press.shared.sync.git.GitHostService
import me.saket.press.shared.sync.git.GitHubService
import me.saket.press.shared.sync.git.GitSyncer
import org.koin.dsl.module

class SharedSyncComponent {

  val module = module {
    single { httpClient() }
    factory { GitHostAuthPresenter(get(), get(), get()) }
    factory<GitHostService> { GitHubService(get()) }

    factory { RealGit().repository(get<DeviceInfo>().appStorage.path) }
    factory<Syncer> { GitSyncer(get(), get(), get(), get()) }
  }

  private fun httpClient(): HttpClient {
    return HttpClient {
      install(JsonFeature) {
        serializer = KotlinxSerializer(Json(Stable.copy(
            prettyPrint = true,
            ignoreUnknownKeys = true
        )))
      }
      install(Logging) {
        logger = Logger.SIMPLE
        level = LogLevel.ALL
      }
    }
  }

  companion object {
    fun gitHostAuthPresenter(): GitHostAuthPresenter = koin()
  }
}

# My News — Android App

A Kotlin/Compose Android application that displays top news headlines from [NewsAPI](https://newsapi.org), adapting results to the device's locale with an automatic fallback cascade.

---

## Screenshots

| Main Screen | Detail Screen |
|:-----------:|:-------------:|
| ![Main Screen](Screenshot_main.png) | ![Detail Screen](Screenshot_detail.png) |

---

## Screens

### MainScreen — Headlines List

Displays a scrollable list of news cards. Each card shows:

- **Image** (`urlToImage`) — full-width hero thumbnail
- **Title** (`title`) — up to 3 lines, ellipsized if longer

Tapping a card navigates to the DetailScreen. Pull down on the list to refresh headlines.

### DetailScreen — Article Detail

Full article view. Displays:

- **Title** — above the image, bold serif font
- **Image** — full-width hero image (240 dp height)
- **Source** + **Author** — pill tag + label row below the image
- **Body text** — `content` field preferred over `description`; the API truncation marker `[+N chars]` is automatically removed; texts over 2000 characters have a collapsible "Read more / Show less" toggle
- **"Read the full story"** link — opens the original article URL in the device browser

---

## UI Design Decisions

**Why the list shows only image + title**

The news list is designed for fast scanning. Showing description, author, and source in every list item increases visual noise and cognitive load. The single-focus layout (image → title) lets users quickly identify articles of interest before opening the detail view, where the full content is available.

| Screen | Content shown | Rationale |
|--------|--------------|-----------|
| MainScreen card | Image, Title | Fast scanning; minimal cognitive load |
| DetailScreen | Title, Image, Source, Author, Body, URL | Complete article context once the user has chosen to read |

---

## Features

- **Top Headlines** — loads the `top-headlines` endpoint using device locale (language + country).
- **Locale-aware fallback** — 3-step cascade ensures articles are shown even when the device language returns no results.
- **Detail screen** — hero image, cleaned body text, and a clickable link to the full article.
- **Error handling** — network errors and truly empty states show a localised message with a Retry button.
- **Swipe-to-refresh** — pull down on the headlines list to reload; the Material3 indicator appears at the top while existing content stays visible underneath.
- **Secure API key** — injected by `ApiKeyInterceptor` at the OkHttp layer; never appears in service interfaces.
- **Debug logging** — OkHttp `HttpLoggingInterceptor` (BODY level) + Timber tree active only in debug builds via `BuildConfig.ENABLE_LOGGING`.

---

## Architecture

**Clean Architecture + MVVM**

```
data/         — DTOs, Retrofit API, mappers, repository implementation, API config
domain/       — Pure Kotlin models, repository interface, use cases
presentation/ — ViewModels, UiState, UiEvent
ui/           — Compose screens, components, navigation, theme, UiText abstraction
di/           — Hilt modules (NetworkModule, RepositoryModule)
util/         — Language detection, extensions
```

**Layer responsibilities**

| Layer | Responsibility |
|---|---|
| `ui` | Renders state; dispatches events to ViewModel |
| `presentation` | Manages UI state via StateFlow; delegates to use cases |
| `domain` | Business logic use cases; pure Kotlin, no Android deps |
| `data` | Network calls, DTO mapping; implements domain repository |

---

## NewsAPI — `top-headlines` Endpoint

The app calls the following endpoint:

```
GET https://newsapi.org/v2/top-headlines
    ?country={country}
    &language={language}   ← omitted when using country-only fallback
    &pageSize=20
```

> **API Key:** injected automatically as a query parameter by `ApiKeyInterceptor` (OkHttp interceptor layer). It never appears in the Retrofit service interface.

> **Note:** NewsAPI does not allow combining `sources` with `country` or `language`.
> The app uses `country` + optional `language` for locale personalisation.

---

## Supported Languages by NewsAPI

The `language` parameter of `top-headlines` accepts the following codes:

```
ar  de  en  es  fr  he  it  nl  no  pt  ru  sv  ud  zh
```

Any device language outside this list is automatically mapped to `"en"` (fallback defined in `NewsDefaults`, applied by `LanguageProvider`).

---

## Languages That Can Return an Empty List

Even for a **supported** language code, NewsAPI may return:

```json
{ "status": "ok", "totalResults": 0, "articles": [] }
```

This is **not** an API error — it is a valid 200 response meaning no articles are currently indexed for that combination of `language` + `country`.

Observed combinations that can return empty results (non-exhaustive):

| language | country | Notes |
|----------|---------|-------|
| `fr` | `fr` | Returns 0 results intermittently |
| `he` | `il` | Low article volume |
| `ud` | — | Rarely populated |
| `zh` | `cn` | Restricted access on free tier |

The app handles this transparently via an automatic fallback cascade (see below).

---

## Locale Fallback Cascade

When the initial call returns an empty article list, `GetTopHeadlinesUseCase` retries automatically — **at most 3 network calls per refresh**:

```
Attempt 1  country=<deviceCountry>  language=<deviceLanguage>
               ↓  articles == empty?
Attempt 2  country=<deviceCountry>  (language param omitted)
               ↓  articles == empty?
Attempt 3  country=us               language=en   ← NewsDefaults.FALLBACK_COUNTRY / FALLBACK_LANGUAGE
               ↓  return result (empty or not — cascade ends here)
```

The cascade stops as soon as a non-empty result is returned. If all 3 attempts return empty, the UI shows **"No articles available right now."** (localised via `strings.xml`) with a Retry button that restarts the cascade from attempt 1.

**Implementation location:** `domain/usecase/GetTopHeadlinesUseCase.kt`

> The use case is the correct layer for this business logic: it decides *which parameters to try*, while the repository remains a simple API gateway and the ViewModel knows nothing about locale.

---

## Language / Country Detection

`util/LanguageProvider.kt` reads the device locale and validates it against the NewsAPI-supported sets. Fallback values come from `domain/NewsDefaults`:

```kotlin
fun getLanguage(): String {
    val lang = Locale.getDefault().language.lowercase()
    return if (lang in supportedLanguages) lang else NewsDefaults.FALLBACK_LANGUAGE
}

fun getCountry(): String {
    val country = Locale.getDefault().country.lowercase()
    return if (country in supportedCountries) country else NewsDefaults.FALLBACK_COUNTRY
}
```

`LanguageProvider` is injected into `GetTopHeadlinesUseCase` (not the ViewModel), keeping locale logic entirely in the domain layer.

---

## Serialisation

The app uses **kotlinx-serialization** for all JSON parsing:

- **Network DTOs** (`ArticleDto`, `NewsResponseDto`, `SourceDto`) — annotated `@Serializable`; field names match JSON keys so no `@SerialName` is needed.
- **Retrofit converter** — `converter-kotlinx-serialization` (Retrofit 3.x built-in); configured with `ignoreUnknownKeys = true` and `coerceInputValues = true`.
- **`Json` instance** — provided as a `@Singleton` by `NetworkModule` and used exclusively for network response parsing.

---

## Navigation Strategy

`AppNavGraph` defines two destinations:

- `NavRoutes.MAIN` → `MainScreen`
- `NavRoutes.DETAIL` → `DetailScreen`, receives the **article URL** as a URL-encoded navigation argument (`NavRoutes.DETAIL_ARG`)

**How it works:**

1. When the user taps an article, `MainScreen` calls `onNavigateToDetail(article.url)`.
2. `AppNavGraph` navigates to `detail/{articleUrl}` with the URL-encoded article URL.
3. `DetailViewModel` reads the URL from `SavedStateHandle` (preserved across configuration changes and restored from the navigation back-stack after process death).
4. `DetailViewModel` looks up the article in `ArticlesCache` — the in-memory cache populated by `MainViewModel` when headlines are loaded.
5. If the article is found → `DetailUiState.Loaded`; if not (cache empty after process death) → `DetailUiState.Unavailable` → automatic back-navigation.

**Why this approach:**

- The article URL is a stable, string-safe navigation argument — no serialisation of complex objects.
- `SavedStateHandle` preserves the URL across process death (the back-stack is restored).
- `ArticlesCache` is a lightweight in-memory singleton that decouples `MainViewModel` and `DetailViewModel` without requiring a local database.
- If a persistent solution is needed in the future (deep links, offline), `DetailViewModel` only needs to swap `ArticlesCache.findByUrl()` for a Room query.

> **Known limitation:** on process death, if the OS restores the back-stack but the cache is empty (fresh process), the detail screen navigates back automatically. This is an acceptable trade-off for the current scope — adding a local DB would resolve it.

---

## API Key Security

The API key is **never hardcoded** in any service interface or network call:

1. `local.properties` holds `API_KEY=...` (not committed to version control).
2. `build.gradle.kts` exposes it as `BuildConfig.NEWS_API_KEY`.
3. `ApiKeyInterceptor` (Hilt `@Singleton`) appends it as a query parameter to every outgoing OkHttp request.

```kotlin
class ApiKeyInterceptor @Inject constructor() : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val url = chain.request().url.newBuilder()
            .addQueryParameter("apiKey", BuildConfig.NEWS_API_KEY)
            .build()
        return chain.proceed(chain.request().newBuilder().url(url).build())
    }
}
```

---

## Localised Strings & UiText

All user-visible strings are declared in `res/values/strings.xml`. No string literal is hardcoded in a Composable or UI state class.

Error messages from `Throwable.toUserMessage()` return a `UiText` sealed class:

```kotlin
sealed class UiText {
    data class DynamicString(val value: String) : UiText()       // e.g. server error text
    data class StringResource(@StringRes val resId: Int) : UiText() // localised resource
}
```

`UiText.asString()` is a `@Composable` extension that resolves the correct string at render time. A `asString(context: Context)` overload is available for non-Composable contexts.

---

## UI Components

### Reusable components (`ui/components/`)

| Component | Purpose |
|---|---|
| `ScreenSurface` | Full-size `Surface` + `Column` + `statusBarsPadding`; used by `MainScreen` and `DetailContent` |
| `ArticleImage` | `AsyncImage` (Coil) with crossfade |
| `CategoryPillTag` | Small rounded pill displaying the article source name |
| `LoadingView` | Centred `CircularProgressIndicator` with accessibility `contentDescription` |
| `ErrorView` | Centred message + Retry button; accepts `UiText` |

### Constants (`ui/theme/Dimens.kt`)

`Dimens` centralises all recurring UI dimensions (`ScreenPaddingHorizontal`, `ContentSpacing`, `ImageHeight`, etc.) to ensure visual consistency without magic numbers scattered across composables.

---

## Library Choices

| Library | Version | Why chosen |
|---|---|---|
| **Hilt** | 2.59.2 | Android-standard DI; compile-time safety; excellent IDE integration |
| **kotlinx-serialization** | 1.8.1 | Kotlin-native serialisation; `@Serializable` codegen; no reflection at runtime; integrates natively with Retrofit 3.x |
| **Retrofit** | 3.0.0 | Standard REST client; built-in `converter-kotlinx-serialization` |
| **OkHttp** | 5.3.2 | HTTP logging interceptor (debug-only); `ApiKeyInterceptor` for secure key injection |
| **Coil** | 2.7.0 | Compose-native image loading; suspend-based; lighter than Glide on Compose |
| **Navigation Compose** | 2.9.7 | Back-stack management; `SavedStateHandle` integration for nav args |
| **Coroutines + Flow** | 1.10.2 | Structured concurrency; reactive UI state with StateFlow |
| **Timber** | 5.0.1 | Structured application logging; `DebugTree` planted conditionally on `ENABLE_LOGGING` |
| **Turbine** | 1.2.1 | Flow testing utility from CashApp |
| **MockK** | 1.14.9 | Kotlin-idiomatic mocking |
| **KSP** | 2.3.5 | Fast code generation for Hilt; compatible with AGP 9.0 built-in Kotlin mode |

---

## Dependency Management

Using **Gradle Version Catalog** (`gradle/libs.versions.toml`):
- All versions centralised in `[versions]`.
- All library declarations in `[libraries]`.
- Plugin declarations in `[plugins]`.
- No raw version strings in `build.gradle.kts` files.

### AGP 9.0 + KSP compatibility

AGP 9.0.1 activates **built-in Kotlin mode**, which registers the `kotlin` extension internally and forbids usage of `kotlin.sourceSets`. KSP ≤ 2.2.x used this API (issue #2729). The solution applied:

- Kotlin **2.3.10**
- KSP **2.3.5** (first stable release fixing AGP 9.0 incompatibility)

---

## Configuration

### NewsAPI Key

The API key and base URL are read from `local.properties` (never committed to version control):

```properties
API_KEY=your_newsapi_key_here
URL=https://newsapi.org/v2/
```

They are exposed to the app via `BuildConfig` fields defined in `app/build.gradle.kts`:

```kotlin
buildConfigField("String", "NEWS_API_KEY",  "\"${localProperties["API_KEY"]}\"")
buildConfigField("String", "NEWS_BASE_URL", "\"${localProperties["URL"]}\"")
```

`ApiKeyInterceptor` reads `BuildConfig.NEWS_API_KEY` and appends it to every request at the OkHttp layer.

---

## Build & Run

### Prerequisites
- Android Studio Hedgehog or newer
- JDK 11+
- An emulator or physical device running Android 8.0+ (API 26)
- A [NewsAPI](https://newsapi.org) key (free tier available)

### Steps

1. Clone the repository.
2. Create `local.properties` at the project root:
   ```properties
   sdk.dir=/path/to/your/android/sdk
   API_KEY=your_newsapi_key
   URL=https://newsapi.org/v2/
   ```
3. Open in Android Studio and wait for Gradle sync.
4. Run on your target device: **Run > Run 'app'**

### Build Commands

```bash
# Assemble debug APK
./gradlew clean assembleDebug

# Run unit tests
./gradlew test

# Install on connected device
./gradlew installDebug
```

---

## Unit Tests

### Running the tests

```bash
./gradlew test
```

### Test classes

| File | Layer | What is tested |
|------|-------|---------------|
| `ArticleDtoMapperTest` | data/mapper | DTO → domain mapping; null fields → empty strings; `[Removed]` filtering; blank URL rejection |
| `NewsRepositoryImplTest` | data/repository | DTO list mapping; `[Removed]` article filtering end-to-end |
| `GetTopHeadlinesUseCaseTest` | domain/usecase | 3-step fallback cascade: locale → country-only → US/EN; all-empty path |
| `MainViewModelTest` | presentation | Loading/Success/Error/Empty states; `isRefreshing` lifecycle (set on swipe, cleared on completion/error); cache population; state ordering via Turbine |
| `DetailViewModelTest` | presentation | Article lookup from `ArticlesCache` via URL; `Unavailable` state when cache is empty |
| `LanguageProviderTest` | utils | Language validation + fallback to `"en"`; country validation + fallback to `"us"`; Hebrew/Israel; empty tags |
| `ExtensionsTest` | utils | `IOException` → `UiText.StringResource(R.string.error_no_internet)`; generic exception with message → `UiText.DynamicString`; blank/null message → `UiText.StringResource(R.string.error_unexpected)` |

### Test infrastructure

- **`MainDispatcherRule`** — replaces `Dispatchers.Main` with `StandardTestDispatcher` for coroutine control
- **`TestFixtures.kt`** — `fakeArticle(...)` factory with sensible defaults for concise test setup
- **MockK** for mocking dependencies; **Turbine** for Flow emission assertions

---

## Project Structure

```
app/src/main/java/com/yaropaul/mynews/
├── MyNewsApplication.kt           Timber initialisation (debug only)
├── MainActivity.kt
├── data/
│   ├── remote/
│   │   ├── NewsApiConfig.kt       DEFAULT_PAGE_SIZE
│   │   ├── api/                   NewsApiService (Retrofit, no apiKey param)
│   │   ├── dto/                   JSON DTOs (@Serializable — kotlinx-serialization)
│   │   └── mapper/                DTO → domain model
│   └── repository/                NewsRepositoryImpl
├── domain/
│   ├── NewsDefaults.kt            FALLBACK_COUNTRY, FALLBACK_LANGUAGE
│   ├── model/                     Article (pure Kotlin)
│   ├── repository/                NewsRepository interface
│   └── usecase/                   GetTopHeadlinesUseCase (+ fallback cascade)
├── presentation/
│   ├── main/                      MainViewModel, MainUiState, MainUiEvent
│   └── detail/                    DetailViewModel (SavedStateHandle + ArticlesCache), DetailUiState
├── ui/
│   ├── UiText.kt                  Sealed class for localised UI messages
│   ├── components/                ArticleImage, CategoryPillTag, LoadingView, ErrorView, ScreenSurface
│   ├── navigation/                AppNavGraph, NavRoutes, ArticlesCache
│   ├── screen/
│   │   ├── main/                  MainScreen, NewsCard, NewsListContent
│   │   └── detail/                DetailScreen, DetailContent
│   └── theme/                     Type, Theme, Dimens
├── di/                            NetworkModule, RepositoryModule
│   └── ApiKeyInterceptor.kt       OkHttp interceptor for API key injection
└── util/                          LanguageProvider (uses NewsDefaults), Extensions (returns UiText)
```

---

## Known Limitations

| Area | Notes |
|---|---|
| **Content truncation** | NewsAPI free tier truncates `content` at ~200 chars. The app cleans the `[+N chars]` marker and links to the full article. |
| **Detail on process death** | The article URL is preserved in the nav back-stack via `SavedStateHandle`, but `ArticlesCache` is in-memory only. If the OS kills the app and restores the back-stack, the detail screen navigates back automatically. Adding a local DB (Room) would resolve this. |
| **Pagination** | Only first page (20 articles) loaded. No Paging 3 integration. |
| **HTTP cache** | No OkHttp cache or ETag handling. Each refresh is a full network round-trip. |
| **Offline mode** | Articles are not cached locally. No offline reading. |
| **Rate limiting** | NewsAPI free tier = 100 req/day. No retry/backoff strategy on 429 errors. |
| **Article search** | Only `top-headlines` endpoint; no keyword search. |
| **API key in binary** | The key is not in the source repository, but it is compiled into the APK. A backend proxy would fully address this for production use. |

# MyNews — Technical Reference

## 1. Organisation du code

Le projet suit une structure **Clean Architecture** organisée en 6 packages distincts sous `app/src/main/java/com/yaropaul/mynews/` :

```
data/
  remote/
    NewsApiConfig.kt     constantes réseau (DEFAULT_PAGE_SIZE)
    api/                 NewsApiService          interface Retrofit
    dto/                 ArticleDto, NewsResponseDto, SourceDto   (@Serializable)
    mapper/              ArticleDtoMapper         DTO → domain model
  repository/            NewsRepositoryImpl       implémentation du repository

domain/
  NewsDefaults.kt        constantes métier (FALLBACK_COUNTRY, FALLBACK_LANGUAGE)
  model/                 Article                  modèle pur Kotlin
  repository/            NewsRepository           interface
  usecase/               GetTopHeadlinesUseCase   logique métier + fallback cascade

presentation/
  main/                  MainViewModel, MainUiState, MainUiEvent
  detail/                DetailViewModel, DetailUiState

ui/
  UiText.kt              abstraction pour les messages UI localisables
  components/            ArticleImage, CategoryPillTag, LoadingView, ErrorView, ScreenSurface
  navigation/            AppNavGraph, NavRoutes, ArticlesCache
  screen/
    main/                MainScreen, NewsCard, NewsListContent
    detail/              DetailScreen, DetailContent
  theme/                 Type, Theme, Dimens

di/
  NetworkModule.kt       Json, ApiKeyInterceptor, OkHttpClient, Retrofit, NewsApiService
  ApiKeyInterceptor.kt   OkHttp interceptor pour l'injection de la clé API
  RepositoryModule.kt    binding NewsRepository → NewsRepositoryImpl

util/
  LanguageProvider.kt    détection locale + validation contre les codes NewsAPI
  Extensions.kt          Throwable.toUserMessage() → UiText
```

Chaque couche a une responsabilité unique et les dépendances ne descendent que vers les couches inférieures (`ui` → `presentation` → `domain` ← `data`).

> **Note architecturale :** la couche `domain` n'est pas 100 % pure au sens strict — `GetTopHeadlinesUseCase` dépend de `LanguageProvider` (couche `utils`), qui s'appuie sur `Locale` d'Android. C'est un compromis délibéré pour garder la structure simple à ce stade du projet. Une interface `LocaleProvider` côté domain + impl côté app serait la solution propre si la testabilité isolée de la locale devenait un besoin réel.

---

## 2. Kotlin

### Idiomes utilisés

- **`data class`** pour tous les modèles (`Article`, `ArticleDto`, `MainUiState`)
- **`sealed interface`** pour les états UI : `NewsUiState` (`Loading`, `Success`, `Empty`, `Error`), `DetailUiState` (`Loaded`, `Unavailable`)
- **`sealed class`** pour `UiText` (`DynamicString`, `StringResource`)
- **Opérateur `invoke`** sur le use case pour un appel fluide : `getTopHeadlines()`
- **Extension functions** : `Throwable.toUserMessage(): UiText` dans `Extensions.kt`
- **`takeIf`**, **`isNotBlank()`** pour la gestion null-safe des strings
- **`StateFlow`** avec `MutableStateFlow` privé et exposition en `asStateFlow()`
- **`viewModelScope.launch`** + `Job` pour le lancement et l'annulation de coroutines dans le ViewModel
- **`rememberSaveable`** avec état local dans `DetailContent` (toggle "Read more / Show less")
- **Regex** compilée une seule fois comme constante privée de niveau fichier : `TRUNCATION_REGEX`
- **Constantes nommées** (`CONTENT_EXPAND_THRESHOLD`, `MAX_SOURCE_NAME_LENGTH`, `COLLAPSED_MAX_LINES`) dans `DetailContent` pour remplacer les valeurs magiques

### Coroutines

- `suspend operator fun invoke()` dans le use case pour un chaînage séquentiel lisible
- `CancellationException` explicitement rethrow dans `MainViewModel.loadHeadlines()` pour éviter qu'un refresh rapide ne produise un état d'erreur parasite
- `StandardTestDispatcher` + `MainDispatcherRule` dans les tests pour un contrôle total du scheduling
- Turbine pour l'assertion des émissions `StateFlow` en tests

---

## 3. Technologies et librairies

| Librairie | Version | Rôle |
|-----------|---------|------|
| **Hilt** | 2.59.2 | Injection de dépendances compile-time ; `@HiltViewModel`, `@Singleton`, `@InstallIn(SingletonComponent)` |
| **kotlinx-serialization** | 1.8.1 | Sérialisation JSON Kotlin-native ; `@Serializable` sur DTOs ; aucune réflexion à l'exécution |
| **Retrofit** | 3.0.0 | Client REST ; interface `NewsApiService` annotée `@GET`, `@Query` ; converter `converter-kotlinx-serialization` intégré |
| **OkHttp** | 5.3.2 | Couche HTTP ; `ApiKeyInterceptor` pour l'injection de la clé API ; `HttpLoggingInterceptor` conditionnel |
| **Coil** | 2.7.0 | Chargement d'images Compose-natif (`AsyncImage`) ; suspend-based, cache mémoire |
| **Navigation Compose** | 2.9.7 | Navigation back-stack ; `SavedStateHandle` pour les nav args (URL article) |
| **Coroutines + Flow** | 1.10.2 | Concurrence structurée ; état réactif via `StateFlow` |
| **Timber** | 5.0.1 | Logging applicatif structuré ; `DebugTree` planté conditionnellement sur `BuildConfig.ENABLE_LOGGING` |
| **Turbine** | 1.2.1 | Test des émissions `Flow` / `StateFlow` |
| **MockK** | 1.14.9 | Mocking Kotlin-idiomatique (`coEvery`, `coVerify`) |
| **KSP** | 2.3.5 | Génération de code pour Hilt ; compatible mode built-in Kotlin d'AGP 9.0 |

### Choix notables

- **kotlinx-serialization plutôt que Moshi** : sérialisation 100 % Kotlin sans réflexion à l'exécution ; `@Serializable` codegen natif ; intégration directe avec Retrofit 3.x via `converter-kotlinx-serialization`.
- **Coil plutôt que Glide** : API suspend native, poids léger sur Compose.
- **`LinkAnnotation.Url`** (Compose 1.7+) remplace le `ClickableText` déprécié pour les liens cliquables dans `DetailContent`.

---

## 4. Gestion des dépendances

**Gradle Version Catalog** (`gradle/libs.versions.toml`) :

- Toutes les versions centralisées dans `[versions]`.
- Toutes les déclarations de librairies dans `[libraries]`.
- Plugins dans `[plugins]`.
- Aucune version en dur dans les fichiers `build.gradle.kts`.

### Compatibilité AGP 9.0 + KSP

AGP 9.0.1 active le mode **built-in Kotlin**, qui enregistre l'extension `kotlin` en interne et interdit l'usage de `kotlin.sourceSets`. KSP ≤ 2.2.x utilisait cette API (issue #2729). Solution appliquée :

- Kotlin **2.3.10**
- KSP **2.3.5** (premier release stable corrigeant l'incompatibilité AGP 9.0)

---

## 5. Architecture

### MVVM + Clean Architecture

```
UI (Compose)  ──event──▶  ViewModel  ──suspend──▶  UseCase  ──▶  Repository  ──▶  API
      ◀──state (StateFlow)──              ◀────────────────────────── List<Article>
```

| Couche | Type | Responsabilité |
|--------|------|----------------|
| `ui` | Composable | Rendu de l'état ; dispatch d'événements |
| `presentation` | ViewModel + StateFlow | Gestion de l'état UI ; délégation aux use cases |
| `domain` | UseCase + interfaces | Logique métier ; dépendance Android minimale (via LanguageProvider) |
| `data` | Repository + DTO + Mapper | Appels réseau ; mapping DTO → domain |

---

## 6. Navigation — Stratégie retenue

### Flux de navigation

```
MainScreen
  └── tap article
        └── onNavigateToDetail(article.url)
              └── AppNavGraph → navigate("detail/{articleUrl}")
                    └── DetailScreen
                          └── DetailViewModel
                                ├── lit articleUrl depuis SavedStateHandle
                                └── ArticlesCache.findByUrl(articleUrl)
                                      ├── trouvé → DetailUiState.Loaded(article)
                                      └── absent → DetailUiState.Unavailable → popBackStack()
```

### Détail de la stratégie

`AppNavGraph` définit deux destinations :
- `NavRoutes.MAIN` → `MainScreen`
- `NavRoutes.DETAIL` (`detail/{articleUrl}`) → `DetailScreen`, reçoit l'URL de l'article comme argument String URL-encodé

**Argument de navigation :** l'URL de l'article (`article.url`) est le seul argument passé à la destination détail. Elle est URL-encodée via `Uri.encode()` dans `NavRoutes.detailRoute()` et déclarée comme `NavType.StringType` dans `AppNavGraph`.

**Résolution de l'article :** `DetailViewModel` lit l'URL depuis `SavedStateHandle[NavRoutes.DETAIL_ARG]`, puis cherche l'article correspondant dans `ArticlesCache`. Ce cache est un singleton Hilt peuplé par `MainViewModel` à chaque chargement réussi des headlines.

**Gestion du process death :** l'URL est préservée dans le back-stack de navigation via `SavedStateHandle` (mécanique standard Android). En revanche, `ArticlesCache` est en mémoire uniquement. Si l'OS tue l'app et restaure le back-stack, `DetailViewModel` émet `DetailUiState.Unavailable` et l'écran navigue automatiquement en arrière.

**Avantages par rapport à l'ancienne approche (SelectedArticleHolder) :**
- L'argument de navigation est un String lisible et stable (l'URL) — pas d'objet complexe en mémoire partagée sans lien avec le back-stack
- `MainViewModel` ne gère plus d'état de sélection : il charge et cache, c'est tout
- `DetailViewModel` n'est plus couplé à `MainViewModel` via un singleton partagé
- La voie d'évolution vers Room est claire : remplacer `ArticlesCache.findByUrl()` par une requête DAO

---

## 7. Flux de données

### MainViewModel

**Chargement initial (`isRefresh = false`) :**
```
init → loadHeadlines(isRefresh = false)
         ↓
  emit newsState = Loading, isRefreshing = false   (spinner plein écran)
         ↓
  getTopHeadlines() [UseCase, suspend — jusqu'à 3 appels réseau]
         ↓
  articlesCache.updateArticles(articles)
         ↓
  emit isRefreshing = false, newsState = Success | Empty | Error
```

**Swipe-to-refresh (`isRefresh = true`) :**
```
MainUiEvent.Refresh → loadHeadlines(isRefresh = true)
         ↓
  si newsState ≠ Loading : emit isRefreshing = true  (indicateur swipe, contenu conservé)
  si newsState = Loading  : emit newsState = Loading  (chargement initial en cours → traité comme initial)
         ↓
  getTopHeadlines()
         ↓
  emit isRefreshing = false, newsState = Success | Empty | Error
```

Le `Job` précédent est annulé avant de relancer. `CancellationException` est rethrow explicitement pour ne pas être confondu avec une erreur réseau.

### DetailViewModel

```
init
  ↓
articleUrl = savedStateHandle[DETAIL_ARG]   (toujours présent — argument de navigation obligatoire)
  ↓
articlesCache.findByUrl(articleUrl)
  ├── Article trouvé → emit Loaded(article)
  └── null (cache vide) → emit Unavailable
```

Résolution synchrone — aucun état `Loading` nécessaire.

---

## 8. Gestion des erreurs et messages UI

### Contrat d'erreur

`Throwable.toUserMessage(): UiText` (dans `Extensions.kt`) mappe les exceptions en messages localisables :

| Exception | UiText retourné |
|-----------|----------------|
| `IOException` | `StringResource(R.string.error_no_internet)` |
| `HttpException(401)` | `StringResource(R.string.error_unauthorized)` |
| `HttpException(429)` | `StringResource(R.string.error_rate_limit)` |
| `HttpException(5xx)` | `StringResource(R.string.error_server)` |
| autre `HttpException` | `StringResource(R.string.error_unexpected)` |
| autre exception avec message | `DynamicString(message)` |
| autre exception sans message | `StringResource(R.string.error_unexpected)` |

Tous les messages visibles par l'utilisateur sont localisables (`StringResource`) sauf les messages dynamiques d'origine serveur, encapsulés dans `DynamicString`.

### Zéro chaîne hardcodée

Toutes les chaînes affichées à l'utilisateur sont déclarées dans `res/values/strings.xml` :
- labels d'UI (`app_name`, `cd_back`, `cd_loading`)
- textes d'action (`action_retry`, `detail_read_more`, `detail_show_less`)
- messages d'erreur et états vides (`error_no_internet`, `error_unexpected`, `error_no_articles`, etc.)
- textes de détail (`detail_by_author`, `detail_full_story_label`)

---

## 9. Stratégie de réutilisabilité UI

### ScreenSurface

Composable générique qui encapsule le patron répété `Surface + Column + statusBarsPadding` :

```kotlin
@Composable
fun ScreenSurface(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Surface(modifier = modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(modifier = Modifier.fillMaxSize().statusBarsPadding(), content = content)
    }
}
```

Utilisé par `MainScreen` et `DetailContent`.

### Swipe-to-refresh

`MainScreen` utilise `PullToRefreshBox` (Material3) pour le refresh par geste. Le composant reçoit `isRefreshing` depuis le `MainUiState` et `onRefresh` dispatch `MainUiEvent.Refresh` au ViewModel.

Le `PullToRefreshBox` enveloppe `NewsListContent` et remplace l'ancien `IconButton` de rafraîchissement dans le header. Le header est désormais un titre centré uniquement.

Comportement :
- **Chargement initial** : `isRefreshing = false`, `newsState = Loading` → spinner plein écran `LoadingView`
- **Swipe sur contenu existant** : `isRefreshing = true`, `newsState` inchangé → indicateur swipe en haut, contenu visible
- **Swipe pendant chargement initial** : traité comme chargement initial (pas de double indicateur)

### Insets système

- **MainScreen / NewsListContent** : padding bottom calculé depuis `WindowInsets.navigationBars` dans la `LazyColumn` pour que la liste ne soit pas cachée derrière la barre de navigation.
- **DetailContent** : `navigationBarsPadding()` appliqué sur le `Column` scrollable pour le même effet.

### Dimens.kt

Centralise toutes les dimensions récurrentes (`ScreenPaddingHorizontal`, `ContentSpacing`, `ImageHeight`, `DividerThickness`, etc.) pour garantir la cohérence visuelle et faciliter les ajustements globaux.

### NewsDefaults

Centralise les constantes métier de fallback (`FALLBACK_COUNTRY = "us"`, `FALLBACK_LANGUAGE = "en"`) dans la couche `domain`. `NewsApiConfig` dans `data` contient `DEFAULT_PAGE_SIZE`.

---

## 10. Configuration

Les clés sensibles (`API_KEY`, `URL`) sont lues depuis `local.properties` (non versionné) et exposées via `BuildConfig`. Voir README pour les instructions de setup.

---

## 11. Qualité du code

### Points forts

- **Séparation des responsabilités** : le ViewModel ne connaît pas la locale, le Repository ne connaît pas la logique de fallback, `NewsApiService` ne gère pas la clé API.
- **Injection propre** : `Json`, `ApiKeyInterceptor`, `LanguageProvider`, `ArticlesCache` tous injectés via Hilt — aucune instanciation manuelle dans les ViewModels ou services.
- **Mapper null-safe** : `ArticleDtoMapper.toDomain()` retourne `null` pour les articles invalides (URL vide, titre `[Removed]`), filtrés avec `mapNotNull` dans le repository.
- **Logging conditionnel** : `HttpLoggingInterceptor` + Timber actifs uniquement si `BuildConfig.ENABLE_LOGGING` est `true`.
- **Regex compilée une seule fois** : `TRUNCATION_REGEX` est une propriété de niveau fichier.
- **`LinkAnnotation.Url`** : API moderne Compose pour les liens ; évite la gestion manuelle des `Intent`.
- **Zéro chaîne hardcodée** dans les composables ou états UI.
- **CancellationException rethrow** : le `catch (e: Exception)` dans `loadHeadlines()` rethrow explicitement `CancellationException` pour éviter un état d'erreur parasite lors d'un refresh rapide.

### Points d'attention restants

- `DetailContent` : la logique de nettoyage de texte (`TRUNCATION_REGEX.replace`) pourrait être déléguée au mapper ou au use case pour garder les Composables purement déclaratifs.
- `NewsRepositoryImpl` : pas de gestion fine des codes HTTP d'erreur (4xx/5xx) en dehors des exceptions Retrofit.
- `LanguageProvider` dans `utils` : dépendance de `GetTopHeadlinesUseCase` (domain) vers une classe avec `Locale` Android. Un interface `LocaleProvider` côté domain + impl côté app serait plus propre architecturalement, mais est délibérément ignoré pour garder la structure simple.

---

## 12. Tests unitaires

### Couverture

| Fichier | Couche | Ce qui est testé |
|---------|--------|-----------------|
| `ArticleDtoMapperTest` | data/mapper | DTO → domain ; champs null → empty string ; filtre `[Removed]` ; rejet URL vide |
| `NewsRepositoryImplTest` | data/repository | Mapping liste DTO ; filtrage articles `[Removed]` end-to-end |
| `GetTopHeadlinesUseCaseTest` | domain/usecase | Cascade 3 tentatives : locale → country-only → US/EN ; chemin tout-vide |
| `MainViewModelTest` | presentation | États Loading/Success/Error/Empty ; cycle de vie `isRefreshing` (set au swipe, cleared à la fin/erreur) ; population du cache ; ordonnancement via Turbine |
| `DetailViewModelTest` | presentation | Lookup article depuis `ArticlesCache` via URL ; état `Unavailable` quand le cache est vide (process death) |
| `LanguageProviderTest` | utils | Validation langue + fallback `"en"` ; validation pays + fallback `"us"` ; hébreu/Israël ; tag vide |
| `ExtensionsTest` | utils | `IOException` → `UiText.StringResource(R.string.error_no_internet)` ; exception avec message → `UiText.DynamicString` ; message blank/null → `UiText.StringResource(R.string.error_unexpected)` |

### Infrastructure de test

- **`MainDispatcherRule`** — remplace `Dispatchers.Main` par `StandardTestDispatcher`
- **`TestFixtures.kt`** — factory `fakeArticle(...)` avec des valeurs par défaut
- **MockK** — `coEvery` / `coVerify` pour les fonctions `suspend`
- **Turbine** — assertion séquentielle des émissions `StateFlow`
- **`Locale.forLanguageTag()`** (non déprécié) dans `LanguageProviderTest`

---

## 13. Documentation et commentaires

### KDoc / commentaires présents

- **`GetTopHeadlinesUseCase`** : KDoc bloc décrivant les 3 tentatives, la condition d'arrêt et la limite de 3 appels réseau.
- **`ArticlesCache`** : KDoc décrivant le rôle, le cycle de vie, la limitation process death et la voie d'évolution vers Room.
- **`ArticleDtoMapper`** : commentaire inline expliquant le filtre `[Removed]`.
- **`DetailContent`** : commentaires de section et explication du choix `content` vs `description`.
- **`NewsApiConfig`** / **`NewsDefaults`** : commentaires décrivant le rôle de centralisation.
- **`ScreenSurface`** : KDoc décrivant le composable réutilisable.
- **`UiText`** : KDoc expliquant le rôle de l'abstraction et les deux modes de résolution.
- **`ApiKeyInterceptor`** : commentaire expliquant la stratégie de centralisation de la clé.

---

## 14. Dettes techniques

| Dette | Localisation | Effort estimé |
|-------|-------------|---------------|
| `ArticlesCache` en mémoire uniquement | `ArticlesCache.kt`, `DetailViewModel` | Moyen — remplacer par Room pour la persistance et le support des deep links |
| Nettoyage texte dans Composable | `DetailContent.kt` | Faible — déléguer `TRUNCATION_REGEX.replace` au mapper |
| Pas de gestion 4xx/5xx granulaire | `NewsRepositoryImpl.kt` | Moyen — wrapper `Response<T>` Retrofit |
| `LanguageProvider` non abstrait côté domain | `GetTopHeadlinesUseCase.kt`, `LanguageProvider.kt` | Faible — interface `LocaleProvider` + impl dans `di` |
| Pas de pagination | `NewsApiService` / `NewsRepositoryImpl` | Élevé — intégration Paging 3 |
| Pas de cache HTTP/offline | `NetworkModule` | Moyen — cache OkHttp + Room |
| Clé API dans le binaire | `ApiKeyInterceptor.kt`, `build.gradle.kts` | Élevé — proxy backend pour la production |

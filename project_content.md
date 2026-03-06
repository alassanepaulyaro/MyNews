# MyNews — Technical Review

## 1. Organisation du code

Le projet suit une structure **Clean Architecture** stricte, organisée en 5 packages distincts sous `app/src/main/java/com/yaropaul/mynews/` :

```
data/
  mapper/          ArticleDtoMapper       DTO → domain model
  remote/
    api/           NewsApiService          Retrofit interface
    dto/           ArticleDto, NewsResponseDto, SourceDto
  repository/      NewsRepositoryImpl      implémentation du repository

domain/
  model/           Article                 modèle pur Kotlin
  repository/      NewsRepository          interface
  usecase/         GetTopHeadlinesUseCase  logique métier + fallback cascade

presentation/
  main/            MainViewModel, MainUiState, MainUiEvent
  detail/          DetailViewModel, DetailUiState

ui/
  components/      ArticleImage, CategoryPillTag, LoadingView, ErrorView
  navigation/      AppNavGraph, NavRoutes, ArticleNavType, ArticleNavDto
  screen/
    main/          MainScreen, NewsCard, NewsListContent
    detail/        DetailScreen, DetailContent
  theme/           Color, Type, Theme

di/                NetworkModule, RepositoryModule
util/              LanguageProvider, Extensions
```

Chaque couche a une responsabilité unique et les dépendances ne descendent que vers les couches inférieures (`ui` → `presentation` → `domain` ← `data`). La couche `domain` est un module Kotlin pur, sans dépendance Android.

---

## 2. Kotlin

### Idiomes utilisés

- **`data class`** pour tous les modèles (`Article`, `ArticleDto`, `MainUiState`)
- **`sealed interface`** pour les états UI : `NewsUiState` (`Loading`, `Success`, `Empty`, `Error`)
- **Opérateur `invoke`** sur le use case pour un appel fluide : `getTopHeadlines()`
- **Extension functions** : `Throwable.toUserMessage()` dans `Extensions.kt`
- **`takeIf`**, **`orEmpty()`**, **`isNotBlank()`** pour la gestion null-safe des strings
- **`StateFlow`** avec `MutableStateFlow` privé et exposition en `asStateFlow()`
- **`viewModelScope.launch`** pour le lancement de coroutines dans le ViewModel
- **`rememberSaveable`** avec état local dans `DetailContent` (toggle "Read more")
- **Regex** nommée comme constante privée : `TRUNCATION_REGEX` au niveau fichier

### Coroutines

- `suspend operator fun invoke()` dans le use case pour un chaînage séquentiel lisible
- `StandardTestDispatcher` + `MainDispatcherRule` dans les tests pour un contrôle total du scheduling
- Turbine pour l'assertion des émissions `StateFlow` en tests

---

## 3. Technologies et librairies

| Librairie | Version | Rôle |
|-----------|---------|------|
| **Hilt** | 2.59.2 | Injection de dépendances compile-time ; `@HiltViewModel`, `@Singleton`, `@InstallIn(SingletonComponent)` |
| **Retrofit** | 3.0.0 | Client REST ; interface `NewsApiService` annotée `@GET`, `@Query` |
| **OkHttp** | 5.3.2 | Couche HTTP ; `HttpLoggingInterceptor` conditionnel (`BuildConfig.ENABLE_LOGGING`) |
| **Moshi** | 1.15.2 | Sérialisation JSON ; codegen via `@JsonClass(generateAdapter = true)` + fallback `KotlinJsonAdapterFactory` |
| **Coil** | 2.7.0 | Chargement d'images Compose-natif (`AsyncImage`) ; suspend-based, cache en mémoire |
| **Navigation Compose** | 2.9.7 | Navigation back-stack ; `SavedStateHandle` pour les nav args JSON |
| **Coroutines + Flow** | 1.10.2 | Concurrence structurée ; état réactif via `StateFlow` |
| **Turbine** | 1.2.1 | Test des émissions `Flow` / `StateFlow` |
| **MockK** | 1.14.9 | Mocking Kotlin-idiomatique (`coEvery`, `coVerify`) |
| **KSP** | 2.3.5 | Génération de code pour Hilt et Moshi ; compatible AGP 9.0 built-in Kotlin |

### Choix notables

- **Moshi plutôt que Gson** : meilleure intégration Kotlin (null-safety stricte), performance codegen.
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

AGP 9.0.1 active le mode **built-in Kotlin**, qui enregistre l'extension `kotlin` en interne et interdit l'usage de `kotlin.sourceSets`. KSP ≤ 2.2.x utilisait cette API (issue #2729). La solution appliquée :

- Kotlin **2.3.10** (LTS stable, févr. 2026)
- KSP **2.3.5** (premier release stable corrigeant l'incompatibilité AGP 9.0)

Les clés sensibles (`API_KEY`, `BASE_URL`) sont lues depuis `local.properties` (non versionné) et exposées via `BuildConfig`.

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
| `domain` | UseCase + interfaces | Logique métier pure ; aucune dépendance Android |
| `data` | Repository + DTO + Mapper | Appels réseau ; mapping DTO → domain |

### Navigation

`AppNavGraph` définit deux destinations :
- `NavRoutes.Main` → `MainScreen`
- `NavRoutes.Detail` → `DetailScreen` (reçoit un `Article` sérialisé en JSON via `ArticleNavType`)

Le passage de l'article entre écrans utilise un `ArticleNavDto` sérialisé en JSON par Moshi, récupéré dans `DetailViewModel` via `SavedStateHandle`.

### Flux de données : MainViewModel

```
init → loadHeadlines()
         ↓
  emit Loading
         ↓
  getTopHeadlines() [UseCase, suspend]
         ↓
  emit Success(articles) | Empty | Error(message)
```

L'événement `MainUiEvent.Refresh` rappelle `loadHeadlines()`.

---

## 6. Qualité du code

### Points forts

- **Séparation des responsabilités** stricte : le ViewModel ne connaît pas la locale, le Repository ne connaît pas la logique de fallback.
- **Mapper null-safe** : `ArticleDtoMapper.toDomain()` retourne `null` pour les articles invalides (URL vide, titre `[Removed]`), filtrés avec `mapNotNull` dans le repository.
- **Logging conditionnel** : `HttpLoggingInterceptor` actif uniquement si `BuildConfig.ENABLE_LOGGING` est `true` (debug builds).
- **Regex compilée une seule fois** : `TRUNCATION_REGEX` est une propriété de niveau fichier, pas recréée à chaque recomposition.
- **`LinkAnnotation.Url`** : API moderne Compose pour les liens ; évite la gestion manuelle des `Intent`.

### Points d'amélioration possibles

- `DetailContent` : la logique de nettoyage de texte (`TRUNCATION_REGEX.replace`) pourrait être déléguée au use case ou au mapper.
- `NewsRepositoryImpl` : pas de gestion des codes HTTP d'erreur (4xx/5xx) en dehors des exceptions Retrofit.

---

## 7. Tests unitaires

### Couverture

| Fichier | Couche | Ce qui est testé |
|---------|--------|-----------------|
| `ArticleDtoMapperTest` | data/mapper | DTO → domain ; champs null → empty string ; filtre `[Removed]` ; rejet URL vide |
| `NewsRepositoryImplTest` | data/repository | Mapping liste DTO ; filtrage articles `[Removed]` end-to-end |
| `GetTopHeadlinesUseCaseTest` | domain/usecase | Cascade 3 tentatives : locale → country-only → US/EN ; chemin tout-vide |
| `MainViewModelTest` | presentation | États Loading/Success/Error/Empty ; événement Refresh ; ordonnancement via Turbine |
| `DetailViewModelTest` | presentation | Désérialisation Article depuis `SavedStateHandle` (round-trip JSON) |
| `LanguageProviderTest` | utils | Validation langue + fallback `"en"` ; validation pays + fallback `"us"` ; hébreu/Israël ; tag vide |
| `ExtensionsTest` | utils | `IOException` → message réseau ; exception avec message ; message blank/null → fallback |

### Infrastructure de test

- **`MainDispatcherRule`** — remplace `Dispatchers.Main` par `StandardTestDispatcher` pour le contrôle des coroutines
- **`TestFixtures.kt`** — factory `fakeArticle(...)` avec des valeurs par défaut pour un setup concis
- **MockK** — `coEvery` / `coVerify` pour les fonctions `suspend`
- **Turbine** — assertion séquentielle des émissions `StateFlow`
- **`Locale.forLanguageTag()`** (non déprécié) dans `LanguageProviderTest` au lieu du constructeur `Locale(String, String)`

---

## 8. Documentation et commentaires

### KDoc / commentaires

- **`GetTopHeadlinesUseCase`** : KDoc bloc décrivant les 3 tentatives, la condition d'arrêt et la limite de 3 appels réseau.
- **`ArticleDtoMapper`** : commentaire inline expliquant le filtre `[Removed]`.
- **`DetailContent`** : commentaires de section (`// Top bar`, `// Scrollable content`, `// Hero image`, etc.) et explication du choix `content` vs `description`.
- **`NetworkModule`** : commentaire implicite via la structure des `@Provides` ordonnés logiquement.
- **README.md** : documentation complète (architecture, bibliothèques, tests, limitations, configuration).

### Ce qui manque

- Pas de KDoc sur les Composables (`@param`, `@return`) — acceptable pour du code UI interne.
- Pas de documentation sur `ArticleNavDto` et `ArticleNavType` (logique non triviale de sérialisation de nav arg).

---

## 9. Problèmes identifiés — Cascade de fallback de langue

### Description

`GetTopHeadlinesUseCase` implémente une cascade à 3 niveaux pour pallier les résultats vides de NewsAPI selon la locale :

```
Tentative 1  country=<deviceCountry>  language=<deviceLanguage>
               ↓  articles vides ?
Tentative 2  country=<deviceCountry>  (sans paramètre language)
               ↓  articles vides ?
Tentative 3  country=us               language=en
               ↓  retourne le résultat (vide ou non — fin de cascade)
```

### Justification du placement dans le UseCase

C'est la couche **correcte** pour cette logique :
- Le repository reste un simple gateway API (n'a pas à connaître la stratégie de retry).
- Le ViewModel ne connaît ni la locale ni les paramètres de l'API.
- Le use case decide *quels paramètres essayer* — responsabilité de logique métier pure.

### Limites actuelles

| Limite | Impact |
|--------|--------|
| Maximum 3 appels réseau par refresh | Acceptable ; évite les boucles infinies |
| Fallback final `us/en` non paramétrable | Comportement fixe ; difficile à tester avec des locales exotiques |
| Pas de backoff sur erreur 429 (rate limit) | Sur le tier gratuit (100 req/jour), un retry rapide peut épuiser le quota |
| Cascade non annulable | Si l'utilisateur rafraîchit pendant la tentative 2, les 2 coroutines coexistent brièvement |

### Amélioration suggérée

Annuler la cascade en cours lors d'un nouveau `Refresh` en utilisant `viewModelScope.launch` avec annulation explicite du `Job` précédent (pattern `job?.cancel(); job = launch { ... }`).


### Amélioration proposée

Une amélioration possible concerne le chargement des articles lorsque l’utilisateur parcourt la liste.

Dans l’implémentation actuelle, l’application récupère un nombre limité d’articles (par exemple 20) via l’API. Lorsque l’utilisateur atteint la fin de la liste, aucun autre article n’est chargé même si l’API peut encore fournir d’autres résultats.

L’amélioration consiste à mettre en place un mécanisme de pagination avec chargement progressif des données.

Principe :

Lorsque l’utilisateur fait défiler la liste et atteint la fin des éléments affichés, l’application déclenche automatiquement une nouvelle requête vers l’API afin de récupérer la page suivante d’articles.

Les nouveaux articles sont ensuite ajoutés à la liste existante, ce qui permet à l’utilisateur de continuer à faire défiler le contenu sans interruption.

Cette approche, souvent appelée infinite scrolling ou lazy loading, améliore :
	•	l’expérience utilisateur
	•	les performances de l’application
	•	la gestion de grandes listes de données

Techniquement, cela peut être implémenté en utilisant :
	•	un mécanisme de pagination de l’API (page, pageSize)
	•	un PagingData ou une logique équivalente
	•	une détection de la fin de liste lors du scroll dans la LazyColumn

Ainsi, les articles sont chargés progressivement au fur et à mesure que l’utilisateur navigue dans la liste, jusqu’à ce que toutes les données disponibles via l’API aient été récupérées.
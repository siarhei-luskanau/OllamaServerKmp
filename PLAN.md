# OllamaServerKmp - Implementation Plan

## Research Summary

### What is Ollama?

Ollama is a Go application that runs a local HTTP REST server (default port `11434`) wrapping
`llama.cpp` for LLM inference. Its architecture:

```
ollama binary (Go)
  └── HTTP server (net/http, port 11434)
       └── LLM inference (llama.cpp via CGO)
            └── GPU backends: CUDA / Metal / CPU fallback
```

Key API endpoints:
| Method | Path | Description |
|--------|------|-------------|
| `POST` | `/api/generate` | Streaming text generation |
| `POST` | `/api/chat` | Chat completions (OpenAI-compatible) |
| `GET`  | `/api/tags` | List local models |
| `POST` | `/api/pull` | Download model from registry |
| `POST` | `/api/show` | Model metadata |
| `GET`  | `/api/ps` | Running models |

Responses stream as newline-delimited JSON (NDJSON) with `"done": false` until final token.

---

### Current OllamaServerKmp Project State

| Aspect | Current State |
|--------|--------------|
| Project type | KMP template skeleton |
| Platforms | Android + iOS (arm64, simulator arm64) |
| Kotlin | 2.3.20 |
| Compose Multiplatform | 1.11.0-beta01 |
| AGP | 9.1.0 |
| Android minSdk | 30 (Android 11) |
| DI | Koin 4.2 with annotation processing |
| Navigation | Jetbrains Navigation3 |
| Preferences | DataStore + Okio (working) |
| HTTP client | **None** |
| Process management | **None** |
| Ollama integration | **None** (submodule present, not integrated) |
| Android Service | **None** |
| Ollama submodule | Present at `ollama/` (commit `4589fa2`), Dependabot-managed |
| App ID / name | `io.ollama.server.compose.multiplatform` / "CMP template" |

**Module graph:**
```
androidApp ─┐
iosApp     ─┤
            ├── diApp
            │    ├── coreCommon
            │    └── corePref
            ├── navigation
            └── ui/
                 ├── uiCommon
                 ├── uiMain
                 └── uiSplash
```

---

## Critical Platform Difference: Android vs iOS

> **Verified technical constraints - researched against Ktor docs, Apple Developer Forums, and llama.cpp issues.**

| | Android | iOS |
|--|---------|-----|
| Execute arbitrary binary (subprocess) | ✅ Yes (from filesDir) | ❌ No - `fork()`/`exec()`/`posix_spawn()` blocked at **kernel level** (EPERM), not just policy |
| Run in-process HTTP server | ✅ Yes | ✅ Yes - POSIX socket `bind()`/`listen()` IS allowed; GCDWebServer proves this |
| Ktor CIO server | ✅ Yes (JVM) | ✅ Yes - `ktor-server-iosarm64` artifact exists; CIO engine uses POSIX sockets |
| llama.cpp inference | ✅ CPU only (no Vulkan compute) | ✅ Metal GPU-accelerated - best-in-class on Apple Silicon |
| Loopback server needs special permission | N/A | ✅ No - `127.0.0.1` exempt from Local Network permission prompt |
| App Store allows localhost server | N/A | ✅ Yes - GCDWebServer ships in App Store apps; foreground use is fine |
| **Server works in background** | ✅ Via ForegroundService | ❌ **Hard limit** - app suspends after ~5s in background; TCP server freezes |
| HTTPS on embedded server | ✅ | ❌ - Ktor native does not support TLS; HTTP-only (fine for loopback) |

### Android approach
Launch the pre-compiled `ollama` Go binary as a subprocess inside a `ForegroundService`.
Same as the sunshine0523/OllamaServer reference implementation. IPC via HTTP on `127.0.0.1:11434`.

### iOS approach - Embedded In-Process Server (feasible, foreground-only)
iOS **cannot** execute the Go ollama binary (kernel blocks `exec()`). But iOS **can** run a Ktor
CIO HTTP server in-process while the app is in the foreground:

```
iOS App Process
  └── Ktor CIO Server (port 11434, POSIX sockets)
       └── OllamaApiHandler (Kotlin/Native)
            └── llama.cpp (static XCFramework, Metal GPU)
```

This is **not client-only**. The iOS app IS the server - it just runs inference in the same
process rather than as a subprocess. The same Ollama-compatible HTTP API is exposed on
`http://127.0.0.1:11434`, making the commonMain client code identical for both platforms.

**The one hard constraint on iOS:** when the user backgrounds the app, iOS suspends it after
~5 seconds. The Ktor server stops accepting connections until the app returns to foreground.
This is an architectural reality to communicate to users - not a technical blocker.

---

## Clarifying Questions (to resolve before implementation)

> iOS server mode is **resolved**: iOS will embed an in-process Ktor CIO server with llama.cpp
> (foreground-only). Client-only is not the target - see Phase 3.

1. **Android binary delivery**: The ollama binary is 50–200 MB, exceeding Google Play's 150 MB APK limit.
   Options: (a) side-load / direct APK only, (b) Play Asset Delivery (PAD), (c) download at first launch.

2. **Model download**: Should the app allow pulling models via Ollama API (`POST /api/pull`),
   or are models pre-bundled? Pre-bundling is impractical - download strongly recommended.

3. **Network exposure**: Bind to `127.0.0.1` only (secure) or optionally `0.0.0.0:11434`
   (LAN-visible) as a toggle? LAN mode useful for using the phone as a server for a PC client.

4. **Port conflict**: If 11434 is occupied, auto-find next free port or error with message?

5. **Minimum Android API**: Current minSdk 30. The ollama binary targets API 21+.
   Lower to 26 or 28 for broader reach?

6. **Architecture scope**: Standalone user-facing app, or also a publishable library for
   other KMP apps to embed Ollama?

7. **Android background persistence**: Permanent ForegroundService (server runs even after
   leaving the app), or service stops when app goes to background?

8. **iOS background UX**: When iOS app is backgrounded and server pauses, show a banner/notification
   telling the user to return to the app, or silently pause/resume?

---

## Implementation Plan

### Phase 0 - Project Setup (non-breaking, foundation)

#### 0.1 Rename App Identity
- Change `applicationId` from `template.compose.multiplatform` to `io.ollama.server` (or similar)
- Rename package root from `template.*` to `io.ollama.server.*` across all modules
- Update `app_name` string resource from "CMP template" to "Ollama Server"

#### 0.2 Add HTTP Client Dependency
Add Ktor to `libs.versions.toml` and `convention-plugin-multiplatform`:
```toml
[versions]
ktor = "3.1.3"

[libraries]
ktor-client-core = { module = "io.ktor:ktor-client-core", version.ref = "ktor" }
ktor-client-android = { module = "io.ktor:ktor-client-android", version.ref = "ktor" }
ktor-client-darwin = { module = "io.ktor:ktor-client-darwin", version.ref = "ktor" }
ktor-client-content-negotiation = { module = "io.ktor:ktor-client-content-negotiation", version.ref = "ktor" }
ktor-serialization-kotlinx-json = { module = "io.ktor:ktor-serialization-kotlinx-json", version.ref = "ktor" }
```

#### 0.3 Add New Gradle Modules
Add to `settings.gradle.kts`:
```kotlin
include(":core:coreOllama")       // Ollama API models + client
include(":core:coreServer")       // Server lifecycle management
include(":feature:featureServer") // Server control UI
include(":feature:featureModels") // Model management UI
include(":feature:featureChat")   // Chat/generate UI
```

---

### Phase 1 - Common API Layer (`:core:coreOllama`)

**Location:** `core/coreOllama/`

Define the contracts shared between Android and iOS:

```kotlin
// commonMain
data class OllamaModel(val name: String, val size: Long, val digest: String)
data class GenerateRequest(val model: String, val prompt: String, val stream: Boolean = true)
data class GenerateResponse(val response: String, val done: Boolean)
data class ChatMessage(val role: String, val content: String)
data class ServerStatus(val isRunning: Boolean, val port: Int, val pid: Int?)

interface OllamaServerController {
    val status: StateFlow<ServerStatus>
    suspend fun start(): Result<Unit>
    suspend fun stop(): Result<Unit>
    suspend fun restart(): Result<Unit>
}

interface OllamaApiClient {
    suspend fun listModels(): List<OllamaModel>
    suspend fun pullModel(name: String): Flow<PullProgress>
    suspend fun deleteModel(name: String): Result<Unit>
    fun generate(request: GenerateRequest): Flow<GenerateResponse>
    fun chat(model: String, messages: List<ChatMessage>): Flow<ChatMessage>
    suspend fun isServerReady(): Boolean
}
```

**Ktor client factory (commonMain):**
```kotlin
// Platform-specific HttpClient engine provided via expect/actual
expect fun createHttpClient(): HttpClient

// Common client implementation
class OllamaApiClientImpl(private val baseUrl: String) : OllamaApiClient {
    private val client = createHttpClient()
    // ... Ktor calls to http://127.0.0.1:11434/api/...
}
```

---

### Phase 2 - Android Server Implementation (`:core:coreServer`, androidMain)

#### 2.1 Build the Ollama Binary

Create `scripts/build_ollama_android.sh`:
```bash
#!/bin/bash
# Requires: Android NDK r25+, Go 1.22+
NDK_HOME=${ANDROID_NDK_HOME:-$HOME/Library/Android/sdk/ndk/25.2.9519653}
TOOLCHAIN=$NDK_HOME/toolchains/llvm/prebuilt/$(uname -s | tr '[:upper:]' '[:lower:]')-x86_64

export GOOS=android
export GOARCH=arm64
export CGO_ENABLED=1
export CC=$TOOLCHAIN/bin/aarch64-linux-android30-clang
export CXX=$TOOLCHAIN/bin/aarch64-linux-android30-clang++

cd ollama
go build -ldflags="-s -w" -o ../app/androidApp/src/main/assets/arm64-v8a/ollama ./cmd/ollama
```

Binary goes to `app/androidApp/src/main/assets/arm64-v8a/ollama` (~100 MB).

Add a GitHub Actions workflow `build-ollama-android.yml` that:
1. Checks out with submodules
2. Sets up Go 1.22 + Android NDK
3. Runs the build script
4. Commits the updated binary

#### 2.2 Android ForegroundService

**New file:** `core/coreServer/src/androidMain/kotlin/OllamaForegroundService.kt`
```kotlin
class OllamaForegroundService : Service() {
    private var ollamaProcess: Process? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(NOTIFICATION_ID, buildNotification())
        startOllamaProcess()
        return START_STICKY
    }

    private fun startOllamaProcess() {
        val binary = prepareOllamaBinary()  // copy from assets + chmod
        val modelsDir = getExternalFilesDir(null)!!.resolve("models/ollama")
        modelsDir.mkdirs()

        ollamaProcess = ProcessBuilder(binary.absolutePath, "serve")
            .apply {
                environment()["OLLAMA_HOST"] = "127.0.0.1:11434"
                environment()["OLLAMA_MODELS"] = modelsDir.absolutePath
                environment()["HOME"] = filesDir.absolutePath
                redirectErrorStream(true)
            }
            .start()
        // Forward stdout/stderr to Android Logcat in a coroutine
    }

    override fun onDestroy() {
        ollamaProcess?.destroyForcibly()
        super.onDestroy()
    }
}
```

**AndroidManifest additions:**
```xml
<uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
<uses-permission android:name="android.permission.FOREGROUND_SERVICE_DATA_SYNC" />
<uses-permission android:name="android.permission.INTERNET" />

<service
    android:name=".OllamaForegroundService"
    android:foregroundServiceType="dataSync"
    android:exported="false" />
```

#### 2.3 OllamaServerController (Android)

```kotlin
// androidMain
class AndroidOllamaServerController(private val context: Context) : OllamaServerController {
    private val _status = MutableStateFlow(ServerStatus(false, 11434, null))
    override val status = _status.asStateFlow()

    override suspend fun start(): Result<Unit> {
        context.startForegroundService(Intent(context, OllamaForegroundService::class.java))
        // Poll /api/tags until server responds (max 30s)
        return waitForServerReady()
    }

    override suspend fun stop(): Result<Unit> {
        context.stopService(Intent(context, OllamaForegroundService::class.java))
        return Result.success(Unit)
    }
}
```

---

### Phase 3 - iOS Implementation (`:core:coreServer`, iosMain)

> iOS CAN run a local server - just in-process, not as a subprocess.
> Verified: Ktor publishes `ktor-server-iosarm64`; POSIX sockets work on iOS; llama.cpp has
> Metal-accelerated iOS support. Foreground-only is the hard constraint.

#### 3.1 Add Ktor Server Dependency

Add to `libs.versions.toml`:
```toml
ktor-server-core = { module = "io.ktor:ktor-server-core", version.ref = "ktor" }
ktor-server-cio  = { module = "io.ktor:ktor-server-cio",  version.ref = "ktor" }
ktor-server-content-negotiation = { module = "io.ktor:ktor-server-content-negotiation", version.ref = "ktor" }
```

These are added to `iosMain` only - Android uses the subprocess approach and does not need an
in-process server.

#### 3.2 Build llama.cpp as XCFramework

The built-in llama.cpp HTTP server (`tools/server`) fails to compile for iOS due to an
`httplib.h` Objective-C++ interop issue ([#10371](https://github.com/ggml-org/llama.cpp/issues/10371)).
Only the **inference library** is needed - we provide our own Ktor HTTP server.

Build script `scripts/build_llamacpp_ios.sh`:
```bash
#!/bin/bash
# Produces: build/ios/llama.xcframework
cmake -B build/ios-arm64 -G Xcode \
  -DCMAKE_TOOLCHAIN_FILE=cmake/ios.toolchain.cmake \
  -DPLATFORM=OS64 \
  -DLLAMA_METAL=ON \
  -DLLAMA_BUILD_SERVER=OFF \
  -DLLAMA_BUILD_TESTS=OFF \
  -DCMAKE_BUILD_TYPE=Release

cmake --build build/ios-arm64 --config Release -- -arch arm64

cmake -B build/ios-sim -G Xcode \
  -DCMAKE_TOOLCHAIN_FILE=cmake/ios.toolchain.cmake \
  -DPLATFORM=SIMULATORARM64 \
  -DLLAMA_METAL=ON \
  -DLLAMA_BUILD_SERVER=OFF

cmake --build build/ios-sim --config Release

xcodebuild -create-xcframework \
  -library build/ios-arm64/src/Release-iphoneos/libllama.a \
  -headers ollama/llama.cpp/include \
  -library build/ios-sim/src/Release-iphonesimulator/libllama.a \
  -headers ollama/llama.cpp/include \
  -output build/llama.xcframework
```

XCFramework goes to `app/iosApp/llama.xcframework` and is referenced in the Xcode project.

#### 3.3 Kotlin/Native Cinterop Bindings

Create `core/coreServer/src/iosMain/cinterop/llama.def`:
```
headers = llama.h ggml.h
staticLibraries = libllama.a
libraryPaths = ../../../app/iosApp/llama.xcframework/ios-arm64
compilerOpts = -x objective-c++
```

Add to `core/coreServer/build.gradle.kts`:
```kotlin
kotlin {
    iosArm64 {
        compilations["main"].cinterops {
            val llama by creating {
                defFile(project.file("src/iosMain/cinterop/llama.def"))
            }
        }
    }
}
```

#### 3.4 iOS OllamaServerController

```kotlin
// iosMain
class IosOllamaServerController : OllamaServerController {
    private val _status = MutableStateFlow(ServerStatus(false, 11434, null))
    override val status = _status.asStateFlow()
    private var server: EmbeddedServer<*, *>? = null

    override suspend fun start(): Result<Unit> = runCatching {
        val engine = embeddedServer(CIO, port = 11434, host = "127.0.0.1") {
            install(ContentNegotiation) { json() }
            routing {
                post("/api/generate") { handleGenerate(call) }
                post("/api/chat")     { handleChat(call) }
                get("/api/tags")      { handleTags(call) }
                // ... other endpoints
            }
        }
        server = engine
        engine.start(wait = false)
        _status.value = ServerStatus(isRunning = true, port = 11434, pid = null)
    }

    override suspend fun stop(): Result<Unit> = runCatching {
        server?.stop(gracePeriodMillis = 1000, timeoutMillis = 5000)
        server = null
        _status.value = ServerStatus(isRunning = false, port = 11434, pid = null)
    }

    private suspend fun handleGenerate(call: ApplicationCall) {
        val req = call.receive<GenerateRequest>()
        // Call llama.cpp via cinterop
        val ctx = llama_init_from_model(loadModel(req.model), llama_context_default_params())
        // Stream NDJSON tokens back
        call.respondTextWriter(contentType = ContentType.Application.Json) {
            // token streaming loop via llama_decode / llama_sampling_sample
        }
    }
}
```

> **Background note:** iOS suspends the app ~5 seconds after backgrounding. The Ktor server
> stops accepting connections when suspended. On re-foreground the server resumes immediately
> (no restart needed). Users should be informed the server is "foreground-only" on iOS.

---

### Phase 4 - UI Modules

#### 4.1 `:feature:featureServer` - Server Control Screen

- Shows server status (Running / Stopped / Starting)
- Start/Stop button
- Port display
- Server log tail (last N lines from stdout)
- iOS: shows a text field to enter remote server URL

#### 4.2 `:feature:featureModels` - Model Management Screen

- Lists installed models (name, size, quantization)
- Pull new model by name (with streaming download progress bar)
- Delete model with confirmation dialog
- Shows disk usage

#### 4.3 `:feature:featureChat` - Chat/Generate Screen

- Model selector dropdown
- Chat message list with streaming token display
- Input field + send button
- Clear conversation button
- System prompt configuration

---

### Phase 5 - Navigation & DI Wiring

Update `:navigation` to include new routes:
```kotlin
sealed class AppRoutes {
    data object Splash : AppRoutes()
    data object Server : AppRoutes()   // NEW
    data object Models : AppRoutes()   // NEW
    data object Chat : AppRoutes()     // NEW
    data class Settings(...) : AppRoutes() // NEW
}
```

Update `:diApp` to wire new modules:
```kotlin
@Module
class OllamaModule {
    @Single
    fun provideServerController(ctx: Context): OllamaServerController =
        AndroidOllamaServerController(ctx)  // actual/expect per platform

    @Single
    fun provideApiClient(prefs: PrefService): OllamaApiClient =
        OllamaApiClientImpl(baseUrl = "http://127.0.0.1:11434")
}
```

---

### Phase 6 - Binary Distribution Strategy

**Problem:** The ollama binary (~100 MB) exceeds Google Play's 150 MB AAB limit for base APK.

**Recommended approach: Play Asset Delivery (PAD)**
- Declare the binary as a `fast-follow` install-time asset pack
- Binary is delivered automatically after app install without user interaction
- Use `AssetPackManager` API to check delivery status before starting the server

**Alternative (simpler): Direct APK distribution**
- Host the APK directly (GitHub Releases, F-Droid, etc.)
- No Play Store restrictions

**Alternative: Download at first run**
- App downloads the ollama binary from a CDN/GitHub Release on first launch
- Shows a one-time setup screen with progress bar
- Stores binary in `filesDir` permanently

---

### Phase 7 - Testing Strategy

| Layer | Test type | Framework |
|-------|-----------|-----------|
| `coreOllama` data models | Unit | kotlinx-coroutines-test |
| `OllamaApiClient` | Integration (mock server) | Ktor MockEngine |
| `OllamaServerController` | Unit (mock service) | Mockk |
| `featureServer` ViewModel | Unit | coroutines-test |
| UI screens | Screenshot | Roborazzi (already configured) |
| Android Service lifecycle | Instrumented | AndroidJUnit4 |

---

### Milestone Roadmap

| Milestone | Deliverable | Scope |
|-----------|-------------|-------|
| **M1** | Android build pipeline | ollama binary cross-compiled for Android ARM64, committed as asset |
| **M2** | Android server | ForegroundService starts/stops ollama subprocess |
| **M3** | Common API | `coreOllama` module with Ktor client, all Ollama API calls working |
| **M4** | Model UI | List, pull, delete models on Android |
| **M5** | Chat UI | Streaming chat working on Android |
| **M6** | iOS llama.cpp build | llama.cpp XCFramework (arm64 + simulator) with Metal, linked into Xcode project |
| **M7** | iOS cinterop | Kotlin/Native cinterop bindings to llama.cpp; model loading + basic inference |
| **M8** | iOS Ktor server | Ktor CIO server in-process on iOS exposing Ollama-compatible API |
| **M9** | iOS UI | Same chat/model UI working on iOS (foreground-only caveat noted in UI) |
| **M10** | Binary delivery | Play Asset Delivery or download-on-first-run for Android |

---

### Open Architecture Decisions (need answers before M1)

1. **Android binary delivery** - Play Asset Delivery, direct APK, or download-on-first-run?
2. **Network exposure** - localhost-only (`127.0.0.1`) or toggleable LAN exposure (`0.0.0.0`)?
3. **Port conflict** - fixed 11434 or auto-detect next free port?
4. **App identity** - final package name / app name?
5. **Android background persistence** - server stays running after app closes (via a persistent ForegroundService), or stops when the user leaves the app?
6. **iOS background UX** - show a visible "Server paused" banner when the iOS app goes to background, or silently pause/resume?

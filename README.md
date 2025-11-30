# fiabrica

**Kotlin-based Minecraft hacked client mod for Fabric 1.21.10**

## 🎯 Features

### Event-Driven Architecture
- **EventBus**: Thread-safe central event system with automatic cleanup
- **Module System**: Base class for all hacks with auto registration/unregistration
- **State Machine**: Each module uses FSM pattern for complex behavior

### Modules

#### Movement
- **Fly**: Creative-style flight in survival with adjustable speed

#### Combat
- **Killaura**: Humanized combat bot with Grim AC bypass
  - Silent rotations (fake view - server sees rotation, client camera unchanged)
  - Smooth interpolation with ease-in-out curve
  - Noise injection (±0.1° micro-corrections)
  - Raycast line-of-sight checks
  - Attack cooldown respect (99% threshold)
  - Random packet drops (5% chance)
  - Configurable: Range (3-6), CPS (8-20), Rotation Speed (5-30)

### GUI
- **CraftUI Integration**: ImGui-based interface
- **Keybind**: Right Shift opens GUI
- **Dynamic Categories**: Auto-populated from ModuleManager
- **Live Settings**: Sliders, checkboxes, tooltips

## 🛠️ Tech Stack

- **Fabric Loader:** 0.18.1
- **Minecraft:** 1.21.10
- **Yarn Mappings:** 1.21.10+build.3
- **Fabric API:** 0.138.3+1.21.10
- **Kotlin:** 2.2.21 (via Fabric Language Kotlin 1.13.7)
- **Java:** 21
- **CraftUI:** 0.3.0 (Dear ImGui wrapper)
- **Gradle:** 8.10

## 🚀 Quick Start

### Requirements
- Java 21+ ([Download](https://adoptium.net/))
- Git

### Build (Automated)

**Linux/macOS:**
```bash
git clone https://github.com/rafalohaki/fiabrica.git
cd fiabrica
chmod +x compile.sh
./compile.sh
```

**Windows:**
```cmd
git clone https://github.com/rafalohaki/fiabrica.git
cd fiabrica
compile.bat
```

The script will:
1. ✅ Auto-download `gradle-wrapper.jar` if missing
2. ✅ Detect Java version (requires 21+)
3. ✅ Optionally clean previous builds
4. ✅ Build the project
5. ✅ Show output JAR location

### Build (Manual)

```bash
git clone https://github.com/rafalohaki/fiabrica.git
cd fiabrica
./gradlew build  # Linux/macOS
gradlew.bat build  # Windows
```

Output JAR: `build/libs/fiabrica-1.0.0.jar`

## 📦 Installation

1. Build the mod (see above)
2. Copy `fiabrica-1.0.0.jar` to your Minecraft mods folder:
   - **Linux/macOS**: `~/.minecraft/mods/`
   - **Windows**: `%APPDATA%\.minecraft\mods\`
3. Launch Minecraft with Fabric 0.18.1+
4. Press **Right Shift** in-game to open GUI

## 🔧 Development

### Run Dev Client
```bash
./gradlew runClient
```

### Project Structure
```
fiabrica/
├── src/
│   ├── main/
│   │   ├── kotlin/com/rafalohaki/
│   │   │   ├── Fiabrica.kt              # Main mod initializer
│   │   │   ├── event/
│   │   │   │   ├── EventBus.kt          # Central event system
│   │   │   │   ├── Event.kt             # Base event classes
│   │   │   │   └── Events.kt            # All event definitions
│   │   │   └── module/
│   │   │       ├── Module.kt            # Base module class
│   │   │       ├── ModuleManager.kt     # Module registry
│   │   │       └── modules/
│   │   │           ├── FlyModule.kt
│   │   │           └── KillauraModule.kt
│   │   ├── java/com/rafalohaki/mixin/   # Mixins (Java)
│   │   └── resources/
│   │       ├── fabric.mod.json
│   │       ├── fiabrica.mixins.json
│   │       └── fiabrica.client.mixins.json
│   └── client/
│       ├── kotlin/com/rafalohaki/
│       │   ├── FiabricaClient.kt        # Client initializer
│       │   └── FiabricaGuiApp.kt        # ImGui GUI
│       └── java/com/rafalohaki/mixin/client/
├── build.gradle
├── gradle.properties
├── settings.gradle
├── compile.sh                            # Auto-build script (Linux/macOS)
├── compile.bat                           # Auto-build script (Windows)
└── README.md
```

### Adding a New Module

1. **Create module class:**
```kotlin
class MyHackModule : Module(
    name = "MyHack",
    description = "Does something cool",
    category = Category.MISC
) {
    override fun onEnable() {
        // Enable logic
    }
    
    override fun registerEvents() {
        EventBus.register<ClientTickEvent> { event ->
            // Your logic here
        }
    }
}
```

2. **Register in ModuleManager.kt:**
```kotlin
init {
    register(MyHackModule())
}
```

3. **Done!** Module auto-appears in GUI.

## 🎮 Usage

1. Launch Minecraft
2. Press **Right Shift** to open GUI
3. Navigate categories (Movement, Combat, Render)
4. Toggle modules with checkboxes
5. Adjust settings with sliders
6. Hover for tooltips

## 🔐 Grim AC Bypass

Killaura uses advanced techniques:
- **Silent Rotations**: Server sees rotation, client camera stays fixed
- **Humanization**: Ease-in-out interpolation, noise injection, random delays
- **Smart Targeting**: Raycast LOS checks, cooldown respect, CPS limits
- **State Machine**: IDLE → SCANNING → ROTATING → READY → ATTACKING

Result: Server sees "skilled human player", not "perfect robot".

## 📄 License

CC0-1.0 (Public Domain)

## 🤝 Contributing

Pull requests welcome! Please:
1. Follow existing code style (Kotlin conventions)
2. Test with `./gradlew build`
3. Document new modules in README

## ⚠️ Disclaimer

This project is for educational purposes only. Using hacked clients on multiplayer servers may violate their Terms of Service. Use at your own risk.

---

**Made with ❤️ for bypassing Grim AC**

# MineGlot v2

MineGlot v2 is a Minecraft translation mod that uses a locally hosted translation server to translate in-game messages.

Instead of relying on an external translation API, MineGlot communicates with a Python server running locally on the player's machine. The server uses [Argos Translate](https://github.com/argosopentech/argos-translate), an open-source offline translation library.

The mod is completed and is actively used in Minecraft.

## How It Works

MineGlot uses two components:

1. **Minecraft Mod:** Handles the Minecraft-side integration and communicates with the translation server.
2. **Local Translation Server:** Processes translation requests using Argos Translate.

The translation server runs on the player's machine, allowing translations to be processed locally without depending on a third-party translation API.

## Translation Languages

The default configuration primarily supports:

- Russian → English
- English → Russian

However, MineGlot is not limited to these language pairs.

Users can extend translation support to other languages by:

1. Installing the required language packages through Argos Translate.
2. Updating the translation server's language configuration.
3. Restarting the translation server.

This allows users to configure translation between other supported language pairs without changing the fundamental architecture of the Minecraft mod.

Available language pairs depend on the Argos Translate packages installed on the system.

## Technology Stack

| Component | Technology |
|---|---|
| Minecraft mod | Java |
| Minecraft version | 1.8.9 |
| Mod loader | Minecraft Forge |
| Java compatibility | Java 8 |
| Translation server | Python |
| Translation engine | Argos Translate |
| Communication | Local HTTP API |

## Translation Server

The local server runs at:

```text
http://127.0.0.1:5000
```

It exposes a translation endpoint:

```text
POST /translate
```

Example request:

```json
{
  "q": "Привет",
  "source": "ru",
  "target": "en"
}
```

Example response:

```json
{
  "translatedText": "Hello"
}
```

## Requirements

- Minecraft 1.8.9
- Minecraft Forge
- Java 8
- Python
- Argos Translate

## Building the Mod

On Windows:

```bash
gradlew.bat clean build
```

On Linux/macOS:

```bash
./gradlew clean build
```

## Running the Translation Server

Install the required Python dependency:

```bash
pip install argostranslate
```

Start the server:

```bash
python argos_server.py
```

The server checks for its configured translation models and installs missing models when required. Initial model downloads require an internet connection; installed models can subsequently be used locally.

## Project Status

**Completed and actively used.**

MineGlot v2 was developed to provide a locally hosted translation solution for Minecraft. Its translation language support can be extended through Argos Translate packages and server configuration.

## License

See [LICENSE](LICENSE) for licensing information.

## Author

**Developed by [Shaheer Ali](https://github.com/shaheer-ali-dev)**
MineGlot v2 is a personal project developed to explore and build a practical, locally hosted translation solution for Minecraft.



# MineGlot Translation Server

Local translation API for the MineGlot Minecraft mod. Translation is handled by
[Argos Translate](https://github.com/argosopentech/argos-translate) on the same
computer as the Minecraft client.

## Requirements

- Python 3.9 or newer
- An internet connection for the first installation of language models
- The Minecraft mod configured to send requests to `http://127.0.0.1:5000`

## Setup

Create and activate a virtual environment:

```bash
python -m venv .venv
```

Windows PowerShell:

```powershell
.venv\Scripts\Activate.ps1
```

Windows Command Prompt:

```bat
.venv\Scripts\activate.bat
```

Install dependencies and start the server:

```bash
python -m pip install -r requirements.txt
python argos_server.py
```

On first startup, the server checks for the configured Argos language packages
and installs missing packages. This first installation needs internet access.
After that, translation runs locally.

## API

### Health check

```http
GET /health
```

Example response:

```json
{
  "status": "ok",
  "pairs": ["ru->en", "en->ru"]
}
```

### Translate text

```http
POST /translate
Content-Type: application/json
```

Request:

```json
{
  "q": "Привет",
  "source": "ru",
  "target": "en"
}
```

Response:

```json
{
  "translatedText": "Hello"
}
```

The default supported pairs are `ru->en` and `en->ru`.

## Configuration

The defaults are suitable for a local Minecraft installation. Optional
environment variables are documented in `.env.example`.

For example, to change the port in PowerShell for the current terminal session:

```powershell
$env:MINEGLOT_PORT = "5050"
python argos_server.py
```

To add or change language pairs, set `MINEGLOT_PAIRS` to comma-separated
`source:target` pairs. The matching Argos packages must be available.

## Security and network scope

The server binds to `127.0.0.1` by default, so it is intended for local use.
Do not bind it to a public network interface without adding appropriate
authentication, access controls, and deployment hardening.

## Project layout

```text
translation-server/
├── argos_server.py
├── translator.py
├── model_manager.py
├── config.py
├── requirements.txt
├── .env.example
└── README.md
```

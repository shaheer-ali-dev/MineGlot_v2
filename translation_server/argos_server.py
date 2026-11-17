"""HTTP API for the MineGlot local translation server.

Run from this directory:
    python argos_server.py

Health check:
    GET http://127.0.0.1:5000/health

Translation:
    POST http://127.0.0.1:5000/translate
    {"q": "Привет", "source": "ru", "target": "en"}
"""
from __future__ import annotations

import json
from http.server import BaseHTTPRequestHandler, ThreadingHTTPServer
from typing import Any

import argostranslate.translate

from config import HOST, MAX_REQUEST_BYTES, PAIRS, PORT
from model_manager import ensure_models
from translator import TranslationError, translate_text


class MineGlotHandler(BaseHTTPRequestHandler):
    """Handle health checks and translation requests."""

    server_version = "MineGlotLocalServer/2.0"

    def _send_json(self, status: int, payload: dict[str, Any]) -> None:
        body = json.dumps(payload, ensure_ascii=False).encode("utf-8")
        self.send_response(status)
        self.send_header("Content-Type", "application/json; charset=utf-8")
        self.send_header("Content-Length", str(len(body)))
        self.send_header("Cache-Control", "no-store")
        self.end_headers()
        self.wfile.write(body)

    def do_GET(self) -> None:
        if self.path.rstrip("/") not in ("", "/health"):
            self._send_json(404, {"error": "Not found. Try GET /health."})
            return

        self._send_json(
            200,
            {
                "status": "ok",
                "pairs": [f"{source}->{target}" for source, target in PAIRS],
            },
        )

    def do_POST(self) -> None:
        if self.path.rstrip("/") != "/translate":
            self._send_json(404, {"error": "Not found. Use POST /translate."})
            return

        content_type = self.headers.get("Content-Type", "")
        if "application/json" not in content_type.lower():
            self._send_json(415, {"error": "Content-Type must be application/json."})
            return

        try:
            raw_length = self.headers.get("Content-Length")
            if raw_length is None:
                self._send_json(411, {"error": "Content-Length header is required."})
                return

            length = int(raw_length)
            if length < 0:
                self._send_json(400, {"error": "Invalid Content-Length."})
                return
            if length > MAX_REQUEST_BYTES:
                self._send_json(413, {"error": "Request body is too large."})
                return

            raw_body = self.rfile.read(length)
            data = json.loads(raw_body.decode("utf-8"))

            if not isinstance(data, dict):
                self._send_json(400, {"error": "JSON body must be an object."})
                return

            text = data.get("q")
            source = data.get("source", "ru")
            target = data.get("target", "en")

            if not isinstance(text, str) or not text.strip():
                self._send_json(400, {"error": "'q' must be a non-empty string."})
                return
            if not isinstance(source, str) or not isinstance(target, str):
                self._send_json(400, {"error": "'source' and 'target' must be language codes."})
                return

            translated = translate_text(text, source.lower(), target.lower())
            self._send_json(200, {"translatedText": translated})

        except UnicodeDecodeError:
            self._send_json(400, {"error": "Request body must be valid UTF-8."})
        except json.JSONDecodeError:
            self._send_json(400, {"error": "Request body must contain valid JSON."})
        except ValueError:
            self._send_json(400, {"error": "Invalid Content-Length."})
        except TranslationError as exc:
            self._send_json(400, {"error": str(exc)})
        except Exception:
            # Keep internal exception details out of the HTTP response.
            self._send_json(500, {"error": "Internal translation server error."})

    def log_message(self, fmt: str, *args: Any) -> None:
        # Keep the console quiet during normal gameplay.
        return


def main() -> None:
    ensure_models()

    # Warm up configured translation pairs so the first request is less likely
    # to pay the model-loading cost. Empty text is not sent to the translator.
    installed = argostranslate.translate.get_installed_languages()
    installed_codes = {language.code for language in installed}
    for source, target in PAIRS:
        if source in installed_codes and target in installed_codes:
            try:
                translate_text("MineGlot", source, target)
            except TranslationError:
                # A missing pair is reported when the user requests it.
                pass

    server = ThreadingHTTPServer((HOST, PORT), MineGlotHandler)
    print(f"MineGlot translation server listening on http://{HOST}:{PORT}", flush=True)
    try:
        server.serve_forever()
    except KeyboardInterrupt:
        print("\nStopping MineGlot translation server...", flush=True)
    finally:
        server.server_close()


if __name__ == "__main__":
    main()

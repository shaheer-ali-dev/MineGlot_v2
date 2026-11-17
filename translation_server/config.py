"""Configuration for the MineGlot local translation server."""
from __future__ import annotations

import os


def _read_pairs(value: str) -> tuple[tuple[str, str], ...]:
    """Parse pairs such as 'ru:en,en:ru' from an environment variable."""
    pairs = []
    for item in value.split(","):
        item = item.strip()
        if not item:
            continue
        parts = item.split(":")
        if len(parts) != 2 or not all(part.strip() for part in parts):
            raise ValueError(
                "MINEGLOT_PAIRS must use comma-separated source:target pairs, "
                "for example ru:en,en:ru."
            )
        pairs.append((parts[0].strip().lower(), parts[1].strip().lower()))
    if not pairs:
        raise ValueError("At least one language pair must be configured.")
    return tuple(dict.fromkeys(pairs))


# Bind to loopback only: the service is intended for the Minecraft client
# running on the same computer, not for access from other machines.
HOST = os.getenv("MINEGLOT_HOST", "127.0.0.1")
PORT = int(os.getenv("MINEGLOT_PORT", "5000"))

# Keep this list aligned with the languages your mod offers.
PAIRS = _read_pairs(os.getenv("MINEGLOT_PAIRS", "ru:en,en:ru"))

# Set to "false" if you want to manage model installation yourself.
AUTO_INSTALL_MODELS = os.getenv("MINEGLOT_AUTO_INSTALL_MODELS", "true").lower() in {
    "1", "true", "yes", "on"
}

MAX_REQUEST_BYTES = int(os.getenv("MINEGLOT_MAX_REQUEST_BYTES", str(64 * 1024)))

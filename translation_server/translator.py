"""Translation service built on top of Argos Translate."""
from __future__ import annotations

import argostranslate.translate

from config import PAIRS


class TranslationError(Exception):
    """Raised when a translation request cannot be completed."""


def translate_text(text: str, source: str, target: str) -> str:
    """Translate text using an installed Argos language pair."""
    if not text or not text.strip():
        raise TranslationError("Text to translate cannot be empty.")

    if (source, target) not in PAIRS:
        supported = ", ".join(f"{src}->{dst}" for src, dst in PAIRS)
        raise TranslationError(
            f"Unsupported language pair {source}->{target}. Supported pairs: {supported}"
        )

    try:
        result = argostranslate.translate.translate(text, source, target)
    except Exception as exc:
        # Keep the public API message useful without exposing internal details.
        raise TranslationError(
            f"Translation failed for language pair {source}->{target}."
        ) from exc

    if not isinstance(result, str):
        raise TranslationError("The translation engine returned an invalid result.")

    return result

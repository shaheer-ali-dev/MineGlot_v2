"""Checks and installs the Argos Translate language packages."""
from __future__ import annotations

import sys

import argostranslate.package
import argostranslate.translate

from config import AUTO_INSTALL_MODELS, PAIRS


def get_installed_pairs() -> set[tuple[str, str]]:
    """Return all currently installed source/target language pairs."""
    installed = set()
    for language in argostranslate.translate.get_installed_languages():
        for translation in language.translations_from:
            installed.add((language.code, translation.to_lang.code))
    return installed


def ensure_models() -> None:
    """Install configured language pairs that are not installed yet.

    First-time installation requires an internet connection. Once installed,
    Argos Translate can perform translations locally.
    """
    installed = get_installed_pairs()
    missing = [pair for pair in PAIRS if pair not in installed]

    if not missing:
        print("All configured Argos language models are installed.", flush=True)
        return

    if not AUTO_INSTALL_MODELS:
        missing_text = ", ".join(f"{src}->{dst}" for src, dst in missing)
        raise RuntimeError(
            "Missing Argos language models: "
            f"{missing_text}. Install them or enable AUTO_INSTALL_MODELS."
        )

    print(f"Missing language models: {missing}", flush=True)
    print("Updating the Argos package index...", flush=True)
    argostranslate.package.update_package_index()
    available = argostranslate.package.get_available_packages()

    for source, target in missing:
        package = next(
            (
                item for item in available
                if item.from_code == source and item.to_code == target
            ),
            None,
        )

        if package is None:
            raise RuntimeError(
                f"No Argos package is available for {source}->{target}."
            )

        print(f"Downloading and installing {source}->{target}...", flush=True)
        argostranslate.package.install_from_path(package.download())
        print(f"Installed {source}->{target}.", flush=True)

    remaining = set(PAIRS) - get_installed_pairs()
    if remaining:
        missing_text = ", ".join(f"{src}->{dst}" for src, dst in sorted(remaining))
        print(f"Warning: some language pairs remain unavailable: {missing_text}", file=sys.stderr)

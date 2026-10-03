# REPO1 — Public repository housekeeping

## Goal

Prepare the public GitHub repository as the canonical development workspace without changing app runtime behavior.

## Scope

- keep `/doc` as the only normative documentation tree and remove the redundant `/docs` legacy path;
- present the project publicly as an independent community app, clearly separated from the abfahrt.now API operator/developer;
- document the EU-first external-service policy and credential isolation;
- expose the existing 22 bundled UI locales in public project documentation;
- make Gradle/CI reproducibility explicit, including Windows wrapper bootstrap and GitHub Actions;
- retain Build 149 / versionCode 1490 unchanged.

## Non-goals

- no update-checker implementation;
- no startup-performance change;
- no dependency, API-contract or UI behavior change.

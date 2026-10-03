# DOC2 Specification — Build 151 documentation convergence

## Goal
Converge the normative `/doc` tree to the accepted runtime baseline after REPO1, Build 150 and Build 151, without changing Android runtime behavior or versioning.

## Scope
- keep Build 151 (`versionCode 1510`, `versionName 1.1.0`) as the last accepted runtime baseline;
- record REPO1, Build 150 and Build 151 in the normative changelog and durable decision/evidence history;
- update stale current-build markers in Android compatibility, localization, backlog, release plan and handoff;
- record Build 152 as in progress only: PR #6 / Android CI #52 is green, but real-device acceptance and remaining UI/UX scope are still outstanding;
- record the Build-152 access-gate convergence concern as an open finding before further runtime work;
- keep AB-018 reserved for Build 153.

## Non-goals
- no Android source/resource/build-script change;
- no `versionCode` or `versionName` change;
- no Build-152 implementation change;
- no claim that CI #52 alone accepts Build 152;
- no re-opening of already accepted Build 150/151 runtime behavior.

## Acceptance
- changed runtime files: none;
- `/doc` current-state references are mutually consistent;
- REPO1/Build 150/Build 151 acceptance is represented in changelog, decisions and test/evidence history;
- Build 152 is consistently marked in progress/pending runtime evidence;
- normal documentation/governance checks are green.

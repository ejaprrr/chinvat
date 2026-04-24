# Style Guide

## Java Formatting

This repository uses **Google Java Style** enforced by Spotless (`google-java-format`).

Run formatter:

```bash
cd /Users/jiriposavad/Documents/FullStack/chinvat-malaga-2026/backend
./mvnw spotless:apply
```

Check formatting in CI/local:

```bash
cd /Users/jiriposavad/Documents/FullStack/chinvat-malaga-2026/backend
./mvnw spotless:check
```

## Minimalist Comments Policy

- Prefer clear naming and small methods over explanatory comments.
- Add comments only when intent is non-obvious.
- Avoid comments that restate what the code already says.
- Keep comments short, factual, and in English.
- Document external constraints and security caveats when needed.


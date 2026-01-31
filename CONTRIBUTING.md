# Contributing

Welcome to contribute to this project! To ensure an effective collaboration, please read the following guidelines.

## Pull Requests

1. Open an issue to discuss changes.
2. Fork the repository and create a new branch named like `feat/my-feature`, code in it.
3. add `Pfull-build=true` to abi-validating related tasks, for full build is disabled to boost dev build speed.
4. run `./gradlew checkLegacyAbi` to check your changes. If including a breaking change, run `./gradlew updateLegacyAbi`.
5. When creating PR, use a concise, descriptive PR title with prefixes like `feat:` or `fix:`. Please pull to `main` branch.



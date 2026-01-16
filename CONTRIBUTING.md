# Contributing

Welcome to contribute to this project! To ensure an effective collaboration, please read the following guidelines.

## Pull Requests

- Open an issue to discuss changes before coding.
- Fork the repository and create a new branch named like "feat/my-feature".
- Write your codes and tests in the new branch.
- Follow [Kotlin Coding Conventions](https://kotlinlang.org/docs/coding-conventions.html).
- add `Pfull-build=true` to abi-validating related tasks.
- run `./gradlew checkLegacyAbi` to check your changes. If including a breaking change, run `./gradlew updateLegacyAbi`.
- Use concise, descriptive PR titles with prefixes like `feat:` or `fix:`.
- Link PRs to issues with `Closes #xx`.
- Create a pull request to the `main` branch.
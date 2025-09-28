# How to publish a new release

- Update `versionCode` and `versionName` in [libs.versions.toml](/gradle/libs.versions.toml)
- Create a changelog in: `\fastlane\metadata\android\en-US`
- Create a **semvar** tag in the form of "v1.0.0" on `master` and push it, this will trigger a release.
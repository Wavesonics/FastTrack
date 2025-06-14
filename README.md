# FastTrack

Free and Open Source app for tracking Fasting

[![Google Play](https://img.shields.io/endpoint?color=green&logo=google-play&logoColor=green&url=https%3A%2F%2Fplay.cuzi.workers.dev%2Fplay%3Fi%3Dcom.darkrockstudios.apps.fasttrack%26l%3DGoogle%2520Play%26m%3D%24version)](https://play.google.com/store/apps/details?id=com.darkrockstudios.apps.fasttrack)
[![F-Droid](https://img.shields.io/f-droid/v/com.darkrockstudios.apps.fasttrack?logo=FDROID)](https://f-droid.org/en/packages/com.darkrockstudios.apps.fasttrack/)
[![GitHub](https://img.shields.io/github/v/release/Wavesonics/FastTrack?include_prereleases&logo=github)](https://github.com/Wavesonics/FastTrack/releases/latest)

Intermittent fasting has many benefits! But it can be very hard to stay motivated during a fast.

This is a simple FOSS app intended to help keep you motivated. It is not a medical app and does not provide medical
advice.

This uses a "rule-of-thumb" quality model for determining what fasting stage you are currently in. It has been somewhat
validated through real world testing using a ketone breath meter, as well as reading medical studies on the matter, but
even still, it is just a rule-of-thumb. There are many things that would affect what stage you are in on this particular
fast.

## Privacy

We don't collect any data whatsoever. The app doesn't even have the INTERNET permission. The app operates entirely
locally.

## Development

FastTrack is not in active development; however, I will fix bugs if they are reported. If you have some neat feature
you'd like to contribute, open an issue to discuss it.

### Release

- Update `versionCode` and `versionName` in [libs.versions.toml](/gradle/libs.versions.toml)
- Create a **semvar** tag in the form of "v1.0.0" on `master` and push it, this will trigger a release.

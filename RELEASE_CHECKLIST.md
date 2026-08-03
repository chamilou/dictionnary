# Google Play Release Checklist

This checklist tracks what is still required to publish `dictionnary` on Google Play.

Current status as of August 3, 2026:

- `targetSdk = 36` and `compileSdk = 36`
- `testDebugUnitTest` passes
- `lintDebug` passes
- `assembleRelease` passes
- Gradle warning cleanup is complete
- Release shrinking/obfuscation is enabled for `release`
- Room schema export is enabled and checked into `app/schemas/`
- Android backup is disabled with `android:allowBackup="false"`
- Release signing is not configured yet
- A public privacy policy URL is not set yet
- Play Console declarations and store assets are not prepared yet

## Repo Checklist

- [ ] Create a release upload keystore and keep it outside the repo.
- [ ] Add release signing config for the Play upload build.
- [ ] Build and verify a signed Android App Bundle (`.aab`).
- [ ] Bump `versionCode` and set a production `versionName`.
- [ ] Publish a real privacy policy page at a public URL.
- [ ] Make sure in-app privacy/support text matches the public privacy policy.
- [ ] Review unsupported or draft language directions and hide, disable, or label them clearly.
- [ ] Run a final manual QA pass on a release build.
- [ ] Generate final Play screenshots from the shipping UI.
- [ ] Prepare a 512x512 Play icon and 1024x500 feature graphic.

## Play Console Checklist

- [ ] Create the Play app entry.
- [ ] Choose `App` vs `Game`.
- [ ] Choose `Free` vs `Paid`.
- [ ] Add store contact email.
- [ ] Enroll in Play App Signing and register the upload key.
- [ ] Upload the first signed `.aab`.
- [ ] Complete the Data safety form.
- [ ] Add the public privacy policy URL.
- [ ] Complete App content declarations.
- [ ] Declare whether the app contains ads.
- [ ] Complete Target audience and content.
- [ ] Complete the IARC content rating questionnaire.
- [ ] Add store listing text: app title, short description, full description.
- [ ] Add store listing graphics: icon, feature graphic, screenshots.
- [ ] Choose countries or regions for distribution.
- [ ] If the app is paid, create and link a payments profile.
- [ ] If Play review needs gated access, add reviewer instructions in App access.

## Testing / Release Gate

- [ ] Run Internal testing first.
- [ ] If the developer account is a new personal account created after November 13, 2023, complete Closed testing with at least 12 opted-in testers for 14 continuous days before requesting production access.
- [ ] Promote to production only after Play review passes.

## Policy / Deadline Checks

- [ ] Keep `targetSdk` at API 36 or higher for Play submissions after August 31, 2026.
- [ ] Register the app package in Play Console before September 30, 2026.
- [ ] Make sure developer identity verification is complete in Play Console.

## Current Repo State

- [x] `targetSdk = 36`
- [x] `compileSdk = 36`
- [x] `testDebugUnitTest` passes
- [x] `lintDebug` passes
- [x] `assembleRelease` passes
- [x] Release shrinking/obfuscation enabled
- [x] Room schema export enabled
- [x] Backup policy made explicit in manifest
- [ ] Release signing configured
- [ ] Public privacy policy URL published
- [ ] Signed release bundle built
- [ ] Play listing assets prepared
- [ ] Play Console declarations completed

## Official References

- Target API requirement:
  https://developer.android.com/google/play/requirements/target-sdk
- Create and set up a Play app:
  https://support.google.com/googleplay/android-developer/answer/9859152?hl=en
- Data safety:
  https://support.google.com/googleplay/android-developer/answer/10787469?hl=en
- App content declarations:
  https://support.google.com/googleplay/android-developer/answer/9859455?hl=en
- Store listing assets:
  https://support.google.com/googleplay/android-developer/answer/9866151?hl=en
- Personal-account closed testing requirement:
  https://support.google.com/googleplay/android-developer/answer/14151465?hl=en
- Play App Signing:
  https://support.google.com/googleplay/android-developer/answer/9842756?hl=en
- Paid app pricing and payments:
  https://support.google.com/googleplay/android-developer/answer/6334373?hl=en
  https://support.google.com/googleplay/android-developer/answer/3092739?hl=en
- Package name registration:
  https://support.google.com/googleplay/android-developer/answer/16984799?hl=en-EN

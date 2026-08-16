# Internal Test Release

## Release signing

Create a local signing key once and back up the resulting `.jks` file and its passwords in a secure password manager. Do not commit either the key or `keystore.properties`.

```bash
mkdir -p release
keytool -genkeypair -keystore release/mealledger-release.jks -alias mealledger -keyalg RSA -keysize 4096 -validity 10000
cp keystore.properties.example keystore.properties
```

Set the file's values to the key path, alias, and passwords. Then create a signed installable APK:

```bash
./gradlew :app:assembleRelease
```

The artifact is `app/build/outputs/apk/release/app-release.apk`.

## Manual Test Checklist

- [ ] Install the release APK on a physical Android device and open the app.
- [ ] Add a food entry with a meal type, calories, optional protein, and a price.
- [ ] Add a water entry using a cup shortcut and a custom amount.
- [ ] Edit then delete one food entry and one water entry; confirm Today totals update.
- [ ] Change currency, daily water goal, and default cup size; restart the app and confirm they persist.
- [ ] Confirm the Summary tab includes seven dates, shows empty days clearly, and reflects recorded totals.
- [ ] Confirm the launcher icon and app name display as Meal Ledger.
- [ ] Confirm the device is offline and the main flows still work.

## Privacy Statement

The current privacy statement is maintained in [privacy-policy.md](privacy-policy.md). Before a Play Console submission, publish the same policy at a public HTTPS URL and add that URL to the store listing.

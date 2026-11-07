# TORQUE — Release Checklist

- [x] Unique Android `applicationId` configured (`com.torque.bikecare`).
- [x] Custom adaptive launcher icon generated with Material Design 3 foreground and background layers.
- [x] Raster launcher icons (`ic_launcher.png`, `ic_launcher_round.png`) generated for all five mipmap densities (`mdpi`, `hdpi`, `xhdpi`, `xxhdpi`, `xxxhdpi`).
- [x] Default green Android `.webp` files removed.
- [x] Firebase Authentication provisioned and integrated with Jetpack Credential Manager (`GetSignInWithGoogleOption`).
- [x] Cloud Firestore provisioned with enterprise database ID.
- [x] Zero-Trust Firestore Security Rules (`firestore.rules`) audited against the 8 Pillars and deployed via `DeployRules`.
- [x] Composite indexes declared in `firestore.indexes.json`.
- [x] No hardcoded sample or fake data; all data operations backed by Firestore.
- [x] All visible UI actions connected to real working event handlers.
- [x] WhatsApp deep linking helper configured with international phone number sanitization.
- [x] Strict admin authorization gate configured for `muzamillp20@gmail.com`.
- [x] Edge-to-edge transparent system bars enabled with insets padding.
- [x] Tier 1 & Tier 2 security tests passing.
- [x] Clean compilation verified via `compile_applet`.

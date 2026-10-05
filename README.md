# Jay Mata Vaishno Devi Family Pathpedi — Android

This project packages the tested Phase 10 Pathpedi HTML application inside an Android WebView.

## Build with GitHub Actions

1. Create a GitHub repository.
2. Upload this complete project.
3. Push to the `main` branch or run **Actions → Build Pathpedi APK → Run workflow**.
4. Download the `Pathpedi-debug-apk` artifact from the completed workflow.

The application keeps the existing HTML/JavaScript accounting logic and uses Android WebView for the mobile shell. LocalStorage data remains inside the app's WebView storage.

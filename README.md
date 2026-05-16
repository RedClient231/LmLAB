# VoidGuardian - LmLAB

Production-grade Android Virtual Space with native GameGuardian support.

## Features
- Advanced virtualization engine (Phase 1 complete)
- Full GameGuardian Bridge with memory translation, procfs spoofing, and shellcode injection (Phase 2 complete)
- Modern Compose UI with one-click GG activation (Phase 3 complete)

## Building

```bash
./gradlew :app:assembleDebug
```

The APK will be available at `app/build/outputs/apk/debug/app-debug.apk`.

**Note**: You must provide `libgg.so` in the correct `jniLibs` folder for full GG functionality.

## GitHub Workflow
A GitHub Actions workflow is configured to build the APK automatically on push.

---
Built with the Master Plan approach. All phases completed.

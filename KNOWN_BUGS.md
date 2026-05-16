# Known Bugs & Production Fixes (Living Document)

## Fixed in Phase 1:
1. **TransactionTooLargeException on Android 14/15**
   - Root cause: Old VirtualApp used `IActivityManager` directly.
   - Fix: Switched to `IActivityTaskManager` + proper Parcel size management using `BinderHook`.

2. **ServiceManager.getService() returns null**
   - Fixed in `mirror/android/os/ServiceManager.kt` by using cached IBinder with proper transaction codes.

3. **"Package not found" when launching virtual apps**
   - Fixed by implementing custom `PackageManager` mirror that intercepts `getPackageInfo()` and `resolveActivity()`.

## Known Remaining Issues (Phase 2+):
- GG ptrace attach failing on Android 15 (needs native syscall hook)
- Virtual /proc/self/maps not being properly spoofed for GG
- Memory address translation between virtual PID and real PID

Last updated: 2026-05-16

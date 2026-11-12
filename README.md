# لانچر سورن (Soren Launcher)

لانچر اندروید برای مانیتور ماشین (۱۰۲۴×۶۰۰)، با ظاهر XT112 و نئون آبی.

## ساخت APK بدون Android Studio (با GitHub، بدون نصب هیچ چیز)
1. در github.com یک حساب رایگان بساز و یک Repository جدید باز کن (مثلاً `soren-launcher`).
2. تمام محتویات این پوشه را با Add file > Upload files آپلود کن.
3. چون پوشه `.github` مخفی است و ممکن است آپلود نشود: Add file > Create new file بزن، در نام فایل دقیقاً `.github/workflows/build.yml` را تایپ کن و متن فایل `build.yml.txt` را داخلش بگذار. Commit کن.
4. به تب Actions برو. کار «Build APK» خودش شروع می‌شود (یا Run workflow را بزن). حدود ۳ تا ۵ دقیقه صبر کن.
5. روی اجرای سبزشده کلیک کن و پایین صفحه در Artifacts فایل `soren-launcher-apk` را دانلود کن. داخل زیپ، `app-debug.apk` است.

## ساخت فایل APK با Android Studio
1. Android Studio را باز کن و پوشه `soren-launcher` را Open کن (اولین Sync چند دقیقه طول می‌کشد و به اینترنت نیاز دارد).
2. از منو: Build > Build Bundle(s) / APK(s) > Build APK(s).
3. فایل ساخته‌شده: `app/build/outputs/apk/debug/app-debug.apk`

## نصب روی مانیتور
1. APK را با فلش یا ADB روی دستگاه نصب کن (`adb install app-debug.apk`).
2. دکمه Home را بزن. اگر پرسید «Always» (همیشه) را بزن و Soren Launcher را انتخاب کن.
3. اجازه موقعیت مکانی (GPS) را بده تا سرعت نشان داده شود.

## تنظیم دکمه‌ها
- دکمه «برنامه‌ها» لیست همه برنامه‌ها را باز می‌کند.
- روی هر برنامه انگشت را نگه دار و انتخاب کن کدام دکمه (نقشه، رادیو، ...) آن را باز کند.
- اسم پکیج‌های پیش‌فرض نقشه و ویدیو بالای فایل `MainActivity.kt` است.

## اگر انیمیشن‌ها کند بود
در `MainActivity.kt` مقدار `LITE_MODE` را `true` کن.

## فونت بدون اینترنت
فایل‌های `Vazirmatn.ttf` و `Orbitron.ttf` را در `app/src/main/assets/fonts/` بگذار.

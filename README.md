# 810hStudy

810時間勉強するだけのアプリです。たまげたなぁ


## 必要環境

- Android 8.0 (API 26) 以上
- ビルド: JDK 17 以上、Android SDK Platform 36 / Build-Tools 36.0.0
- このリポジトリは `local.properties` の `sdk.dir` を環境に合わせて変更して使用してください。

## ビルドとインストール

```powershell
# デバッグAPK
.\gradlew.bat assembleDebug

# 実機 / エミュレータへインストール
adb install -r app\build\outputs\apk\debug\app-debug.apk
```

APK は `app/build/outputs/apk/debug/app-debug.apk` に出力されます。


## 構成

```
app/src/main/java/com/study810/app/
├── MainActivity.kt                 # Edge-to-edge + Compose エントリ
├── Study810Application.kt          # AppContainer / リポジトリ生成
├── data/
│   ├── StudySession.kt             # Room Entity
│   ├── StudySessionDao.kt          # DAO (Flow)
│   ├── StudyDatabase.kt            # Room DB
│   ├── StudyPreferences.kt         # DataStore (計測開始時刻・初回起動日)
│   └── StudyRepository.kt          # 開始/停止・セッション保存
├── timer/
│   ├── StudyTimerService.kt        # フォアグラウンドサービス / 通知
│   └── BootReceiver.kt             # 再起動後の計測復元
├── ui/
│   ├── StudyViewModel.kt           # 残り時間・今日の合計の算出
│   ├── Study810Root.kt             # タブ / 通知権限
│   ├── TimerScreen.kt              # 背景画像 + カウントダウン
│   ├── CalendarScreen.kt           # 月カレンダー + 日別詳細
│   └── Theme.kt                    # ダークテーマ
└── util/
    ├── TimeUtils.kt                # 時間整形・日別分割
    └── ...
```

## バージョン

| 項目 | バージョン |
|---|---|
| Kotlin | 2.2.10 |
| Android Gradle Plugin | 8.13.2 |
| Gradle | 9.4.1 |
| compileSdk / targetSdk | 36 |
| minSdk | 26 |
| Jetpack Compose BOM | 2026.03.01 (Compose 1.10.6) |
| Material3 | 1.4.0 |
| Room | 2.8.4 |
| DataStore | 1.2.1 |

# Buzzebees Services Plugins

## Overview
`Buzzebees Services Plugins` is a Gradle Plugin designed to generate an XML file by reading values from `buzzebees-service.json` and registering the generated XML as a resource in an Android Project.

## Installation

### 1. Add the Plugin to `build.gradle.kts` (Project-Level)
Modify your **root `build.gradle.kts`** (or `build.gradle` for Groovy DSL) to include the plugin:

```kotlin
plugins {
    id("com.buzzebees.sdk.services") version "1.0.1" apply false
}
```

### 2. Apply the Plugin in `build.gradle.kts` (Module-Level)
In your **app or library module** (`app/build.gradle.kts` or `library/build.gradle.kts`):

```kotlin
plugins {
    id("com.buzzebees.sdk.services")
}
```

## Usage

### 1. Add `buzzebees-service.json` to the Project
Create a `buzzebees-service.json` file and include the required configuration values:
```json
{
  "AppId": "2952697274802274",
  "BaseUrl": "https://apigateway.buzzebees-dev.com/api",
  "BlobUrl": "https://devstoragebuzzebees.blob.core.windows.net",
  "CdnUrl": "https://cdn.buzzebees-dev.com/th",
  "IsDebugMode": false,
  "ModuleShoppingUrl": "https://shoppingcartmodule47.buzzebees-dev.com",
  "ModuleUrl": "https://buzzcrmplusmodule.buzzebees-dev.com",
  "PrefixClientVersion": "android_buzzebeesdemo",
  "SponsorId": "142985",
  "SubscriptionKey": "89c1d9bafb65486aa02606f63cb86b5c",
  "TimeoutInterval": 50,
  "UrlSchemesMainProject": "beesbenefit",
  "WebMisc": "",
  "WebShoppingUrl": "https://shoppingcartproduct.buzzebees-dev.com/landing/buzzebeesdemo",
  "WebURL": "",
  "EnvironmentName": "development",
  "TokenHeaderType": "Bearer",
  "AppName": "Buzzebees Demo",
  "Theme": "light"
}
```

### 2. Run the Build
When running `./gradlew assembleDebug`, the plugin will generate a `values.xml` file in the `generated/res/` directory and automatically register it with the project's resources.

## Generated XML Output
After building the project, the generated XML file will be located at:
```
app/build/generated/res/process{VariantName}BuzzebeesSDK/values/values.xml
```
Example content of the generated `values.xml`:
```xml
<resources>
    <string name="app_id" translatable="false">2952697274802274</string>
    <string name="base_url" translatable="false">https://apigateway.buzzebees-dev.com/api</string>
    <string name="blob_url" translatable="false">https://devstoragebuzzebees.blob.core.windows.net</string>
    <string name="cdn_url" translatable="false">https://cdn.buzzebees-dev.com/th</string>
    <bool name="is_debug_mode">false</bool>
    <string name="module_shopping_url" translatable="false">https://shoppingcartmodule47.buzzebees-dev.com</string>
    <string name="module_url" translatable="false">https://buzzcrmplusmodule.buzzebees-dev.com</string>
    <string name="prefix_client_version" translatable="false">android_buzzebeesdemo</string>
    <string name="sponsor_id" translatable="false">142985</string>
    <string name="subscription_key" translatable="false">89c1d9bafb65486aa02606f63cb86b5c</string>
    <integer name="timeout_interval">50</integer>
    <string name="url_schemes_main_project" translatable="false">beesbenefit</string>
    <string name="web_misc" translatable="false"></string>
    <string name="web_shopping_url" translatable="false">https://shoppingcartproduct.buzzebees-dev.com/landing/buzzebeesdemo</string>
    <string name="web_url" translatable="false"></string>
    <string name="environment_name" translatable="false">development</string>
    <string name="token_header_type" translatable="false">Bearer</string>
    <string name="app_name" translatable="false">Buzzebees Demo</string>
    <string name="theme" translatable="false">light</string>
</resources>

```

## Debugging
- Ensure the `buzzebees-service.json` file is in the correct location.
- Check the Gradle Console log to verify if the XML file was successfully generated.
- Add `println` statements to debug the JSON values being read.

## Conclusion
`Buzzebees Services Plugins` simplifies configuration management in Android Projects by automatically reading JSON values and converting them into XML resources.


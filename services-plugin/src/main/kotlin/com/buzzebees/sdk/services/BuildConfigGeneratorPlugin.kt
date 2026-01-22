package com.buzzebees.sdk.services

import com.android.build.api.variant.AndroidComponentsExtension
import com.android.build.api.variant.Variant
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.json.JSONObject
import java.io.File
import java.util.Locale
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.transform.OutputKeys
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult

class BuildConfigGeneratorPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.plugins.withId("com.android.application") {
            configurePlugin(project)
        }
        project.plugins.withId("com.android.library") {
            configurePlugin(project)
        }
    }

    private fun configurePlugin(project: Project) {
        val androidComponents =
            project.extensions.findByType(AndroidComponentsExtension::class.java)
        androidComponents?.registerSourceType(SOURCE_TYPE)
        androidComponents?.onVariants { variant ->
            // 🔹 Generate XML file and register resources
            processVariant(variant, project)
        }
    }
    private fun String.capitalize(): String = replaceFirstChar {
        if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
    }

    private fun <T> processVariant(
        variant: T,
        project: Project
    ) where T : Variant {
        val variantName = variant.name.capitalize()
        val generatedDir = File(
            project.buildDir,
            "generated/res/process${variantName}BuzzebeesSDK"
        ).apply { mkdirs() }
        val valuesDir = File(
            generatedDir,
            "values"
        ).apply { mkdirs() }  // Create the "values" directory inside "generatedDir"

        val taskName = "process${variantName}BuzzebeesSDK"

        val generateXmlTask = project.tasks.register(taskName) {
            it.doLast {
                getConfigFile(variant, project.projectDir)?.let { configFile ->
                    val jsonObject = JSONObject(configFile.readText())
                    generateXmlFile(valuesDir, jsonObject)
                    println("✅ config.json found at: ${configFile.absolutePath}")
                } ?: println("❌ buzzebees-service.json NOT FOUND. Searched locations:")
            }
        }

        project.afterEvaluate {
            val android = project.extensions.findByName("android") as? com.android.build.gradle.BaseExtension
            val activeVariant = android?.let {
                when (it) {
                    is com.android.build.gradle.AppExtension -> it.applicationVariants.find { v ->
                        (variant.flavorName.isNullOrEmpty() || v.flavorName == variant.flavorName) && v.buildType.name == variant.buildType
                    }
                    is com.android.build.gradle.LibraryExtension -> it.libraryVariants.find { v ->
                        (variant.flavorName.isNullOrEmpty() || v.flavorName == variant.flavorName) && v.buildType.name == variant.buildType
                    }
                    else -> null
                }
            }

            if (activeVariant?.name.equals(variant.name, ignoreCase = true)) {
                println("✅ Registering generated resource directory for active variant: ${variant.name} -> $generatedDir")
                android?.sourceSets?.findByName(variant.name)?.res?.srcDirs(generatedDir)
            } else {
                println("⏩ Skipping registration for non-active variant: ${variant.name}")
            }
        }
        // Ensure task runs before `preBuild`
        project.tasks.named("preBuild").configure {
            it.dependsOn(generateXmlTask)
        }
    }

    private fun getConfigFile(variant: Variant, root: File): File? {
        return getJsonLocations(
            variant.buildType.orEmpty(),
            variant.productFlavors.map { it.second })
            .map { root.resolve(it) }
            .firstOrNull { it.exists() }
    }

    private fun generateXmlFile(generatedDir: File, jsonObject: JSONObject) {
        val xmlFile = File(generatedDir, "values.xml")
        val document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument()
        val root = document.createElement("resources").apply { document.appendChild(this) }

        fun addElement(name: String, value: Any) {
            val element = when (value) {
                is Boolean -> document.createElement("bool")
                is Int -> document.createElement("integer")
                is String -> document.createElement("string")
                else -> return
            }

            element.apply {
                setAttribute("name", name)
                setAttribute("translatable", "false")
                appendChild(document.createTextNode(value.toString()))
            }
            root.appendChild(element)
        }

        // Convert camelCase or PascalCase to snake_case
        fun String.toSnakeCase(): String {
            return this.replace(Regex("([a-z0-9])([A-Z])"), "$1_$2")
                .lowercase()
        }

        val fields = listOf(
            "AppId" to jsonObject.optString("AppId", ""),
            "BaseUrl" to jsonObject.optString("BaseUrl", ""),
            "BlobUrl" to jsonObject.optString("BlobUrl", ""),
            "CdnUrl" to jsonObject.optString("CdnUrl", ""),
            "IsDebugMode" to jsonObject.optBoolean("IsDebugMode", false),
            "ModuleShoppingUrl" to jsonObject.optString("ModuleShoppingUrl", ""),
            "ModuleUrl" to jsonObject.optString("ModuleUrl", ""),
            "PrefixClientVersion" to jsonObject.optString("PrefixClientVersion", ""),
            "SponsorId" to jsonObject.optString("SponsorId", ""),
            "SubscriptionKey" to jsonObject.optString("SubscriptionKey", ""),
            "TimeoutInterval" to jsonObject.optInt("TimeoutInterval", 60),
            "UrlSchemesMainProject" to jsonObject.optString("UrlSchemesMainProject", ""),
            "WebMisc" to jsonObject.optString("WebMisc", ""),
            "WebShoppingUrl" to jsonObject.optString("WebShoppingUrl", ""),
            "WebURL" to jsonObject.optString("WebURL", ""),
            "EnvironmentName" to jsonObject.optString("EnvironmentName", ""),
            "TokenHeaderType" to jsonObject.optString("TokenHeaderType", ""),
            "AppName" to jsonObject.optString("AppName", ""),
            "AppNameUrl" to jsonObject.optString("AppNameUrl", ""),
            "DisplayName" to jsonObject.optString("DisplayName", ""),
            "IsAutoUse" to jsonObject.optString("IsAutoUse", ""),
            "Theme" to jsonObject.optString("Theme", ""),
            "ApiVersion" to jsonObject.optString("ApiVersion", ""),
        )

        // Add all elements
        fields.forEach { (key, value) -> addElement(key.toSnakeCase(), value) }

        TransformerFactory.newInstance().newTransformer().apply {
            setOutputProperty(OutputKeys.INDENT, "yes")
            setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4")
            transform(DOMSource(document), StreamResult(xmlFile))
        }
        println("✅ XML file generated at: ${xmlFile.absolutePath}")
    }

    private fun getJsonLocations(buildType: String, flavorNames: List<String>): List<String> {
        val flavorPath = flavorNames.joinToString("") { it.capitalize() }
        val basePaths = listOf(
            "",
            "src/$flavorPath/$buildType",
            "src/$buildType/$flavorPath",
            "src/$flavorPath",
            "src/$buildType"
        )
        return (basePaths + flavorNames.runningFold("src") { acc, flavor -> "$acc/$flavor" }).distinct()
            .map { "$it/$JSON_FILE_NAME" }
    }

    companion object {
        private const val SOURCE_TYPE = "buzzebees-service"
        private const val JSON_FILE_NAME = "buzzebees-service.json"
    }
}
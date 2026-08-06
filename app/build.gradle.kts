android {
    namespace = "com.symdev.netswitch"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.symdev.netswitch"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
    }

    signingConfigs {
        create("release") {
            if (System.getenv("SIGNING_KEY") != null) {
                // CI: decode base64 key from environment
                val keyBytes = java.util.Base64.getDecoder().decode(System.getenv("SIGNING_KEY"))
                val keyFile = file("${project.buildDir}/release-key.jks")
                keyFile.parentFile.mkdirs()
                keyFile.writeBytes(keyBytes)
                storeFile = keyFile
                storePassword = System.getenv("STORE_PASSWORD")
                keyAlias = System.getenv("KEY_ALIAS")
                keyPassword = System.getenv("KEY_PASSWORD")
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            signingConfig = signingConfigs.findByName("release")
        }
    }

    buildFeatures {
        compose = true
    }
}

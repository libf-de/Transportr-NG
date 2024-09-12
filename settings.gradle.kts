rootProject.name = "Transportr"

pluginManagement {
    repositories {
        google()
        jcenter()
        mavenCentral()
        maven(url = "https://jitpack.io")
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        jcenter()
        mavenCentral()
        maven(url = "https://jitpack.io")
        maven(url = uri("../public-transport-enabler-ktx/build/repo"))
    }
}

include(":app")
// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    id("com.android.application") version "8.2.2" apply false
    id("org.jetbrains.kotlin.android") version "1.9.22" apply false
    id("com.google.devtools.ksp") version "1.9.22-1.0.17" apply false
    id("com.google.dagger.hilt.android") version "2.51.1" apply false
    id("com.google.gms.google-services") version "4.4.4" apply false
    id("org.jetbrains.kotlin.plugin.parcelize") version "1.9.22" apply false
    id("androidx.navigation.safeargs.kotlin") version "2.7.7" apply false
}

// Fix for "task 'testClasses' not found" error often triggered by IDE or external tools
allprojects {
    afterEvaluate {
        if (tasks.findByName("testClasses") == null) {
            tasks.register("testClasses") {
                group = "verification"
                description = "Compatibility task for 'testClasses'"
                // In Android projects, this usually corresponds to compileDebugUnitTestSources
                val compileTask = tasks.findByName("compileDebugUnitTestSources")
                    ?: tasks.findByName("compileDebugUnitTestJavaWithJavac")
                if (compileTask != null) {
                    dependsOn(compileTask)
                }
            }
        }
    }
}

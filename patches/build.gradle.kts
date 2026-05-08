group = "app.xob0t"

patches {
    about {
        name = "Avito Morphe Patches"
        description = "Avito patches for Morphe."
        source = "https://github.com/xob0t/avito-morphe-patches"
        author = "xob0t"
        contact = "https://github.com/xob0t/avito-morphe-patches/issues"
        website = "https://github.com/xob0t/avito-morphe-patches"
        license = "GPLv3"
    }
}

kotlin {
    compilerOptions {
        freeCompilerArgs.add("-Xcontext-parameters")
    }
}

dependencies {
    // Used by JsonGenerator.
    implementation(libs.gson)
}

tasks {
    register<JavaExec>("generatePatchesList") {
        description = "Build patch with patch list"

        dependsOn(build)

        classpath = sourceSets["main"].runtimeClasspath
        mainClass.set("app.morphe.util.PatchListGeneratorKt")
    }
    // Used by gradle-semantic-release-plugin.
    publish {
        dependsOn("generatePatchesList")
    }
}

kotlin {
    compilerOptions {
        freeCompilerArgs = listOf("-Xcontext-receivers")
    }
}

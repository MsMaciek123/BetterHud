import io.papermc.hangarpublishplugin.model.Platforms
import io.papermc.paperweight.tasks.RemapJar
import me.lucko.jarrelocator.JarRelocator
import me.lucko.jarrelocator.Relocation

buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        classpath("me.lucko:jar-relocator:1.7")
    }
}

plugins {
    `java-library`
    kotlin("jvm") version "2.0.20"
    id("io.github.goooler.shadow") version "8.1.8"
    id("io.papermc.paperweight.userdev") version "1.7.3" apply false
    id("xyz.jpenilla.run-paper") version "2.3.1"
    id("org.jetbrains.dokka") version "2.0.0-Beta"
    id("io.papermc.hangar-publish-plugin") version "0.1.2"
    id("fabric-loom") version "1.7-SNAPSHOT" apply false
    id("com.modrinth.minotaur") version "2.+"
}

val minecraft = project.properties["minecraft_version"]!!.toString()
val folia = "1.20.6" // TODO bumps version to 1.21.1
val adventure = "4.17.0"
val platform = "4.3.4"
val targetJavaVersion = 21
val velocity = "3.3.0"
val bStats = "3.1.0"

val supportedMinecraftVersions = listOf(
    //1.17
    "1.17",
    "1.17.1",
    //1.18
    "1.18",
    "1.18.1",
    "1.18.2",
    //1.19
    "1.19",
    "1.19.1",
    "1.19.2",
    "1.19.3",
    "1.19.4",
    //1.20
    "1.20",
    "1.20.1",
    "1.20.2",
    "1.20.3",
    "1.20.4",
    "1.20.5",
    "1.20.6",
    //1.21
    "1.21",
    "1.21.1"
)
val supportedVelocityVersions = listOf(
    "3.3"
)

val legacyNmsVersion = listOf(
    "v1_17_R1",
    "v1_18_R1",
    "v1_18_R2",
    "v1_19_R1",
    "v1_19_R2",
    "v1_19_R3",
    "v1_20_R1",
    "v1_20_R2",
    "v1_20_R3",
)
val currentNmsVersion = listOf(
    "v1_20_R4",
    "v1_21_R1"
)

val allNmsVersion = legacyNmsVersion + currentNmsVersion

allprojects {
    apply(plugin = "java")
    apply(plugin = "kotlin")
    apply(plugin = "org.jetbrains.dokka")

    group = "kr.toxicity.hud"
    version = "1.6" + (System.getenv("BUILD_NUMBER")?.let { ".$it" } ?: "")

    repositories {
        mavenCentral()
        maven("https://jitpack.io")
        maven("https://repo.papermc.io/repository/maven-public/")
        maven("https://nexus.phoenixdevt.fr/repository/maven-public/")
        maven("https://repo.opencollab.dev/main/")
        maven("https://repo.codemc.org/repository/maven-public/")
        maven("https://maven.fabricmc.net/")
    }

    dependencies {
        testImplementation("org.jetbrains.kotlin:kotlin-test")
    }

    tasks {
        test {
            useJUnitPlatform()
        }
        compileJava {
            options.compilerArgs.addAll(listOf("-source", "17", "-target", "17"))
            options.encoding = Charsets.UTF_8.name()
        }
        compileKotlin {
            compilerOptions {
                freeCompilerArgs.addAll(listOf("-jvm-target", "17"))
            }
        }
    }
    java {
        toolchain.vendor = JvmVendorSpec.ADOPTIUM
        toolchain.languageVersion = JavaLanguageVersion.of(targetJavaVersion)
    }
}

dokka {
    moduleName = "BetterHud docs"
    dokkaSourceSets.configureEach {
        displayName = project.name
    }
}

subprojects {
    dokka {
        val list = mutableListOf(project.name)
        var parent: Project? = project.parent
        do {
            parent?.let {
                list.add(it.name)
            }
            parent = parent?.parent
        } while (parent != null)
        moduleName = list.reversed().joinToString("/")
        dokkaSourceSets.configureEach {
            displayName = project.name
        }
    }
}


fun Project.bukkit() = also {
    it.dependencies {
        compileOnly("org.spigotmc:spigot-api:$minecraft-R0.1-SNAPSHOT")
        compileOnly("org.bstats:bstats-bukkit:$bStats")
        compileOnly(rootProject.fileTree("shaded"))
    }
}
fun Project.velocity() = also {
    it.dependencies {
        compileOnly("com.velocitypowered:velocity-api:$velocity-SNAPSHOT")
        compileOnly("com.velocitypowered:velocity-proxy:$velocity-SNAPSHOT")
        annotationProcessor("com.velocitypowered:velocity-api:$velocity-SNAPSHOT")
        compileOnly("io.netty:netty-all:4.1.112.Final")
        compileOnly("org.bstats:bstats-velocity:$bStats")
    }
}
fun Project.folia() = also {
    it.dependencies {
        compileOnly("dev.folia:folia-api:$folia-R0.1-SNAPSHOT")
    }
}
fun Project.adventure() = also {
    it.dependencies {
        compileOnly("net.kyori:adventure-api:$adventure")
        compileOnly("net.kyori:adventure-text-minimessage:$adventure")
        compileOnly("net.kyori:adventure-text-serializer-legacy:$adventure")
        compileOnly("net.kyori:adventure-text-serializer-gson:$adventure")
    }
}
fun Project.library() = also {
    it.dependencies {
        implementation("org.yaml:snakeyaml:2.2")
        implementation("com.google.code.gson:gson:2.11.0")
        implementation("net.objecthunter:exp4j:0.4.8")
        implementation(rootProject.fileTree("shaded"))
    }
}
fun Project.bukkitAudience() = also {
    it.dependencies {
        compileOnly("net.kyori:adventure-platform-bukkit:$platform")
    }
}
fun Project.legacy() = also {
    it.java {
        toolchain.languageVersion = JavaLanguageVersion.of(17)
    }
}
fun Project.dependency(any: Any) = also {
    it.dependencies.compileOnly(any)
}

val apiShare = project("api:standard-api").adventure().legacy()
val apiBukkit = project("api:bukkit-api").adventure().bukkit().dependency(apiShare).legacy()
val apiVelocity = project("api:velocity-api").velocity().dependency(apiShare).legacy()
val apiFabric = project("api:fabric-api").adventure().dependency(apiShare)

val api = listOf(
    apiShare,
    apiBukkit,
    apiVelocity,
    apiFabric
)

fun Project.api() = also {
    it.dependencies {
        api.forEach { p ->
            compileOnly(p)
        }
    }
}

val dist = project("dist").adventure().library().api()
val scheduler = project("scheduler")
val bedrock = project("bedrock")

legacyNmsVersion.map {
    project("nms:$it")
}.forEach {
    it.legacy()
}

allNmsVersion.forEach {
    project("nms:$it")
        .dependency(apiShare)
        .dependency(apiBukkit)
}

scheduler.project("standard").adventure().bukkit().api()
scheduler.project("folia").folia().api()

dist.dependencies {
    allNmsVersion.forEach {
        compileOnly(project(":nms:$it"))
    }
}

val bukkitBootstrap = project("bootstrap:bukkit")
    .adventure()
    .bukkit()
    .api()
    .dependency(dist)
    .bukkitAudience()
    .also {
        it.dependencies {
            scheduler.subprojects.forEach { p ->
                compileOnly(p)
            }
            bedrock.subprojects.forEach { p ->
                compileOnly(p)
            }
            allNmsVersion.forEach { p ->
                compileOnly(project(":nms:$p"))
            }
        }
    }
val velocityBootstrap = project("bootstrap:velocity").velocity().api().dependency(dist)
val fabricBootstrap = project("bootstrap:fabric").api().dependency(dist).adventure().also {
    it.apply(plugin = "io.github.goooler.shadow")
    it.apply(plugin = "fabric-loom")
}

val bootstrap = listOf(
    bukkitBootstrap,
    velocityBootstrap
)

project(":nms").subprojects.forEach {
    it.apply(plugin = "io.papermc.paperweight.userdev")
}

dependencies {
    api.forEach {
        implementation(it)
    }
    implementation(dist)
    implementation("org.bstats:bstats-bukkit:$bStats")
    implementation("org.bstats:bstats-velocity:$bStats")
}

val sourceJar by tasks.creating(Jar::class.java) {
    dependsOn(tasks.classes)
    fun getProjectSource(project: Project): Array<File> {
        return if (project.subprojects.isEmpty()) project.sourceSets.main.get().allSource.srcDirs.toTypedArray() else ArrayList<File>().apply {
            project.subprojects.forEach {
                addAll(getProjectSource(it))
            }
        }.toTypedArray()
    }
    archiveClassifier = "source"
    from(*getProjectSource(project))
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
}
val dokkaJar by tasks.creating(Jar::class.java) {
    dependsOn(tasks.dokkaGenerate)
    archiveClassifier = "dokka"
    from(layout.buildDirectory.dir("dokka/html").orNull?.asFile)
}
val fabricJar by tasks.creating(Jar::class.java) {
    archiveClassifier = "fabric+$minecraft"
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    from(zipTree(fabricBootstrap.tasks.named("remapJar").map {
        (it as org.gradle.jvm.tasks.Jar).archiveFile
    }))
    from(zipTree(tasks.shadowJar.map {
        it.archiveFile
    }))
    doLast {
        relocateAll()
    }
}
val pluginJar by tasks.creating(Jar::class.java) {
    from(zipTree(velocityBootstrap.tasks.jar.map {
        it.archiveFile
    }))
    from(zipTree(bukkitBootstrap.tasks.jar.map {
        it.archiveFile
    }))
    scheduler.subprojects.forEach {
        from(zipTree(it.tasks.jar.map { t ->
            t.archiveFile
        }))
    }
    bedrock.subprojects.forEach {
        from(zipTree(it.tasks.jar.map { t ->
            t.archiveFile
        }))
    }
    allNmsVersion.forEach {
        from(zipTree(project("nms:$it").tasks.named("reobfJar").map { t ->
            (t as RemapJar).outputJar
        }))
    }
    from(zipTree(tasks.shadowJar.map {
        it.archiveFile
    }))
    manifest {
        attributes["paperweight-mappings-namespace"] = "spigot"
    }
    doLast {
        relocateAll()
    }
}

fun Jar.relocateAll() {
    val file = archiveFile.get().asFile
    val tempFile = file.copyTo(File.createTempFile("jar-relocator", System.currentTimeMillis().toString()).apply {
        if (exists()) delete()
    })
    JarRelocator(
        tempFile,
        file,
        listOf("kotlin","net.objecthunter.exp4j","org.bstats","org.yaml.snakeyaml").map {
            Relocation(it, "${project.group}.shaded.$it")
        }
    ).run()
    tempFile.delete()
}

runPaper {
    disablePluginJarDetection()
}

dependencies {
    fun searchAll(target: Project) {
        val sub = target.subprojects
        if (sub.isNotEmpty()) sub.forEach {
            searchAll(it)
        } else dokka(target)
    }
    searchAll(rootProject)
}

tasks {
    runServer {
        version(minecraft)
        pluginJars(pluginJar.archiveFile)
        pluginJars(fileTree("plugins"))
    }
    build {
//        finalizedBy(sourceJar)
//        finalizedBy(dokkaJar)
        finalizedBy(pluginJar)
//        finalizedBy(fabricJar)
    }
    logLinkDokkaGeneratePublicationHtml {
        enabled = false
    }
}

tasks.modrinth {
    dependsOn(
        tasks.shadowJar,
        sourceJar,
        dokkaJar,
        pluginJar,
        fabricJar,
        tasks.modrinthSyncBody
    )
}

hangarPublish {
    publications.register("plugin") {
        version = project.version as String
        channel = "Snapshot"
        id = "BetterHud"
        apiKey = System.getenv("HANGAR_API_TOKEN")
        changelog = System.getenv("COMMIT_MESSAGE")
        platforms {
            register(Platforms.PAPER) {
                jar = file("build/libs/${project.name}-${project.version}.jar")
                platformVersions = supportedMinecraftVersions
            }
            register(Platforms.VELOCITY) {
                jar = file("build/libs/${project.name}-${project.version}.jar")
                platformVersions = supportedVelocityVersions
            }
        }
    }
}

modrinth {
    token = System.getenv("MODRINTH_API_TOKEN")
    projectId = "betterhud2"
    versionType = "alpha"
    changelog = System.getenv("COMMIT_MESSAGE")
    versionNumber = project.version as String
    uploadFile.set(file("build/libs/${project.name}-${project.version}.jar"))
    additionalFiles = listOf(
        file("build/libs/${project.name}-${project.version}-dokka.jar"),
        file("build/libs/${project.name}-${project.version}-source.jar"),
        file("build/libs/${project.name}-${project.version}-fabric+$minecraft.jar")
    )
    gameVersions = supportedMinecraftVersions
    loaders = listOf("bukkit", "spigot", "paper", "purpur", "folia", "velocity")
    syncBodyFrom = rootProject.file("README.md").readText()
}
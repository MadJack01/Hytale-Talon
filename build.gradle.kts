plugins {
    id("java")
    id("com.azuredoom.hytale-tools") version "1.0.28"
}

group = "com.Light06"
version = "1.0.0"

repositories {
    mavenCentral()
}

hytaleTools {
    hytaleVersion.set("2026.+")
    manifestGroup.set("com.Light06")
    modCredits.set("Light06")
    patchline.set("release")
    modId.set("talon")
    mainClass.set("com.Light06.TalonPlugin")
    modDescription.set("Talon from league of legends!")
    includesPack.set(true)

}
dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
}
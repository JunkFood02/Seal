sealed class Version(val major: Int, val minor: Int, val patch: Int, val build: Int = 0) {
    abstract val name: String
    abstract val code: Long

    class Alpha(versionMajor: Int, versionMinor: Int, versionPatch: Int, versionBuild: Int) :
        Version(versionMajor, versionMinor, versionPatch, versionBuild) {
        override val name: String
            get() = "${major}.${minor}.${patch}-alpha.$build"

        override val code: Long
            get() = major * MAJOR + minor * MINOR + patch * PATCH + build * BUILD + ALPHA
    }

    class Beta(versionMajor: Int, versionMinor: Int, versionPatch: Int, versionBuild: Int) :
        Version(versionMajor, versionMinor, versionPatch, versionBuild) {
        override val name: String
            get() = "${major}.${minor}.${patch}-beta.$build"

        override val code: Long
            get() = major * MAJOR + minor * MINOR + patch * PATCH + build * BUILD + BETA
    }

    class Stable(versionMajor: Int, versionMinor: Int, versionPatch: Int) :
        Version(versionMajor, versionMinor, versionPatch) {
        override val name: String
            get() = "${major}.${minor}.${patch}"

        override val code: Long
            get() = major * MAJOR + minor * MINOR + patch * PATCH + build * BUILD + STABLE
    }

    class ReleaseCandidate(
        versionMajor: Int,
        versionMinor: Int,
        versionPatch: Int,
        versionBuild: Int,
    ) : Version(versionMajor, versionMinor, versionPatch, versionBuild) {
        override val name: String
            get() = "${major}.${minor}.${patch}-rc.$build"

        override val code: Long
            get() =
                major * MAJOR + minor * MINOR + patch * PATCH + build * BUILD + RELEASE_CANDIDATE
    }
}

// private const val ABI = 1L
private const val BUILD = 10L
private const val VARIANT = 100L
private const val PATCH = 10_000L
private const val MINOR = 1_000_000L
private const val MAJOR = 100_000_000L

private const val STABLE = VARIANT * 4
private const val ALPHA = VARIANT * 1
private const val BETA = VARIANT * 2
private const val RELEASE_CANDIDATE = VARIANT * 3

val currentVersion: Version =
    Version.Alpha(versionMajor = 2, versionMinor = 0, versionPatch = 0, versionBuild = 5)

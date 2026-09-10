# Publishing

The Vanniktech Maven Publish plugin configures all three public artifacts:

- `io.github.rock3r:compose-motion-panels`
- `io.github.rock3r:compose-motion-panels-jewel`
- `io.github.rock3r:compose-motion-panels-jewel-standalone`

Android and desktop target variants are emitted automatically for the multiplatform core. Every
publication includes Maven Central POM metadata, sources, Javadoc, checksums, and signatures.

## One-time setup

1. Register and verify the `io.github.rock3r` namespace in the
   [Maven Central Portal](https://central.sonatype.com/).
2. Generate a Central Portal user token and a GPG signing key whose public key is available from a
   public keyserver.
3. Add these GitHub Actions repository secrets:
   - `MAVEN_CENTRAL_USERNAME`
   - `MAVEN_CENTRAL_PASSWORD`
   - `SIGNING_IN_MEMORY_KEY` (the ASCII-armored private key)
   - `SIGNING_IN_MEMORY_KEY_ID` (optional)
   - `SIGNING_IN_MEMORY_KEY_PASSWORD` (optional for an unencrypted key)

Never commit these values. For a local release, use the equivalent Gradle environment variables:

```shell
export ORG_GRADLE_PROJECT_mavenCentralUsername=...
export ORG_GRADLE_PROJECT_mavenCentralPassword=...
export ORG_GRADLE_PROJECT_signingInMemoryKey="$(gpg --export-secret-keys --armor KEY_ID)"
export ORG_GRADLE_PROJECT_signingInMemoryKeyId=...
export ORG_GRADLE_PROJECT_signingInMemoryKeyPassword=...
```

## Validate locally

This writes all publications to `~/.m2/repository` without touching Maven Central:

```shell
./gradlew clean check publishToMavenLocal
```

Inspect the generated POMs under each module's `build/publications` directory before the first
release.

## Release

Push a tag matching the version, such as `v0.1.0`, or run the **Publish to Maven Central** workflow
manually with `0.1.0`. The workflow passes the version to Gradle and runs
`publishAndReleaseToMavenCentral`, which uploads, validates, and releases the deployment.

For a deliberately manual Central Portal release, run:

```shell
./gradlew -PVERSION_NAME=0.1.0 publishToMavenCentral
```

Then review and publish the validated deployment in the Central Portal.

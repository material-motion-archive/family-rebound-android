# Material Motion Rebound Family

[![Build Status](https://travis-ci.org/material-motion/material-motion-family-rebound-android.svg?branch=develop)](https://travis-ci.org/material-motion/material-motion-family-rebound-android)
[![codecov](https://codecov.io/gh/material-motion/material-motion-family-rebound-android/branch/develop/graph/badge.svg)](https://codecov.io/gh/material-motion/material-motion-family-rebound-android)

The Material Motion Rebound Family repo.

## Installation

### Installation with Jitpack

Use Jitpack to depend on any of our [public releases](https://github.com/material-motion/material-motion-family-rebound-android/releases).

Add the Jitpack repository to your project's `build.gradle`:

```gradle
allprojects {
    repositories {
        maven { url "https://jitpack.io" }
    }
}
```

When starting out with a project it is likely that you will want to use the
latest version of the library. Add the dependency to your module's
`build.gradle`:

```gradle
dependencies {
    compile 'com.github.material-motion:material-motion-family-rebound-android:+'
}
```

Later on in the project you may want to freeze to a specific version of the
library. This is **highly recommended** because it makes your builds predictable
and reproducible. Take care to occasionally [check for updates](https://github.com/ben-manes/gradle-versions-plugin).

```gradle
dependencies {
    compile 'com.github.material-motion:material-motion-family-rebound-android:1.0.0'
}
```

It is also possible to specify a *dynamic version* range. This is useful to stay
up to date on a major version, without the risk of new library releases
introducing breaking changes into your project.

```gradle
dependencies {
    compile 'com.github.material-motion:material-motion-family-rebound-android:1.+'
}
```

For more information regarding versioning, see:

- [Gradle Documentation on Dynamic Versions](https://docs.gradle.org/current/userguide/dependency_management.html#sub:dynamic_versions_and_changing_modules)
- [Material Motion Versioning Policies](https://material-motion.gitbooks.io/material-motion-team/content/essentials/core_team_contributors/release_process.html#versioning)

### Using the files from a folder local to the machine

You can have a copy of this library with local changes and test it in tandem
with its client project. To add a local dependency on this library, add this
library's identifier to your project's `local.dependencies`:

```
com.github.material-motion:material-motion-family-rebound-android
```

> Because `local.dependencies` is never to be checked into Version Control
Systems, you must also ensure that any local dependencies are also defined in
`build.gradle` as explained in the previous section.

**Important**

For each local dependency listed, you *must* run `gradle install` from its
project root every time you make a change to it. That command will publish your
latest changes to the local maven repository. If your local dependencies have
local dependencies of their own, you must `gradle install` them as well. See
[Issue #16](https://github.com/material-motion/material-motion-runtime-android/issues/16).

You must `gradle clean` your project every time you add or remove a local
dependency.

### Usage

How to use the library in your project.

#### Editing the library in Android Studio

Open Android Studio,
choose `File > New > Import`,
choose the root `build.gradle` file.

## Example apps/unit tests

Check out a local copy of the repo to access the Catalog application by running the following
commands:

    git clone https://github.com/material-motion/material-motion-family-rebound-android.git
    cd material-motion-family-rebound-android
    gradle installDebug

## Guides

1. [Architecture](#architecture)
2. [How to ...](#how-to-...)

### Architecture

https://github.com/material-motion/material-motion-family-rebound-android/issues/1

### How to ...

https://github.com/material-motion/material-motion-family-rebound-android/issues/1

## Contributing

We welcome contributions!

Check out our [upcoming milestones](https://github.com/material-motion/material-motion-family-rebound-android/milestones).

Learn more about [our team](https://material-motion.gitbooks.io/material-motion-team/content/),
[our community](https://material-motion.gitbooks.io/material-motion-team/content/community/),
and our [contributor essentials](https://material-motion.gitbooks.io/material-motion-team/content/essentials/).

## License

Licensed under the Apache 2.0 license. See LICENSE for details.

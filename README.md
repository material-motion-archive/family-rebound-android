# Material Motion Rebound Family

[![Build Status](https://travis-ci.org/material-motion/material-motion-family-rebound-android.svg?branch=develop)](https://travis-ci.org/material-motion/material-motion-family-rebound-android)
[![codecov](https://codecov.io/gh/material-motion/material-motion-family-rebound-android/branch/develop/graph/badge.svg)](https://codecov.io/gh/material-motion/material-motion-family-rebound-android)

The Rebound Material Motion family provides a bridge between
[Facebook's Rebound library](https://github.com/facebook/rebound) and the
[Material Motion runtime](https://github.com/material-motion/material-motion-runtime-android).

## Features

`SpringTo` uses Rebound springs to animate properties using spring physics driven on the main thread of
the application.

For example, you might use a SpringTo plan to scale a view:

```java
SpringTo<Float> scaleTo = new SpringTo<>(ReboundProperty.SCALE, 0.5f);
scheduler.addPlan(scaleTo, view);
```

SpringTo supports the properties included in the ReboundProperty class.

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

To build the sample application, run the following commands:

    git clone https://github.com/material-motion/material-motion-family-rebound-android.git
    cd material-motion-family-rebound-android
    gradle installDebug

To run all unit tests, run the following commands:

    git clone https://github.com/material-motion/material-motion-family-rebound-android.git
    cd material-motion-family-rebound-android
    gradle test

To run all integration tests, run the following commands:

    git clone https://github.com/material-motion/material-motion-family-rebound-android.git
    cd material-motion-family-rebound-android
    gradle connectedAndroidTest

## Guides

1. [How to animate a property with a SpringTo plan](#how-to-animate-a-property-with-a-springto-plan)
2. [How to configure spring behavior](#how-to-configure-spring-behavior)

### How to animate a property with a SpringTo plan

```java
SpringTo<Float> scaleTo = new SpringTo<>(ReboundProperty.SCALE, 0.5f);
scheduler.addPlan(scaleTo, view);
```

### How to configure spring behavior

A spring's behavior can be configured by setting a `SpringConfig` object on the SpringTo
instance.

```java
scaleTo.configuration = new SpringConfig(SpringTo.DEFAULT_TENSION, SpringTo.DEFAULT_FRICTION);
```

https://github.com/material-motion/material-motion-family-rebound-android/issues/1

## Contributing

We welcome contributions!

Check out our [upcoming milestones](https://github.com/material-motion/material-motion-family-rebound-android/milestones).

Learn more about [our team](https://material-motion.gitbooks.io/material-motion-team/content/),
[our community](https://material-motion.gitbooks.io/material-motion-team/content/community/),
and our [contributor essentials](https://material-motion.gitbooks.io/material-motion-team/content/essentials/).

## License

Licensed under the Apache 2.0 license. See LICENSE for details.

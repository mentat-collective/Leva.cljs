# Changelog

## [unreleased]

- #12 fixes #10 by turning `SubPanel` into a wrapper that properly calls (with
  `:f>`) a new `SubPanel*` function component.

## [0.2.1]

- #6 bumps the `clerk-utils` version to 0.4.1 and bumps the template sha for
  clerk-utils.

## [0.2.0]

- #5:

  - Adds a `deps-new` template that sets up a basic Clerk project with
    `Leva.cljs` installed as a dependency. [The template lives
    here](https://github.com/mentat-collective/Leva.cljs/tree/main/resources/leva/clerk).

  - Adds a `provided` dependency on SCI to `pom.xml`, so that cljdoc builds
    succeed.

  - Adds `leva.sci` with SCI namespace objects for all namespaces, plus a
    `namespaces` map and a `config` for easy installation into SCI. The
    namespaces follows the patterns set by the
    https://github.com/babashka/sci.configs repo.

    - `leva.sci/install!` allows the user to install `Leva.cljs` into SCI's
      shared context with one mutating call.

  - Migrates the project over to `clerk-utils` and all of its new custom build
    stuff. This let me simplify development, update the README and DEVELOPING
    pages and kill my shadow-cljs.edn file. `user.clj` gets quite a bit simpler
    too.

  - Adds notes to the interactive docs guide about using the library with SCI
    and with Clerk

  - Upgrades to Clerk version `fad499407d979916d21b33cc7e46e73f7a485e37` for the
    template and project docs notebook

## 0.1.0

First real release! Leva.cljs has compatibility with almost everything in Leva,
as explored in the interactive documentation notebook at https://leva.mentat.org.

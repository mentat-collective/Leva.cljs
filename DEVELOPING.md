## Dev Dependencies

`clj`, `shadow-cljs`, `node` and `babashka`.

## Github Pages, Docs Notebook

The project's [Github Pages site](https://leva.mentat.org) hosts an
interactive [Clerk](https://github.com/nextjournal/clerk) notebook demonstrating
the library's use.

### Local Notebook Dev

To start a shadow-cljs process watcher for the JS required to run the Clerk
notebook, run

```
bb dev-notebook
```

Then start a Clojure process however you like, and run `(user/start!)` to run
the Clerk server. This command should open up `localhost:7777`.

### Github Pages Static Build

To test the Pages build locally:

```
bb publish-local
```

This will generate the static site in `public`, start a development http server
and open up a browser window (http://127.0.0.1:8080/) with the production build
of the documentation notebook.

### Pages Build

To build and release to Github Pages:

```
bb release-gh-pages
```

This will ship the site to https://leva.mentat.org.

## Publishing to Clojars

The template for the project's `pom.xml` lives at
[`template/pom.xml`](https://github.com/mentat-collective/leva.cljs/blob/main/template/pom.xml).

To create a new release:

- Update the version in
  [build.clj](https://github.com/mentat-collective/leva.cljs/blob/main/build.clj)
- Make a new [Github
  Release](https://github.com/mentat-collective/leva.cljs/releases) with tag
  `v<the-new-version>`.

Submitting the release will create the new tag and trigger the following
command:

```
bb release
```

The new release will appear on Clojars.

## Linting

Code is linted with `clj-kondo`. [Install
`clj-kondo`](https://github.com/clj-kondo/clj-kondo/blob/master/doc/install.md)
then run

```
bb lint
```

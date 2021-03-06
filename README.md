# cljs-live

CLJS Live includes a dependency bundler for self-hosted ClojureScript, and helper utilities for using the bundles with the compiler.

ALPHA

### Example

https://cljs-live.firebaseapp.com

### Requirements:

- [Planck REPL](planck-repl.org)
- Clojure
- Alembic (in `~/.lein/profiles.clj`)


### Warning

This code depends on implementation details of specific versions of ClojureScript and Planck and is not for the faint of heart.

### Goal

Given a map containing `ns`-style requirement expressions (`:require, :require-macros, :import`), and the entry namespace of the compiled ClojureScript app (`:provided`), **cljs-live** should calculate and bundle the minimal set of compiled source files and analysis caches, and provide a utility to read these bundles into the compiler state.

### Usage

- Put a symlink to bundle.sh on your path.
- In your project directory, create a `live-deps.clj` file. See the [example file](https://github.com/mhuebert/cljs-live/blob/master/live-deps.clj) for options.

```clj
{:output-dir     "resources/public/js/cljs_live_cache.js" ;; where to save the output file
 :cljsbuild-out  "resources/public/js/compiled/out"} ;; the `output-dir` of your cljsbuild options
 :source-paths   ["src"]
 :bundles        [{;; same behaviour as `ns` forms:
                   :require        [app.repl-user :include-macros true]
                   :require-macros [] ;; same as above
                   :import         [] ;; same as above

                   ;; other keys:
                   :provided       [app.core] ;; entry namespace(s) to the _compiled_ app
                   :dependencies   [[quil "2.5.0"]] ;; deps to add to classpath
}]
```

**Note the `:cljsbuild-out` key.**
- This should correspond to the `:output-dir` in the compiler options for your build.
- Make sure that compiler options include `:cache-analysis true` (see the [example cljsbuild options](https://github.com/mhuebert/cljs-live/blob/master/script/build_example.clj)).
- Make sure that you have run a build and left this `out` folder intact before running this script.

**Run `bundle.sh live-deps.clj`.**
- This should write a bundle of javascript source files and analysis caches to the `:output-to` path, which you can include on a webpage.
- Use the `load-fn` in `cljs-live.compiler` to read from the bundle.

## Modifying the bundle

If you aren't happy with the calculated dependencies, you can manually require or exclude specific namespaces from a bundle by using the following keys:

```clj
{:require-source      []
 :require-cache       []
 :require-goog        []
 :require-foreign-lib []

 :exclude-source      []
 :exclude-cache       []
 :exclude-goog        []
 :exclude-foreign-lib []}
```

The `cljs-live/compiler` namespace contains a `load-fn` that knows how to read from the resulting bundle.

### Notes

- Not all macros work in the self-host environment. Mike Fikes, creator of [Planck,](planck-repl.org) is an expert on the topic, so check out his blog! Eg: [ClojureScript Macro Tower and Loop](http://blog.fikesfarm.com/posts/2015-12-18-clojurescript-macro-tower-and-loop.html), [Collapsing Macro Tower](http://blog.fikesfarm.com/posts/2016-03-04-collapsing-macro-tower.html)

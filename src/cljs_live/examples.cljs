(ns cljs-live.examples
  (:require
    [npm.marked]
    [cljsjs.react.dom]
    [re-view-hiccup.core :as hiccup]
    [cljs-live.compiler :as c]
    [cljs-live.eval :as e]))

(enable-console-print!)

(def examples (atom []))

(defn md [s]
  (hiccup/element [:div {:dangerouslySetInnerHTML {:__html (js/marked s)}}]))

(defn render-examples []
  (hiccup/element [:div.mw7.center.lh-copy.mb4
                   [:.f1.mt5.mb2.tc "cljs-live"
                    [:canvas#quil-canvas.v-mid.ml4.br-100]]
                   [:p.center.f3.mb3.tc.i "Bundling dependencies for the self-hosted ClojureScript compiler"]
                   [:.absolute.top-0.right-0.pa2
                    [:a.black.dib.ph3.pv2.bg-near-white.br1 {:href "https://www.github.com/mhuebert/cljs-live"} "Source on GitHub"]]
                   (md "Given the following `live-deps.clj` file, we run `./bundle.sh live-deps` to generate a cache of dependencies, which is included in this page. In the examples below, our self-hosted ClojureScript compiler reads from this cache while evaluating `(ns..)` and `(require..)` expressions.

**live-deps.clj**
```
{:cljsbuild-out \"resources/public/js/compiled/out\"
 :output-dir    \"resources/public/js/compiled\"
 :bundles       [{:name          cljs-live.user
                  :require       [npm.marked
                                  cljsjs.bcrypt
                                  goog.events
                                  [quil.core :include-macros true]]
                  :require-cache [cljs-live.sablono]
                  :provided      [cljs-live.examples]}
                 {:name cljs.core
                  :require-caches [cljs.core cljs.core$macros]}]
```

")
                   [:.f2.mt4.mb2.tc "examples"]
                   [:p.tc.mb3 "(hit command-enter in a textarea to re-evaluate)"]
                   (for [{:keys [render]} @examples]
                     (render))]))

(defn render-root []
  (js/ReactDOM.render (render-examples) (js/document.getElementById "app")))

(defn example [label initial-source]
  (let [source (atom initial-source)
        value (atom)
        eval (fn [] (reset! value (try (e/eval-str @source)
                                       (catch js/Error e
                                         (.debug js/console e)
                                         (str e)))))
        render (fn [] (hiccup/element [:.cf.w-100.mb4.mt2
                                       [:.bg-light-gray.ph2.mb3.br1.tc
                                        (md label)]

                                       [:textarea.fl.w-50.pre-wrap.h4
                                        {:on-change   #(reset! source (.-value (.-currentTarget %)))
                                         :on-key-down #(when (and (= 13 (.-which %)) (.-metaKey %))
                                                         (eval))
                                         :value       @source}]
                                       [:.fl.w-50.pl4
                                        (let [{:keys [value error]} @value]
                                          (if error (do

                                                      (.error js/console "eval-error" error)
                                                      (throw error)
                                                      (str error))
                                                    (if (js/React.isValidElement value)
                                                      value
                                                      [:.dib.ma2.gray
                                                       (if value (str value) "nil")])))]]))]
    (eval)
    (add-watch source :src render-root)
    (add-watch value :val render-root)
    {:render render}))


(defn add-examples []
  (swap! examples conj
         (example "**quil**, a ClojureScript library from Clojars with macros and a foreign lib that depends on the browser environment:<br/>(renders next to the page title^^)"
                  "(require '[quil.core :as q :include-macros true])
  (def colors (atom [(rand-int 255) (rand-int 255) (rand-int 255) (rand-int 255)]))
  (def ellipse-args (atom [(rand-int 100) (rand-int 100)]))
  (defn rand-or [a b] (if (< (rand) 0.5) a b))
  (defn rand-shift [v]  (mapv (rand-or (partial + 5) (partial - 5)) v))
  (defn draw []
    (swap! colors rand-shift)
    (swap! ellipse-args rand-shift)
    (apply q/fill @colors)
    (apply q/ellipse (concat '(50 50) @ellipse-args)))
  (q/defsketch my-sketch-definition
    :host \"quil-canvas\"
    :draw draw
    :size [100 100])
\"(Renders logo at top of page)\"
  ")

         (example
           "**goog.events**, a Google Closure Library dependency:"
           "(require '[goog.events :as events])\n(events/listenOnce js/window \"mousedown\" #(prn :mouse-down))
(import '[goog.ui Zippy])\n (subs (str Zippy) 0 250)\n")

         (example "**bcrypt**, from [cljsjs](http://cljsjs.github.io):"
                  "(require '[cljsjs.bcrypt])\n(let [bcrypt js/dcodeIO.bcrypt]\n  (.genSaltSync bcrypt 10))\n")

         (example
           "**npm.marked**, a foreign lib defined in this project's `deps.cljs` file:"
           "(require 'npm.marked)
  (js/marked \"**Hello, _world!_**\")")

         (example
           "**re-view-hiccup.core**, a compiled namespace for which we've bundled the analysis cache:"
           "(require '[re-view-hiccup.core :refer [element]])
  (element
    [:div
      [:div {:style {:padding 20 :margin-bottom 10 :color \"black\" :background-color \"pink\"}} \"This is a div rendered from hiccup syntax.\"]])"))
  (render-root))

(c/load-bundles! ["/js/compiled/cljs.core.json"
                  "/js/compiled/goog.json"
                  "/js/compiled/cljs_live.user.json"]
                 #(do
                    (e/eval '(require '[cljs.core :include-macros true]))
                    (render-root)
                    (add-examples)))
(ns cljs-live.core
  (:require
    [sablono.core :refer-macros [html]]
    [cljs-live.user]
    [cljs-live.compiler :as compiler]))

(enable-console-print!)

(def examples (atom []))

(defn render-examples []
  (html [:div
         [:p "Press command-enter to eval"]
         (for [{:keys [render]} @examples]
           (render))]))

(defn render-root []
  (js/ReactDOM.render (render-examples) (js/document.getElementById "app")))

(defn example [initial-source]
  (let [source (atom initial-source)
        value (atom)
        eval (fn []  (reset! value (try (compiler/eval-str @source)
                                        (catch js/Error e
                                          (.debug js/console e)
                                          (str e)))))
        render (fn [] (html [:div

                             [:textarea {:on-change   #(reset! source (.-value (.-currentTarget %)))
                                         :on-key-down #(when (and (= 13 (.-which %)) (.-metaKey %))
                                                        (eval))
                                         :value       @source
                                         :style       {:width 300 :height 100 :display "inline-block"}}]
                             [:div {:style {:color "#aaa" :display "inline-block" :margin 20}}
                              (let [{:keys [value error]} @value]
                                (if error (str error)
                                          (if (fn? value)
                                            (value)
                                            (if value (str value)
                                                      "nil"))))]]))]
    (eval)
    (add-watch source :src render-root)
    (add-watch value :val render-root)
    {:render render}))

(compiler/preloads!)

(swap! examples conj

       ;; foreign lib copied from npm and defined in deps.cljs
       (example "(require '[npm.marked])\n\n(js/marked \"Hello from markdown\") ")

       ;; foreign lib from cljsjs
       (example "(require '[cljsjs.bcrypt])\n\n(let [bcrypt js/dcodeIO.bcrypt]\n  (.genSaltSync bcrypt 10))\n")

       ;; personal lib, uses macros and foreign libs
       (example "(ns cljs-live.user \n  (:require [firelisp.rules :refer-macros [at]]))\n\n(at \"/\" {:write true})")

       ;; goog deps
       (example "(ns cljs-live.user \n  (:require [goog.events :as events]))\n\n(events/listenOnce js/window \"mousedown\" #(prn :mouse-down))")

       ;; unable to load sablono.compiler in Planck, maybe a macro-loading dependency/order issue.
       #_(example "(require '[sablono.core :refer-macros [html]])")

       ;; unable to load Quil in Planck because of Processing.js browser dependencies.
       #_(example "(require '[quil.core :as q])")

       )

(render-root)
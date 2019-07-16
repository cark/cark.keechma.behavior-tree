(ns cark.keechma.behavior-tree.example.stylesheets.core
  (:require [garden-basscss.core :as core]
            [garden-basscss.vars :refer [vars]]
            [garden.core :as garden]
            [garden.units :refer [em rem px px-]]
            [garden.stylesheet :refer [at-media]]
            [keechma.toolbox.css.core :as toolbox-css]
            [cark.keechma.behavior-tree.example.stylesheets.colors :as colors] 
            [clojure.string :refer [split]])
  (:require-macros [garden.def :refer [defkeyframes]]))

(def system-font-stack
  "-apple-system, BlinkMacSystemFont, 'Segoe UI', 'Roboto', 'Oxygen', 'Ubuntu', 'Cantarell', 'Fira Sans', 'Droid Sans', 'Helvetica Neue', sans-serif")

(def system-font-stack-monospace
  "'Menlo', 'Monaco', 'Consolas', 'Lucida Console', 'Lucida Sans Typewriter', 'Andale Mono', 'Courier New', monospaced")

(defn stylesheet []
  [[:* {:box-sizing 'border-box}]
   [:html {:font-size "100%"
           :height "100%"}]
   [:body {:height "100%"
           :margin 0
           :font-family system-font-stack
           :text-rendering "optimizeLegibility"
           :-webkit-font-smoothing "antialiased"
           :-moz-osx-font-smoothing "grayscale"}]
   [:form {:width "100%"}]
   [:img {:max-width "100%"}]
   [:.monospaced {:font-family system-font-stack-monospace}]
   [:.cursor-pointer {:cursor 'pointer}] 
   [:.pill {:border-radius "999em"}]
   [:.w-100 {:width "100%"}]
   [:h3 {:margin-top "1em"
         :margin-bottom "0.5em"
         :font-weight "normal"
         :text-decoration "underline"}]
   [:p {:margin-top "0.5em"}]
   (core/stylesheet)
   (colors/stylesheet)
   @toolbox-css/component-styles
                                        ;[:html {:height "100%"}]
                                        ;[:body {:height "100%"}]
   [:#app {:height "100%"}]
   ])

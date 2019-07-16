(ns cark.keechma.behavior-tree.example.stylesheets.colors
  (:require [garden.color :as color]))

(defn make-color-variations [colors]
(reduce-kv (fn [m k v]
             (let [base-name (name k)]
               (assoc m
                      k v
                      (keyword (str base-name "-l")) (color/as-hex (color/lighten v 10))
                      (keyword (str base-name "-d")) (color/as-hex (color/darken v 10))))) {} colors))

(def colors {:black "#111111"
             :white "#ffffff"
             :turquoise "#1abc9c"
             :emerald "#2ecc71"
             :peterriver "#3498db"
             :amethyst "#9b59b6"
             :wetasphalt "#34495e"
             :greensea "#16a085"
             :nephritis "#27ae60"
             :belizehole "#2980b9"
             :wisteria "#8e44ad"
             :midnightblue "#2c3e50"
             :sunflower "#f1c40f"
             :carrot "#e67e22"
             :alizarin "#e74c3c"
             :clouds "#ecf0f1"
             :concrete "#95a5a6"
             :orange "#f39c12"
             :pumpkin "#d35400"
             :pomegranate "#c0392b"
             :silver "#bdc3c7"
             :asbestos "#7f8c8d"

             :primary "#2A4764"
             :success "#006B6F"
             :info "#3272B2"
             :warning "#EF8354"
             :danger "#E54C64"})

(def colors-with-variations (make-color-variations colors))

(defn transition [prop]
(str (name prop) " 0.10s ease-in-out"))

(defn gen-colors-styles [class-name prop]
(map (fn [[color-name val]]
       (let [color-name (name color-name)
             normal-class (str "." class-name "-" color-name)
             hover-class (str "." class-name "-h-" color-name)
             darken-val (color/darken val 10)
             lighten-val (color/lighten val 10)
             hover ":hover"]
         [[normal-class {prop val}]
          [(str normal-class "-d") {prop darken-val}]
          [(str normal-class "-l") {prop lighten-val}]
          [(str hover-class hover) {prop val}]
          [(str hover-class "-d" hover) {prop darken-val}]
          [(str hover-class "-l" hover) {prop lighten-val}]])) colors))

(defn stylesheet [] [[:.bg-transparent {:background 'transparent}]
                     (gen-colors-styles "bg" :background-color)
                     (gen-colors-styles "c" :color)
                     (gen-colors-styles "bd" :border-color)
                     [:.t-c {:transition (transition :color)}]
                     [:.t-bg {:transition (transition :background-color)}]
                     [:.t-bd {:transition (transition :border-color)}]])

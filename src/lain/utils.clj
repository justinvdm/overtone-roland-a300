(ns lain.utils
  (:require [clojure.string :refer [join]]
            [overtone.studio.inst :refer [inst]]
            [overtone.sc.synth :refer [synth-form]]
            [overtone.sc.ugens :refer [FREE
                                       env-gen]]
            [overtone.sc.defcgen :refer [defcgen]]
            [overtone.sc.sample :refer [load-sample]]
            [lain config]))

(defmacro temp-inst
  "Workaround to declare an inst with params without using definst
  https://github.com/overtone/overtone/issues/248"
  [inst-name & inst-form]
  {:arglists '([name doc-string? params ugen-form])}
  (let
    [[inst-name params ugen-form] (synth-form inst-name inst-form)
      inst-name (with-meta inst-name (merge (meta inst-name) {:type ::instrument}))]
    `(inst ~inst-name ~params ~ugen-form)))

(defn key-inst
  [wave
   env]
  (temp-inst
    _
    [freq 440
     velocity-f 1
     gate 1]
    (let
      [wave-val (wave :freq freq)
       env-val (env-gen env gate :action FREE)]
      (* velocity-f env-val wave-val))))

(defn key-note-inst
  [wave
   env]
  (temp-inst
    _
    [note 60
     velocity-f 1
     gate 1]
    (let
      [wave-val (wave :note note)
       env-val (env-gen env gate :action FREE)]
      (* velocity-f env-val wave-val)))) 

(defmacro deflcgen
  "Define a lightweight cgen"
  [cgen-name rate params & body]
  (let
    [params (partition 2 params)
     params (for [[p-name, p-val] params] [p-name {:default p-val}])
     params (vec (flatten params))]
    `(defcgen ~cgen-name "" ~params (~rate ~@body))))

(defn get-sample [path]
  (load-sample (join [lain.config/base-path "samples/" path])))
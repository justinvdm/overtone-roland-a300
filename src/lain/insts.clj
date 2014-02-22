(ns lain.insts
  (:require [overtone.sc.ugens :refer [FREE
                                       env-gen
                                       play-buf]]
            [lain.utils :refer [anon-inst]]))

(defn key-inst [wave env]
  (anon-inst
    [freq 440
     velocity-f 1
     gate 1]
    (let
      [wave-val (wave :freq freq)
       env-val (env-gen env gate :action FREE)]
      (* velocity-f env-val wave-val))))

(defn key-note-inst [wave env]
  (anon-inst
    [note 60
     velocity-f 1
     gate 1]
    (let
      [wave-val (wave :note note)
       env-val (env-gen env gate :action FREE)]
      (* velocity-f env-val wave-val)))) 


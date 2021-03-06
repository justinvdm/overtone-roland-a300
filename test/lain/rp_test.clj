(ns lain.rp-test
  (:require [speclj.core :refer :all]
            [overtone.libs.event :refer :all]
            [overtone.algo.scaling :refer :all]
            [overtone.sc.ugens :refer :all]
            [overtone.sc.synth :refer :all]
            [overtone.sc.bus :refer :all]
            [overtone.sc.node :refer :all]
            [lain.utils :refer [deflcgen]]
            [lain.sequencers :refer [metro]]
            [lain.mecha :as mecha]
            [lain.rp :refer :all]))


(defsynth syn-test-tick [bus 0
                         freq 4]
  (replace-out:ar bus (pulse-count:ar (impulse:ar freq))))


(describe "rp"
  (with-stubs)

  (after (sync-event :reset))

  (describe "rp"
    (it "should record and play"
      (let [in-bus (audio-bus)
            out-bus (audio-bus)
            m-out-bus (bus-monitor out-bus)
            t-rp (rp :in-bus in-bus
                     :out-bus out-bus
                     :looped false)
            input-node (syn-test-tick in-bus 4)]
        (rp-mode t-rp :rec)
        (Thread/sleep 1000)
        (rp-mode t-rp :play)

        ; no idea why 300 is working instead of 250 :/
        ; maybe the monitor needs to catch up?
        (Thread/sleep 300)
        (should= 1.0 (round-to @m-out-bus 1))

        (Thread/sleep 300)
        (should= 2.0 (round-to @m-out-bus 1))

        (Thread/sleep 300)
        (should= 3.0 (round-to @m-out-bus 1))

        (Thread/sleep 300)
        (should= 0.0 (round-to @m-out-bus 1))

        (Thread/sleep 300)
        (should= 0.0 (round-to @m-out-bus 1))

        (kill input-node)
        (free-bus in-bus)
        (free-bus out-bus)
        (mecha/stop t-rp)))

    (it "should support synced recording and playing"
      (let [in-bus (audio-bus)
            out-bus (audio-bus)
            sync-bus (control-bus)
            m-out-bus (bus-monitor out-bus)
            t-rp (rp :in-bus in-bus
                     :out-bus out-bus
                     :sync-bus sync-bus
                     :looped false)
            sync-node ((synth (out sync-bus (impulse 2))))
            input-node (syn-test-tick in-bus 4)]
        (rp-mode t-rp :rec)
        (Thread/sleep 1500)

        (rp-mode t-rp :play)
        (Thread/sleep 450)
        (should= 0.0 (round-to @m-out-bus 1))

        (Thread/sleep 100)
        (should= 2.0 (round-to @m-out-bus 1))

        (Thread/sleep 300)
        (should= 3.0 (round-to @m-out-bus 1))

        (Thread/sleep 300)
        (should= 4.0 (round-to @m-out-bus 1))

        (Thread/sleep 500)
        (should= 0.0 (round-to @m-out-bus 1))

        (Thread/sleep 500)
        (should= 0.0 (round-to @m-out-bus 1))

        (kill input-node)
        (free-bus in-bus)
        (free-bus out-bus)
        (free-bus sync-bus)
        (mecha/stop t-rp)))

    (it "should support looped playing"
      (let [in-bus (audio-bus)
            out-bus (audio-bus)
            sync-bus (control-bus)
            m-out-bus (bus-monitor out-bus)
            t-rp (rp :in-bus in-bus
                     :out-bus out-bus
                     :sync-bus sync-bus
                     :looped true)
            sync-node ((synth (out sync-bus (impulse 2))))
            input-node (syn-test-tick in-bus 4)]
        (rp-mode t-rp :rec)
        (Thread/sleep 1500)

        (rp-mode t-rp :play)
        (Thread/sleep 450)
        (should= 0.0 (round-to @m-out-bus 1))

        (Thread/sleep 100)
        (should= 2.0 (round-to @m-out-bus 1))

        (Thread/sleep 300)
        (should= 3.0 (round-to @m-out-bus 1))

        (Thread/sleep 300)
        (should= 4.0 (round-to @m-out-bus 1))

        (Thread/sleep 500)
        (should= 0.0 (round-to @m-out-bus 1))

        (Thread/sleep 500)
        (should= 2.0 (round-to @m-out-bus 1))

        (Thread/sleep 300)
        (should= 3.0 (round-to @m-out-bus 1))

        (Thread/sleep 300)
        (should= 4.0 (round-to @m-out-bus 1))

        (Thread/sleep 500)
        (should= 0.0 (round-to @m-out-bus 1))

        (Thread/sleep 300)
        (should= 2.0 (round-to @m-out-bus 1))

        (Thread/sleep 300)
        (should= 3.0 (round-to @m-out-bus 1))

        (Thread/sleep 300)
        (should= 4.0 (round-to @m-out-bus 1))

        (kill input-node)
        (free-bus in-bus)
        (free-bus out-bus)
        (free-bus sync-bus)
        (mecha/stop t-rp))))

(describe "a300-looper"
  (describe "when the down key is pressed in recording mode"
    (it "should start recording"
      (let [lp (a300-looper [:midi :l1 :down] [:midi :l2 :up])]
        (sync-event [:midi :l9 :down])

        (should-invoke
          rp-mode
          {:with [(:rp lp) :rec]
           :times 1}
          (sync-event [:midi :l1 :down]))

        (mecha/stop lp))))

  (describe "when the mode key is released in recording mode"
    (it "should start playing"
      (let [lp (a300-looper [:midi :l1 :down] [:midi :l2 :up])]
        (sync-event [:midi :l9 :down])
        (sync-event [:midi :l1 :down])

        (should-invoke
          rp-mode
          {:with [(:rp lp) :play]
           :times 1}
          (sync-event [:midi :l9 :up]))

        (mecha/stop lp))))

  (describe "when the up key is pressed in play mode"
    (it "should stop playing"
      (let [lp (a300-looper [:midi :l1 :down] [:midi :l1 :up])]
        (sync-event [:midi :l9 :down])
        (sync-event [:midi :l1 :down])

        (should-invoke
          mecha/stop
          {:with [(get-in lp [:rp :modes])]
           :times 1}
          (sync-event [:midi :l1 :up]))

        (mecha/stop lp))))

  (describe "when the down key is pressed in play mode"
    (it "should start playing"
      (let [lp (a300-looper [:midi :l1 :down] [:midi :l1 :up])]
        (sync-event [:midi :l9 :down])
        (sync-event [:midi :l1 :down])
        (sync-event [:midi :l1 :up])

        (should-invoke
          rp-mode
          {:with [(:rp lp) :play]
           :times 1}
          (sync-event [:midi :l1 :down]))

        (mecha/stop lp))))))

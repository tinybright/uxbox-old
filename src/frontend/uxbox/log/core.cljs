(ns uxbox.log.core
  (:require
   [datascript :as d]
   [uxbox.users.queries :as uq]))

(def event-types #{:uxbox/create-project
                   :uxbox/delete-project
                   :uxbox/create-page
                   :uxbox/change-page-title
                   :uxbox/delete-page
                   :uxbox/create-shape
                   :uxbox/delete-shape
                   :uxbox/change-shape
                   :uxbox/toggle-shape-visibility
                   :uxbox/toggle-shape-lock
                   ;; TODO
                   :uxbox/move-shape-up
                   :uxbox/move-shape-down})

;; to-datoms :: Event -> DB -> [Datom]
(defmulti to-datoms (fn [event db]
                      (:event/type event))
          :default :uxbox/no-op)

(defmethod to-datoms :uxbox/no-op [_ _] [])

;; Type -> Payload -> User -> Date -> Event
(defn event
  [type payload author timestamp]
  {:event/type type
   :event/timestamp timestamp
   :event/payload payload
   :event/author author})

;; needed to make possible to insert arbitrary maps in Datascript
(extend-protocol IComparable
  PersistentArrayMap
  (-compare [one other]
    (compare (vec one) (vec other)))

  PersistentHashMap
  (-compare [one other]
    (compare (vec one) (vec other))))

;; persist! :: Event -> Conn -> Conn
(defn persist!
  [conn ev]
  (let [ds (to-datoms ev @conn)]
    (d/transact! conn ds)))

(defn record!
  [conn type payload]
  (let [author (uq/pull-current-user @conn)
        ev (event type payload author (js/Date.))]
    (persist! conn ev)
    (d/transact! conn [ev])))

(ns ^:figwheel-always uxbox.core
    (:require rum
              [uxbox.db :as db]
              [uxbox.navigation :refer [start-history!]]
              [uxbox.keyboard :refer [start-keyboard!]]
              [uxbox.storage.core :refer [start-storage!]]
              [uxbox.dashboard.views :refer [dashboard]]
              [uxbox.workspace.views :refer [workspace]]
              [uxbox.forms :refer [lightbox]]
              [uxbox.user.views :refer [login]]
              [uxbox.icons-sets.core]
              [hodgepodge.core :refer [local-storage]]))

(enable-console-print!)

(rum/defc ui
  [db]
  (let [[page params] (:location @db)]
    (case page
      :dashboard [:div
                  (dashboard db)
                  (lightbox db)]
      :login (login db)
      :workspace (workspace db))))

(defn render!
  [app-state element]
  (let [component
        (rum/mount (ui app-state) element)]
    (add-watch app-state
               :render
               (fn [_ _ _ _]
                 (rum/request-render component)))))

(def $el (.getElementById js/document "app"))

(defn start!
  [app-state]
  (start-storage! local-storage)
  (start-history!)
  (start-keyboard!)
  (render! app-state $el))

(start! db/app-state)

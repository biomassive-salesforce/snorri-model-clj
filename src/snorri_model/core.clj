(ns snorri-model.core
  (:require [appengine-magic.core :as ae]
            [appengine-magic.services.user :as user]
            [snorri-model.middleware :as mw]
            [snorri-model.store :as store]
            [snorri-model.view :as view])
  (:use compojure.core
        ring.middleware.reload
        ring.util.response))

(defroutes snorri-model-app-handler
  (GET "/" []
    (view/layout
      (view/index (store/get-data))))
  (GET "/symbols/" []
    (if (not (and (user/user-logged-in?) (user/user-admin?)))
      (redirect "/")
      (view/layout 
        (view/symbols (store/get-symbols)))))
  (POST "/symbols/" [symbol]
    (if (not (and (user/user-logged-in?) (user/user-admin?)))
      (redirect "/")
      (do
        (store/create-symbol symbol)
        (redirect "/symbols/"))))
  (POST "/symbols/:symbol" [symbol]
    (if (not (and (user/user-logged-in?) (user/user-admin?)))
      (redirect "/")
      (do
        (store/delete-symbol symbol)
        (redirect "/symbols/"))))
  (ANY "/*" _
    {:status 404
     :headers {"Content-Type" "text/plain"}
     :body "not found"}))

(def interactive?
  (= :interactive (ae/appengine-environment-type)))

(def app
  (-> #'snorri-model-app-handler
    (mw/wrap-if interactive? wrap-reload '[snorri-model.core snorri-model.store
                                           snorri-model.view])))

(ae/def-appengine-app snorri-model-app #'app)

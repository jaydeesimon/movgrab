(defproject movgrab "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.bytedeco/javacv "1.1"]
                 [clj-pdf "2.2.1"]]
  :main ^:skip-aot movgrab.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})

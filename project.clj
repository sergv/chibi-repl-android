
(use '[clojure.java.io :only (file)])

(def project-key-def-file "project-key.clj")

(def key-def-info
  (let [f (file project-key-def-file)]
    (if (.exists f)
      (read-string (slurp project-key-def-file))
      nil)))


(defproject android/chibi-scheme-repl "0.1.0"
  :description "Chibi Scheme-based REPL for Android."
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :min-lein-version "2.0.0"

  :warn-on-reflection true

  :source-paths []
  :java-source-paths ["src" "gen"]
  :javac-options     ["-target" "1.6" "-source" "1.6"]
  :target-path "bin"
  :compile-path "bin/classes"
  :native-path "jni"
  :test-paths []
  :java-only true
  :omit-source false

  ;; :local-repo "/home/sergey/.m2/repository/"
  :repositories [["local" "file:///home/sergey/.m2/repository/"]]
  :plugins [[lein-droid "0.1.0"]]
  :dependencies []
  :android ~(merge
             {:sdk-path "/home/sergey/projects/android/android-sdk-linux"
              :external-classes-paths ["/home/sergey/projects/android/android-sdk-linux/extras/android/support/v4/android-support-v4.jar"]
              :dex-opts ["-JXmx4096M"]

              :proguard-conf-path "proguard.cfg"
              :force-dex-optimize true
              :libraty false
              :dex-aux-opts ["--num-threads=2"]
              ;; :gen-path "bin/gen"
              :native-libraries-paths ["libs"]

              :min-version "10"
              :target-version "15"}
             key-def-info))

;; Local Variables:
;; clojure-compile/lein-command: "DEBUG=1 LEIN_JAVA_CMD=/usr/lib/jvm/java-7-openjdk-amd64/jre/bin/java lein with-profiles release do droid code-gen, droid compile, droid create-dex, droid apk, droid install, droid run"
;; End:

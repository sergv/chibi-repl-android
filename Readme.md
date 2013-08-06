* Overview
This is Chibi Scheme REPL for Android based on [[http://code.google.com/p/scheme-droid/][scheme-droid]],
but with new Scheme engine, the [[http://synthcode.com/wiki/chibi-scheme][Chibi scheme]],
and somewhat (subjectively) enhanced ui.

* Features
+ Chibi Scheme is more complete Scheme implementation than JScheme
++ For example, Chibi supports full numeric tower while JScheme does not
++ But srfi-27, sources of random bits, is not supported yet on Android due to differences in BIONIC libc
+ Interaction history is a list of cells so that previous input may be copied and offending cells may be deleted by a long tap
+ Computations may be interrupted (todo)
+ Files may be loaded from sdcard using AndExplorer dialogue (no other means of selecting files provided yet)

* Build process
First libchibi-scheme.so along with it's modules must be built for Android architecture.
Follow steps [[???][here]].

No build native part a sample build_native.sh script is provided. It only requires
NDK_HOME environment variable to be properly configured.

Java part may be built with Leiningen with [[https://github.com/clojure-android/lein-droid][lein-droid]].
But standard Android approach with ant will work just as well, except that no
build.xml is provided by defalut so you'd have to generate it.
On my debian 7 wheezy I build java part like this

``lein with-profiles release do droid code-gen, droid compile, droid create-dex, droid apk, droid install, droid run``,

and sometimes I need no specify LEIN_JAVA_CMD environment variable,

``LEIN_JAVA_CMD=/usr/lib/jvm/java-7-openjdk-amd64/jre/bin/java lein with-profiles release do droid code-gen, droid compile, droid create-dex, droid apk, droid install, droid run``.



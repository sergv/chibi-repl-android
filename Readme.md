### Overview
This is Chibi Scheme REPL for Android based on [scheme-droid](http://code.google.com/p/scheme-droid/),
but with new Scheme engine, the [Chibi scheme](http://synthcode.com/wiki/chibi-scheme),
and somewhat (subjectively) enhanced ui.

### Features
* Chibi Scheme is more complete Scheme implementation than JScheme
    * For example, Chibi supports full numeric tower while JScheme does not
    * But srfi-27, sources of random bits, is not supported yet on Android due to differences in BIONIC libc
* Interaction history is a list of cells so that previous input may be copied and offending cells may be deleted by a long tap
* Computations may be interrupted (todo)
* Files may be loaded from sdcard using AndExplorer dialogue (no other means of selecting files provided yet)

### Build process
First libchibi-scheme.so along with it's modules must be built for Android architecture.
Use build\_android.sh script from branch android [here](https://github.com/sergv/chibi-scheme-android),
which probably will require modification for your environment
(note that it uses standalone NDK toolchain for building created with
$NDK\_HOME/build/tools/make-standalone-toolchain.sh).

Java part may be built using Leiningen with [lein-droid](https://github.com/clojure-android/lein-droid).
But standard Android approach with ant will work just as well, except that no
build.xml is provided by defalut so you'd have to generate it.
On my debian wheezy I build java part like this

``LEIN_JAVA_CMD=/usr/lib/jvm/java-7-openjdk-amd64/jre/bin/java lein with-profiles release do droid code-gen, droid compile, droid create-obfuscated-dex, droid apk, droid install, droid run``.


# Isabelle/Scala-js

Installation:
1. Build dependencies via `mvn package`
2. Add Isabelle component via `isabelle components -u <dir>`
3. Run with `isabelle scalajs` to compile [example Isabelle/Scala module](src/scalajs_example.scala)

The [index.html](index.html) file calls the example js function, serve via http server to view result
(e.g., in IntelliJ IDEA via 'Open In/Browser/...').
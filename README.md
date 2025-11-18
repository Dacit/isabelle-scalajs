# Isabelle/Scala-js

Installation:
1. Build dependencies via `mvn package`
2. Add Isabelle component via `isabelle components -u <dir>`
3. Run with `isabelle scalajs` to compile [example Isabelle/Scala module](src/scalajs_example.scala)

The [index.html](index.html) file calls the example js function.
Serve the directory via http server and open the file in a browser to see the result
(e.g., in IntelliJ IDEA simply via 'Open In/Browser/...' on the html file).
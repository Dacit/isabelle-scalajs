package isabelle


import java.nio.file.{Path => JPath}
import scala.jdk.CollectionConverters._


object Scalajs {
  val scalajs_home = Path.explode("$SCALAJS_HOME")
  val scalajs_library = scalajs_home + Path.explode("lib/scalajs-library_2.13-1.20.1.jar")

  def compile_ir(output_dir: Path, progress: Progress = new Progress): Unit = {
    val deps = Classpath(List(scalajs_library.file)).jars.map(_.toPath)

    val sources =
      for {
        source <- Scala_Build.context(Path.explode("$ISABELLE_HOME"), component = true).sources
        if source.implode.contains("/src/Pure/")
      } yield source.java_path
  
    progress.echo("Generating IR for " + sources.length + " sources ...")

    val flags = "-scalajs -scalajs-genStaticForwardersForNonTopLevelObjects"

    setup.Build.compile_scala_sources(System.err, output_dir.java_path, flags, deps.asJava,
      sources.asJava)
  }

  val isabelle_tool = Isabelle_Tool("scalajs", "test scalajs", Scala_Project.here, 
  { args =>
    val progress = new Console_Progress(verbose = true)
    val output_dir = scalajs_home + Path.basic("output")
    Isabelle_System.rm_tree(output_dir)
    Isabelle_System.make_directory(output_dir)
    compile_ir(output_dir, progress = progress)
  })
}

class Scalajs_Tools extends Isabelle_Scala_Tools(Scalajs.isabelle_tool)
package isabelle


import java.nio.file.{Path => JPath}
import scala.jdk.CollectionConverters._


object Scalajs {
  val scalajs_home = Path.explode("$SCALAJS_HOME")
  val scalajs_library = scalajs_home + Path.explode("lib/scalajs-library_2.13-1.20.1.jar")

  def compile_ir(output_dir: Path, progress: Progress = new Progress): Unit = {
    val isa_component = Scala_Build.context(Path.explode("$ISABELLE_HOME"), component = true)
    val deps = 
      scalajs_library.java_path ::
        isa_component.requirements.map(_.dir.java_path) :::
        (for {
          s <- setup.Environment.getenv("ISABELLE_CLASSPATH").split(":", -1).toList
          if s.nonEmpty
        } yield JPath.of(setup.Environment.platform_path(s)))

    val sources = isa_component.sources.filter(_.implode.contains("src/Pure/")).map(_.java_path)
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
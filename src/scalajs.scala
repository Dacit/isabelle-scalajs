package isabelle


import java.nio.file.{Path => JPath}
import scala.jdk.CollectionConverters._
import org.scalajs.linker.interface.{StandardConfig, Semantics, ESFeatures, ModuleKind,
  ModuleSplitStyle, OutputPatterns}
import org.scalajs.linker.{PathIRContainer, StandardImpl, PathOutputDirectory}
import org.scalajs.logging
import org.scalajs.logging.Level
import scala.concurrent.ExecutionContext.Implicits.global


object Scalajs {
  val scalajs_home = Path.explode("$SCALAJS_HOME")
  val scalajs_deps = scalajs_home + Path.explode("lib/scalajs-deps.jar")

  val classpath = Classpath(List(scalajs_deps.file)).jars.map(_.toPath)

  def compile_ir(output_dir: Path, progress: Progress = new Progress): Unit = {
    val sources =
      for {
        source <- Scala_Build.context(Path.explode("$ISABELLE_HOME"), component = true).sources
        if source.implode.contains("/src/Pure/")
      } yield source.java_path
  
    progress.echo("Generating IR for " + sources.length + " sources ...")

    val flags = "-scalajs -scalajs-genStaticForwardersForNonTopLevelObjects"

    setup.Build.compile_scala_sources(System.err, output_dir.java_path, flags, classpath.asJava,
      ((scalajs_home + Path.explode("src/scalajs_example.scala")).java_path :: sources).asJava)
  }

  val linker_config =
    StandardConfig()
      .withSemantics(Semantics.Defaults.optimized)
      .withModuleKind(ModuleKind.ESModule)
      .withModuleSplitStyle(ModuleSplitStyle.FewestModules)
      .withOutputPatterns(OutputPatterns.Defaults)
      .withESFeatures(ESFeatures.Defaults)
      .withCheckIR(false)
      .withOptimizer(true)
      .withParallel(true)
      .withSourceMap(false)
      .withClosureCompiler(false)
      .withPrettyPrint(false)
      .withBatchMode(true)

  def link(input_dir: Path, output_dir: Path, progress: Progress = new Progress): Unit = {
    progress.echo("Linking IR ...")
    val linker = StandardImpl.linker(linker_config)
    val cache = StandardImpl.irFileCache().newCache

    val logger =
      new logging.Logger {
        override def log(level: Level, message: => String): Unit =
          level match {
            case Level.Error => progress.echo_error_message(message)
            case Level.Warn => progress.echo_warning(message)
            case Level.Info => progress.echo(message)
            case Level.Debug => if (progress.verbose) progress.echo(message)
          }
        override def trace(t: => Throwable): Unit = throw t
      }

    val link_path = input_dir.java_path :: classpath

    val futures = 
      for {
        containers <- PathIRContainer.fromClasspath(link_path)
        ir_files <- cache.cached(containers._1)
        result <- linker.link(ir_files, Nil, PathOutputDirectory(output_dir.java_path), logger)
      } yield result

    val result = scala.concurrent.Await.result(futures, scala.concurrent.duration.Duration.Inf)
    progress.echo("Finished compilation: " + result)
  }

  val isabelle_tool = Isabelle_Tool("scalajs", "test scalajs", Scala_Project.here, 
  { args =>
    val progress = new Console_Progress(verbose = true)


    val output_dir = scalajs_home + Path.basic("output")
    val ir_dir = output_dir + Path.basic("ir")
    val js_dir = output_dir + Path.basic("js")

    Isabelle_System.rm_tree(ir_dir)
    Isabelle_System.make_directory(ir_dir)
    Isabelle_System.make_directory(js_dir)

    compile_ir(ir_dir, progress = progress)
    link(ir_dir, js_dir, progress = progress)
  })
}

class Scalajs_Tools extends Isabelle_Scala_Tools(Scalajs.isabelle_tool)
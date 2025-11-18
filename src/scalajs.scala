package isabelle

object Scalajs {
  val isabelle_tool = Isabelle_Tool("scalajs", "test scalajs", Scala_Project.here, 
  { args =>
    
  })
}

class Scalajs_Tools extends Isabelle_Scala_Tools(Scalajs.isabelle_tool)
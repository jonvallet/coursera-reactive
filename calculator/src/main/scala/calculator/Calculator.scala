package calculator

sealed abstract class Expr
final case class Literal(v: Double) extends Expr
final case class Ref(name: String) extends Expr
final case class Plus(a: Expr, b: Expr) extends Expr
final case class Minus(a: Expr, b: Expr) extends Expr
final case class Times(a: Expr, b: Expr) extends Expr
final case class Divide(a: Expr, b: Expr) extends Expr

object Calculator {
  def computeValues(
      namedExpressions: Map[String, Signal[Expr]]): Map[String, Signal[Double]] = {
    ???
  }

  def eval(expr: Expr): Double = expr match {
    case Literal(a) => a
    case Plus(a: Expr, b: Expr) => eval(a) + eval(b)
    case Minus(a: Expr, b: Expr) => eval(a) - eval(b)
    case Times(a: Expr, b: Expr) => eval(a) * eval(b)
    case Divide(a: Expr, b: Expr) => eval(a) / eval(b)
  }

  def eval(expr: Expr, references: Map[String, Signal[Expr]]): Double = {
    if (references.isEmpty)
      eval(expr)
    else
      eval(expr) + eval(references.head._2(), references.tail)
  }

  /** Get the Expr for a referenced variables.
   *  If the variable is not known, returns a literal NaN.
   */
  private def getReferenceExpr(name: String,
      references: Map[String, Signal[Expr]]) = {
    references.get(name).fold[Expr] {
      Literal(Double.NaN)
    } { exprSignal =>
      exprSignal()
    }
  }
}

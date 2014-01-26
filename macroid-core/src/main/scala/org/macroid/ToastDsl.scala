package org.macroid

import android.widget.Toast
import android.view.View
import scala.concurrent.{ ExecutionContext, Future }

case class Loaf(f: Toast ⇒ Unit) {
  def apply(t: Toast) = f(t)
}

private[macroid] trait Loafs {
  val long = Loaf(_.setDuration(Toast.LENGTH_LONG))
  def gravity(g: Int, xOffset: Int = 0, yOffset: Int = 0) = Loaf(_.setGravity(g, xOffset, yOffset))
  val fry = Loaf(_.show())
}

object Loafs extends Loafs

private[macroid] trait ToastBuilding {
  def toast(text: CharSequence)(implicit ctx: AppContext) = UiThreading.runOnUiThread {
    Toast.makeText(ctx.get, text, Toast.LENGTH_SHORT)
  }

  def toast(view: ⇒ View)(implicit ctx: AppContext) = UiThreading.runOnUiThread {
    new Toast(ctx.get) { setView(view); setDuration(Toast.LENGTH_SHORT) }
  }
}

object ToastBuilding extends ToastBuilding

private[macroid] trait Loafing {
  import UiThreading._

  implicit class LoafingOps(toast: Future[Toast])(implicit ec: ExecutionContext) {
    def ~>(loaf: Loaf) = toast mapUi { t ⇒ loaf(t); t }
  }
}

object Loafing extends Loafing

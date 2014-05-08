package macroid

import android.view.animation.{ AlphaAnimation, Animation }
import android.view.View
import scala.concurrent.{ Future, Promise, ExecutionContext }
import android.view.animation.Animation.AnimationListener
import scala.util.Success
import android.widget.ProgressBar
import scala.util.control.NonFatal
import macroid.util.AfterFuture
import java.util.concurrent.{ TimeUnit, Executors }

private[macroid] object SnailScheduler {
  val scheduler = Executors.newScheduledThreadPool(1)
  def snailSchedulerEc(millis: Long) = new ExecutionContext {
    def execute(runnable: Runnable) = scheduler.schedule(runnable, millis, TimeUnit.MILLISECONDS)
    def reportFailure(t: Throwable) = t.printStackTrace()
  }
}

private[macroid] trait BasicSnails {
  import SnailScheduler._

  /** A delay to be inserted somewhere between <@~s and <~s */
  def delay(millis: Long) = Snail[View](x ⇒ Future(())(snailSchedulerEc(millis)))

  /** A snail that waits for a given future to finish */
  def wait(f: Future[Any])(implicit ec: ExecutionContext) = Snail[View](x ⇒ AfterFuture(f, ()))
}

private[macroid] trait ProgressSnails extends BasicSnails with VisibilityTweaks {
  import Tweaking._
  import Snailing._
  import UiThreading._

  /** Show this progress bar with indeterminate progress and hide it once `future` is done */
  def waitProgress(future: Future[Any])(implicit ec: ExecutionContext): Snail[ProgressBar] =
    Tweak[ProgressBar] { x ⇒ x.setIndeterminate(true) } + show +@ wait(future) @+ hide

  /** Show this progress bar with determinate progress and hide it once all futures are done */
  def waitProgress(futures: List[Future[Any]])(implicit ec: ExecutionContext): Snail[ProgressBar] =
    Tweak[ProgressBar] { x ⇒
      x.setIndeterminate(false)
      x.setMax(futures.length)
      x.setProgress(0)
      futures.foreach(f ⇒ f.recover { case NonFatal(_) ⇒ }.foreachUi(_ ⇒ x.incrementProgressBy(1)))
    } + show +@ wait(Future.sequence(futures)) @+ hide
}

private[macroid] trait AnimationSnails extends VisibilityTweaks {
  import Snailing._

  /** Run animation, indicating when it’s finished */
  def anim(animation: Animation, duration: Long = -1L) = Snail[View] { x ⇒
    val animPromise = Promise[Unit]()
    animation.setAnimationListener(new AnimationListener {
      override def onAnimationStart(a: Animation) {}
      override def onAnimationRepeat(a: Animation) {}
      override def onAnimationEnd(a: Animation) { animPromise.complete(Success(())) }
    })
    if (duration >= 0) animation.setDuration(duration)
    x.startAnimation(animation)
    animPromise.future
  }

  /** Fade in this view */
  def fadeIn(millis: Long)(implicit ec: ExecutionContext) = show +@ anim(new AlphaAnimation(0, 1), duration = millis)
  /** Fade out this view */
  def fadeOut(millis: Long)(implicit ec: ExecutionContext) = anim(new AlphaAnimation(1, 0), duration = millis) @+ hide
}

private[macroid] trait Snails
  extends BasicSnails
  with ProgressSnails
  with AnimationSnails

object Snails extends Snails

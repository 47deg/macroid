package org.macroid

import android.view.animation.{ AlphaAnimation, Animation }
import android.view.View
import scala.concurrent.{ Future, Promise, ExecutionContext, future }
import android.view.animation.Animation.AnimationListener
import scala.util.Success

trait Snails extends Snailing with Tweaks {
  /** Run animation, indicating when it’s finished */
  def anim(animation: Animation, duration: Long = -1L): Snail[View] = { x ⇒
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

  /** A delay to be inserted somewhere between ~@>s and ~>s */
  def delay(millis: Long)(implicit ec: ExecutionContext): Snail[View] = x ⇒ future { Thread.sleep(millis) }
  /** A snail that waits for a given future to finish */
  def wait(f: Future[Any])(implicit ec: ExecutionContext): Snail[View] = x ⇒ f.map(_ ⇒ ())

  /** Fade in this view */
  def fadeIn(millis: Long)(implicit ec: ExecutionContext) = show +@ anim(new AlphaAnimation(0, 1), duration = millis)
  /** Fade out this view */
  def fadeOut(millis: Long)(implicit ec: ExecutionContext) = anim(new AlphaAnimation(1, 0), duration = millis) @+ hide
}

object Snails extends Snails

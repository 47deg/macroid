package org.macroid.util

import org.macroid.UiThreading

class Ui[+A](protected val v: () ⇒ A) {
  def map[B](f: A ⇒ B) = Ui(f(v()))
  def flatMap[B](f: A ⇒ Ui[B]) = new Ui(f(v()).v)

  /** Run the code on the UI thread */
  def run = UiThreading.runOnUiThread(v())

  /** Get the result of executing the code on the current thread */
  def get = v()
}

object Ui {
  def apply[A](v: ⇒ A) = new Ui(() ⇒ v)
  def sequence[A](vs: Ui[A]*) = Ui(vs.map(_.v()))
}

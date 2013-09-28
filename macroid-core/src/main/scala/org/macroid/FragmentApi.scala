package org.macroid

import android.app.ActionBar
import android.app.FragmentTransaction
import android.support.v4.app.Fragment
import android.widget.FrameLayout
import android.content.Context
import android.app.ActionBar.Tab

trait FragmentApi { self: ViewSearch ⇒
  /** Create a fragment and wrap it in a FrameLayout */
  def fragment(frag: ⇒ Fragment, id: Int, tag: String)(implicit ctx: Context): FrameLayout = {
    findFrag[Fragment](tag) getOrElse {
      fragmentManager.beginTransaction().add(id, frag, tag).commit()
    }
    new FrameLayout(ctx) { setId(id) }
  }

  class TabListener(frag: ⇒ Fragment, tag: String, onSelect: Option[Tab ⇒ Any] = None) extends ActionBar.TabListener {
    def onTabSelected(tab: Tab, dummy: FragmentTransaction) {
      val ft = fragmentManager.beginTransaction()
      findFrag[Fragment](tag).map(ft.attach).getOrElse(ft.add(android.R.id.content, frag, tag))
      ft.commit()
      onSelect.foreach(_.apply(tab))
    }

    def onTabUnselected(tab: Tab, dummy: FragmentTransaction) {
      val ft = fragmentManager.beginTransaction()
      findFrag[Fragment](tag).map(ft.detach)
      ft.commit()
    }

    def onTabReselected(tab: Tab, dummy: FragmentTransaction) {
      onSelect.foreach(_.apply(tab))
    }
  }

  implicit class RichActionBar(bar: ActionBar) {
    def addTab(title: CharSequence, frag: ⇒ Fragment, tag: String, focus: Boolean) {
      bar.addTab(bar.newTab().setTag(tag).setText(title).setTabListener(new TabListener(frag, tag)), focus)
    }
    def addTab(title: Int, frag: ⇒ Fragment, tag: String, focus: Boolean) {
      bar.addTab(bar.newTab().setTag(tag).setText(title).setTabListener(new TabListener(frag, tag)), focus)
    }
    def addTab(title: CharSequence, frag: ⇒ Fragment, tag: String, focus: Boolean, onSelect: Tab ⇒ Any) {
      bar.addTab(bar.newTab().setTag(tag).setText(title).setTabListener(new TabListener(frag, tag, Some(onSelect))), focus)
    }
    def addTab(title: Int, frag: ⇒ Fragment, tag: String, focus: Boolean, onSelect: Tab ⇒ Any) {
      bar.addTab(bar.newTab().setTag(tag).setText(title).setTabListener(new TabListener(frag, tag, Some(onSelect))), focus)
    }
  }
}
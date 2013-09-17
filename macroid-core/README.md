# *Macroid* — a Scala layout DSL for Android

### Requirements

* Scala `2.10+`
* Android `API 11+` (uses `Fragment`s from the support library and the stock `ActionBar`)

### What’s the buzz

*Macroid* is intended to complement the excellent [scaloid](https://github.com/pocorall/scaloid) project. It’s by no means
a replacement, but rather a collection of some useful things under a slightly different sauce :)

Let’s see what we have.

#### The DSL

```scala
// in the activity/fragment class, prepare two member variables
// to hold references to our `View`s
var status: TextView = _
var bar: ProgressBar = _
...
// create a LinearLayout
val view = l[LinearLayout](

  // a TextView
  // note how we use `~>` to tweak each View
  // adventurous unicode lovers can of course stick with `⇝`
  w[TextView] ~>
    // if screen width is more than 400 dp, set long text, otherwise — the short one
    text(minWidth(400 dp) ? "Loooooooading..." | "Loading...")) ~>
    // assign to `var status` above
    wire(status),
    
  // a ProgressBar
  w[ProgressBar] ~>
    // set id to a freshly generated id
    id(Id.progress) ~>
    // assign to `var bar` above
    wire(bar),
    
  // an Easter Egg for people with high-density screens
  w[ImageView] ~> (xhdpi ? show | hide) ~> { x ⇒
    // we can use closures for extra initialization
    x.setImageResource(R.drawable.enjoyingXhdpiHuh)
  },
    
    
  // finally, a button
  w[Button] ~>
    // a shortcut to setOnClickListener
    On.click {
      // hide the progress bar
      bar ~> hide
      // set status text
      status ~> text("Finished!")
    } ~>
    // button’s caption
    text("Click me!") ~>
    // stretch horizontally
    layoutParams(WRAP_CONTENT, MATCH_PARENT),
    
  // create a fragment with freshly generated id and tag
  // convert the rest of the arguments to a Bundle and pass to the fragment
  // put the fragment inside a FrameLayout and insert into our layout
  f[MyAwesomeFragment](Id.stuff, Tag.stuff, "items" → 4, "title" → "buzz")
)
setContentView(view)
```

The three main components are:
* `l[...]` — a macro to create layouts. Supports arbitrary `ViewGroup`s
* `w[...]` — a macro to create widgets. Supports arbitrary `View`s, even with parameters. The only requirement is that `Context` parameter is the first one in `View`’s constructor.
* `f[...]` — a macro to create fragments. It creates the fragment if not already created, wraps in a `FrameLayout` and returns it.
  The macro tries to create the fragment using `newInstance()` from its companion and passing the arguments through.
  If there is no suitable overload of `newInstance()`, it treats the args as a sequence of 2-tuples, converts them to a `Bundle`,
  creates the fragment with the primary constructor and passes the bundle to `setArguments`.

Notice these little things:
* `Id.foo` automatically generates and id for you. The id will be the same upon consequent calls. **Note that ids are not checked.** They are generated starting from 1000. You can override the default id generator.
* `Tag.bar` is a fancy way of saying `"bar"`, added for symmetry.

Configuration of views is done with `~>`. The idea is to provide as many convenient and common transforms as possible.
It is easy to make one yourself:
```scala
def id[A <: View](id: Int): ViewMutator[A] = x ⇒ x.setId(id)
```

#### Noteworthy predefined transforms

Note that you can add transforms together with `+`. If you like `scalaz`, there is a `Monoid` instance available, so
you can use `~` and `|+|` as well.

* `wire` assigns the view to the `var` you provide (see example above).
* `hide`, `show`.
* `layoutParams` or `lp`:
  Automatically uses `LayoutParams` constructor from the parent layout or its superclass.
  ```scala
  import ViewGroup.LayoutParams._ // to get WRAP_CONTENT and MATCH_PARENT
  import org.macroid.Transforms._ // to get layoutParams or lp
  
  l[MyShinyLayout](
      w[Button] ~> layoutParams(WRAP_CONTENT, MATCH_PARENT) ~> text("Click me")
  )
  ```
  To use `layoutParams` ouside the layout, take a look at `layoutParams.of[B](...)`,
  for which you can supply the layout type in `B`.
* You can setup almost any `View` event listener with the following syntax:
  ```scala
  import org.scaloid.common._ // to get toast
  import org.macroid.Transforms._ // to get most of the stuff
  import org.macroid.Util.ByName

  l[LinearLayout](
      w[Button] ~>
        text("Click me") ~>
        On.click(toast("Howdy?")) ~> // use by-name argument
        On.longClick { toast("Splash!"); true }, // use by-name argument, returning a value
      w[Button] ~>
        text("Don’t click me") ~>
        FuncOn.click { v: View ⇒ toast(v.getText) } ~> // use function of the same type as OnClickListener.onClick
        ByNameOn.longClick(ByName { toast("Balderdash!"); true }) // use ByName, same as () ⇒ { toast(...); true }
  ) ~> vertical
  ```
  Why 3 flavors? Suppose we had a single `On.foo` method working with both functions and by-name arguments (like in *scaloid*).
  If you accidentaly pass in a function with the wrong type signature, the by-name overload overtakes, and you end up with a
  function that is not called and no warning. Here, `On.foo` *always* uses by-name arguments and `FuncOn.foo` *always* uses
  functions and issues an error if its argument does not match the listener type signature. Additionally, `ByNameOn.foo`
  allows you to use `ByName` blocks, which is handy if you want to pass the listeners around before assigning.

#### Searching for views and fragments

*Macroid* offers three utils:
* `def findView[A <: View](id: Int): A`
* `def findView[A <: View](root: View, id: Int): A`
* `def findFrag[A <: Fragment](tag: String): A`

They come in two flavors, for `Activities` and `Fragments` respectively: `trait ActivityViewSearch` and `trait FragmentViewSearch`.

#### Media queries (inspired by the eponymous CSS feature)

Media queries allow to use different contols, layouts, transforms or other types of things, depending on display
metrics and orientation. Here are some examples:
```scala
l[LinearLayout](...) ~> (minWidth(1000) ? horizontal | vertical)

(minWidth(1000) ? w[BigTextView] | minWidth(600) ? w[MediumTextView] | w[TextView]) ~> text("Hi there")

w[TextView] ~> text("Balderdash!") ~> (minWidth(500) ?! largeAppearance)
```

Currently supported are
* minWidth, maxWidth
* minHeight, maxHeight
* ldpi, mdpi, hdpi, xhdpi

#### Concurrency

*Macroid* provides a bunch of great ways to run things on UI thread.

Using scala `Future`s:
```scala
import org.macroid.Concurrency._
future {
  ...
} onSuccessUi { case x ⇒
  ...
} onFailureUi { case t ⇒
  ...
}
```

Using akka dataflow:
```scala
import akka.dataflow._
import org.macroid.Concurrency._
flow {
  val a = await(someFuture)
  val b = await(otherFuture)
  switchToUiThread()
  findView[Button](Id.myButton) ~> text(a + b)
} onFailureUi { case t ⇒
  t.printStackTrace()
  findView[TextView](Id.error) ~> text("Oops...")
}
```

The usual `runOnUiThread` has been pimped, so that now it returns a `Future` as well. Everything you put inside
is wrapped in a `Try`, so you don’t have to worry about your layout being destroyed.

### Installation

If you plan to use dataflow:
```scala
autoCompilerPlugins := true

libraryDependencies <+= scalaVersion {
  v => compilerPlugin("org.scala-lang.plugins" % "continuations" % v)
}

scalacOptions += "-P:continuations:enable"
```

To include macroid itself:
```scala
resolvers ++= Seq(
  "JCenter" at "http://jcenter.bintray.com",
  Resolver.sonatypeRepo("snapshots")
)

libraryDependencies += "org.macroid" %% "macroid" % "1.0.0-20130916"
```

### Imports and traits

Most of the stuff comes both in ```trait``` and ```object``` flavors. You can use
```scala
import org.macroid.Concurrency
class MyActivity extends Activity with Concurrency {
...
}
```
or
```scala
import org.macroid.Concurrency._
class MyActivity extends Activity {
...
}
```

Likewise, to use ```LayoutDsl```, inherit from it or import from it.
The ```f[...]``` macro, however, requires ```FragmentDsl``` *trait*, which in its turn depends on either ```ActivityViewSearch``` or ```FragmentViewSearch```.
Thus, the usage is
```scala
import org.macroid._
class MyActivity extends Activity with ActivityViewSearch with LayoutDsl with FragmentDsl {
...
}
class MyFragment extends Fragment with FragmentViewSearch with LayoutDsl with FragmentDsl {
...
}
```

```Transforms``` come both in a trait and in an object.

`MediaQueries` come in a trait `MediaQueries`, as well as in
objects `MediaQueries` and `MQ` (the latter being a useful shortcut to allow `MQ.minWidth(...)`).

# medley-demo

This was the code used to render visuals for a
[song medley](https://www.youtube.com/watch?v=Bcgale0E-pI). It's quite a mess
and not very reusable, but I figured I'd share anyway.

## Post-mortem

Originally I had intended to make this a reusable wrapper around
[Quil](https://github.com/quil/quil) (hence the `theater` top-level namespace)
but ultimately it's gonna need a lot more work.

* `theater.audio` deserves to be a new project. Having access to audio samples
as a Clojure seq is quite nice. It needs some cleanup, performance
optimization, should not be dependent on ffmpeg, etc.

* The `theater.visual/Visual` protocol was an interesting way to handle
state for this project. It's a bit unfriendly to stateless visuals though.
`demos.medley/BackgroundOsc` kinda highlights this limitation as it has to
keep track of the total time passed.

* `demos.medley/make-console-scope` highlights some problems with the way
drawing is handled. It is a bit of a pain to apply the correct stroke, fill,
scale/translation to a nested visual.

* The `theater.timeline/make-timeline` API is pretty cool. Just don't look at
the implementation.

* [Boot](https://github.com/boot-clj/boot) is awesome.

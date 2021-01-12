* Work out the semantics of `set` and `setq` and implement a special form for it.
* The `heap.cons()` implementation is incorrect, but it really needs garbage collection to be sane.
* Garbage collection. But what to do about recursive destroy? I don't like recursive code in this project.
* I might not need the Process class as it seems to get in the way. It's a convenient wrapper, but for what?
* BUG: an empty expression pushed on the stack results in infinite eval.

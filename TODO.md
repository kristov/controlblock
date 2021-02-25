* The `heap.cons()` implementation is incorrect, but it really needs garbage collection to be sane.
* Garbage collection. But what to do about recursive destroy? I don't like recursive code in this project.
* Remove "cond" form and replace with "if" - "cond" is a macro
* Check for memory leaks and report in tests. Everything is reachable from root, everything else is orphaned
* Work out "frame" semantics. This "frame" allows for imports, closures and package declarations

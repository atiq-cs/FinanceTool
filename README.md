### ERDelta
Finance Assets Helper

**Supported platforms**

Any platform that runs kotlin
 

**Command Line Examples**

We provide the year on command argument. This is due to the nature that our
input tables are organized quarter wise and dates in tables always belong to
same year!

Run from outside of project dir, assuming it's `~/code/kotlin/ERDelta`,
```plain
gradle run --project-dir ~/Code/kotlin/ERDelta" --args="2026"
```
  
I utilize following simple version (run inside the project dir) frequently,
```plain
gradle run --quiet --console=plain --args="2026"
```
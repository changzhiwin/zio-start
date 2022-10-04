# Which
> Any object that implements the ZIOSpecDefault trait is a runnable test

# How to run
```
// All
stb> Test/test

// Single
sbt> Test/testOnly zio.reference.experiment.testing.HelloWorldSpec
sbt> Test/runMain zio.reference.experiment.testing.HelloWorldSpec
```
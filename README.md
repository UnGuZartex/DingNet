# DingNet
The source code for the DingNet simulator.

Current up to date version: **1.2.0.**


## Building the simulator

To build the simulator, simply run the command `mvn compile`. The generated source are placed in the `target` folder.
The simulator can then be run with the following command: `mvn exec:java`.

Alternatively, run the command `mvn package`. This will generate a jar file under the target directory: `DingNet-{version}-jar-with-dependencies.jar`.

Similarly to the previously listed commands, `mvn test` runs the tests for the project.

## Running the simulator

Either run the jar file generated from the previous step, or use the maven exec plugin.
<!-- A jar file is exported to the folder DingNetExe which also contains the correct file structure. Run the jar file to run the simulator.
The simulator can also be started from the main method in the MainGUI class. -->



## Libraries

DingNet uses the following libraries:
- AnnotationsDoclets (included in the lib folder, since it is not available online (yet))
- jfreechart-1.5.0
- jxmapviewer2-2.4


## Future goals

- [ ] Refactor Inputprofile
- [ ] Refactor QualityOfService
- [ ] Realistic data generation
- [ ] Rewrite transmission logic (moveTo, transmission power, ...)
- [ ] \(Not important) Allow creation of circular routes for motes

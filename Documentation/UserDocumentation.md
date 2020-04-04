# DingNet User Documentation

In this file you'll find all the information needed to set up a simulation.
## How to setup/load a simulation configuration.
TODO

## How to setup a Pollutionenvironment.

First of all, a simulation configuration has to be loaded in, otherwise an error message will pop up.
 
 ![ErrorMessage](Images/ErrorMessage.PNG) 

See [How to setup/load a simulation configuration.](#how-to-setupload-a-simulation-configuration)

Then, two options are available. The first is loading in an already existing Pollutionenvironment file. The Second is creating your own Pollutionenvironment from scratch.

### Loading in an already existing Pollutionenvironment file.
This can be done by clicking the open button at the top of the screen.
 
 ![OpenButton](Images/OpenImage.png) 

Opening this will open the following screen:
 
 ![OpenFile](Images/SelectFileToLoad.PNG) 

Here you can select the file to load in, a simple file looks like this:
 
 ![Example](Images/ExampleXML.PNG) 

To add a new source/sensor it suffices to fill in all these values declared in the file.

See [Source restrictions.](#source-restrictions) to know which functions can be used in the \<function> tag.

### Creating a Pollutionenvironment file from scratch/Editing a Pollutionenvironment file.

When no configuration is loaded in a blank Pollutionenvironment is loaded in, meaning that no pollution will be generated.

Following steps count for creating and editing the pollutionenvironment.

There are two possible options to change.

The first is the sources. To do this press the Configure Sources button at the top of the screen.

 ![Sources](Images/ConfigureSourcesImage.png) 
 
This will open following screen:

 ![Sourcesview](Images/SelectSourceInListImage.png) 
 
 In the list you are able to select a source to change. This will fill in all values and plot the graph:
 
  ![Values](Images/Values_to_Change_Image.png) 
  ![Graph](Images/GraphOfTheSourceImage.png) 
  
All fields can be updated manually with a few restrictions, for these restrictions see [Source restrictions.](#source-restrictions).

To make it easier to set the position of the Source. A map is added that, if the user clicks within the black square, updates the Position field to the corresponding position.




### Source restrictions


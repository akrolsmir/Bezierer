## About
A sweet Bezier surface converter.

Authors: Alec Mouri (cs184-cq) and Austin Chen (cs184-cn)

This program is written in Java, and was developed against the Windows and [Android](http://puu.sh/8eDxa.apk) platforms.

Youtube 

## Features
* Conversion from Bezier patches to Polygon representations
* Rendering of objects using OpenGL
![](http://www-inst.eecs.berkeley.edu/~cs184-cn/Bezierer/images/input-01.jpg)

* Toggling of Uniform and Adaptive Tessellation
![](http://www-inst.eecs.berkeley.edu/~cs184-cn/Bezierer/images/input-02.jpg)
![](http://www-inst.eecs.berkeley.edu/~cs184-cn/Bezierer/images/input-03.jpg)

* Toggling between flat and smooth shading
* Input and output of .obj files
![](http://www-inst.eecs.berkeley.edu/~cs184-cn/Bezierer/images/input-15.jpg)
![](http://puu.sh/8eDQ7.png)

* Toggling between filled, wireframe, and hidden-line mode
![](http://www-inst.eecs.berkeley.edu/~cs184-cn/Bezierer/images/input-04.jpg)
![](http://www-inst.eecs.berkeley.edu/~cs184-cn/Bezierer/images/input-05.png)
![](http://www-inst.eecs.berkeley.edu/~cs184-cn/Bezierer/images/input-06.png)

* Rotation and Translation of objects
* Loading and control of multiple objects
![](http://www-inst.eecs.berkeley.edu/~cs184-cn/Bezierer/images/input-08.jpg)
![](http://www-inst.eecs.berkeley.edu/~cs184-cn/Bezierer/images/input-09.jpg)
![](http://www-inst.eecs.berkeley.edu/~cs184-cn/Bezierer/images/input-10.jpg)

* Vertex color shading of Gaussian curvatures
![](http://www-inst.eecs.berkeley.edu/~cs184-cn/Bezierer/images/input-13.jpg)

* An Android demo (.apk available [here](http://puu.sh/8eDxa.apk))
![](http://www-inst.eecs.berkeley.edu/~cs184-cn/Bezierer/images/input-51.png)
![](http://www-inst.eecs.berkeley.edu/~cs184-cn/Bezierer/images/input-52.png)


## Command line arguments:
The first argument is always the input file and the second argument is always a subdivision parameter.
Additional arguments:
 * `-a` -- Specifies the use of adaptive tessellation.
 * `-o [file_name]` -- Specifies the output of an .obj file to file_name

## Controls:
* `s` -- Toggles between flat and smooth shading
* `w` -- Toggles between wireframe and filled mode
* `h` -- Toggles between hidden-line and filled mode
* `Arrow keys` -- Rotates the selected object
* `Shift + arrow keys` -- Translates the selected object
* `+/-` -- Zooms in and out
* `v` -- Selects the next object
* `c` -- Toggles coloring of Gaussian curvature

## Scene format:
The .scene file format contains a list of files to be rendered. Each file corresponds to a separate object. Before each file is specified, translations and rotations with respect to the origin must be specified:

Example (test.scene):

	translate 0 0 -10	# Translates Teapot1 -10 along the z-axis
	rotate 0 0 0		# No rotation specified for Teapot1
	teapot.bez			# Defines Teapot1
	translate .5 .5 -15	# Translates Teapot2 .5 along the x-axis, .5 along the y-axis, -15 along the z-axis, 
	rotate 90 90 90		# Rotate Teapot2 by 90 degress along each axis.
	teapot.bez			# Defines Teapot2
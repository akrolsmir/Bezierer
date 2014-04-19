## About

A sweet Bezier surface converter.

Authors: Alec Mouri (cs184-cq) and Austin Chen (cs184-cn)

This program is written in Java, and was developed against the Windows and Android platforms.

## Features

 * Conversion from Bezier patches to Polygon representations
 * Toggling of Uniform and Adaptive Tessellation
 * Rendering of objects using OpenGL
 * Toggling between flat and smooth shading
 * Toggling between filled, wireframe, and hidden-line mode
 * Rotation and Translation of objects
 * Input and output of .obj files
 * Loading and control of multiple objects
 * Vertex color shading of Gaussian curvatures
 * A limited-feature Android version.

## Command line arguments:
The first argument is always the input file and the second argument is always a subdivision parameter.
Additional arguments:
 * `-a` -- Specifies the use of adaptive tessellation.
 * `-o [file_name]` -- Specifies the output of an .obj file to file_name

## Controls:
 * s -- Toggles between flat and smooth shading
 * w -- Toggles between wireframe and filled mode
 * h -- Toggles between hidden-line and filled mode
 * Arrow keys -- Rotates the selected object
 * Shift + arrow keys -- Translates the selected object
 * +/- -- Zooms in and out
 * v -- Selects the next object
 * c -- Toggles coloring of Gaussian curvature

## Scene format:
The .scene file format contains a list of files to be rendered. Each file corresponds to a separate object. Before each file is specified, translations and rotations with respect to the origin must be specified:

Example (test.scene):

	translate 0 0 -10	# Translates Teapot1 -10 along the z-axis
	rotate 0 0 0		# No rotation specified for Teapot1
	teapot.bez			# Defines Teapot1
	translate .5 .5 -15	# Translates Teapot2 .5 along the x-axis, .5 along the y-axis, -15 along the z-axis, 
	rotate 90 90 90		# Rotate Teapot2 by 90 degress along each axis.
	teapot.bez			# Defines Teapot2
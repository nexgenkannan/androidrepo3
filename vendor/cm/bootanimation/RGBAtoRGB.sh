#!/bin/bash

# Gaël Duval <gael@e.email> for e Foundation
# Note: needs imagemagick convert utility

for i in `ls .`
do echo $i
    convert ./$i -define png:color-type=2 -flatten +matte ./$i
done
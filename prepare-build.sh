#!/bin/bash
# This will make the build work from the command line
chmod +x gradlew
printf "GITLAB_FABRIC_KEY = \"\"\n" > gradle.properties

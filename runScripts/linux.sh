#!/bin/sh

# dependencies: uname, wget or curl, tar

##############################
##### JDK DOWNLOAD URLS ######
##############################
AARCH64_JDK_URL='https://github.com/adoptium/temurin8-binaries/releases/download/jdk8u432-b06/OpenJDK8U-jdk_aarch64_linux_hotspot_8u432b06.tar.gz'
ARM_JDK_URL='https://github.com/adoptium/temurin8-binaries/releases/download/jdk8u432-b06/OpenJDK8U-jdk_arm_linux_hotspot_8u432b06.tar.gz'
PPC64LE_JDK_URL='https://github.com/adoptium/temurin8-binaries/releases/download/jdk8u432-b06/OpenJDK8U-jdk_ppc64le_linux_hotspot_8u432b06.tar.gz'
X64_JDK_URL='https://github.com/adoptium/temurin8-binaries/releases/download/jdk8u432-b06/OpenJDK8U-jdk_x64_linux_hotspot_8u432b06.tar.gz'

if ! ls jdk/bin/java > /dev/null 2> /dev/null; then
	if ! arch=$(uname -m); then
		echo "Could not find architecture" >& 2
		exit 1
	fi

	if [ "$arch" = "aarch64" ]; then
		url=$AARCH64_JDK_URL
	elif [ "$arch" = "arm" ]; then
		url=$ARM_JDK_URL
	elif [ "$arch" = "ppc64le" ]; then
		url=$PPC64LE_JDK_URL
	elif [ "$arch" = "x86_64" ]; then
		url=$X64_JDK_URL
	else
		echo "Unsupported architecture $arch" >& 2
		exit 1
	fi
	echo "Downloading JDK..."

	if ! tar --version > /dev/null 2> /dev/null; then
		echo "tar is missing, aborting" >& 2
		exit 1
	fi

	if wget --version > /dev/null 2> /dev/null; then
	  if wget --version | grep -q 'Wget2'; then
	    echo "Using Wget2"
	    if ! wget -O jdk.tar.gz -nv "$url"; then
        echo "Failed to download JDK with wget2" >& 2
        exit 1
      fi
    else
	    echo "Using Wget"
      if ! wget -O jdk.tar.gz -nv --show-progress "$url"; then
        echo "Failed to download JDK with wget" >& 2
        exit 1
      fi
    fi
	elif curl --version > /dev/null 2> /dev/null; then
	  echo "Using curl"
		if ! curl "$url" -L > jdk.tar.gz; then
			echo "Failed to download JDK with cURL" >& 2
			exit 1
		fi
	else
		echo "Both wget and cURL are missing, could not download." >& 2
		exit 1
	fi


	tar -xzf jdk.tar.gz -C .
	mv jdk8* jdk
	rm jdk.tar.gz
fi

if ! ls run > /dev/null 2> /dev/null; then
	mkdir run
fi

cd run
../jdk/bin/java -jar ../launcher.jar

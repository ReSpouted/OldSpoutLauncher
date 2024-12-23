#!/bin/sh

# dependencies: uname, wget or curl, tar

##############################
##### JDK DOWNLOAD URLS ######
##############################
X64_JDK_URL='https://github.com/adoptium/temurin8-binaries/releases/download/jdk8u432-b06/OpenJDK8U-jdk_x64_mac_hotspot_8u432b06.pkg'

if ! ls jdk/bin/java > /dev/null 2> /dev/null; then
	if ! arch=$(uname -m); then
		echo "Could not find architecture" >& 2
		exit 1
	fi

	# either x64 and can run natively or arm64 and has rosetta
	if test "$arch" = "x86_64" || test "$arch" = "arm64" && arch -arch x86_64 uname -m > /dev/null; then
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
		if ! wget -O jdk.tar.gz -nv --show-progress "$url"; then
			echo "Failed to download JDK with wget" >& 2
			exit 1
		fi
	elif curl --version > /dev/null 2> /dev/null; then
		if ! curl "$url" -L > jdk.pkg; then
			echo "Failed to download JDK with cURL" >& 2
			exit 1
		fi
	else
		echo "Both wget and cURL are missing, could not download." >& 2
		exit 1
	fi

	mkdir tmp
	cd tmp
	tar -xf ../jdk.pkg
	cd *.pkg
	cat Payload*/Payload | gunzip -dc | cpio -i
	cd ../..
	mv tmp/*.pkg/Library/Java/JavaVirtualMachines/temurin-8.jdk/Contents/Home jdk
	rm -rf tmp jdk.pkg

fi

if ! ls run > /dev/null 2> /dev/null; then
	mkdir run
fi

cd run
../jdk/bin/java -jar ../launcher.jar


##############################
##### JDK DOWNLOAD URLS ######
##############################
$X64_JDK_URL = 'https://github.com/adoptium/temurin8-binaries/releases/download/jdk8u432-b06/OpenJDK8U-jdk_x64_windows_hotspot_8u432b06.zip'
$X86_JDK_URL = 'https://github.com/adoptium/temurin8-binaries/releases/download/jdk8u432-b06/OpenJDK8U-jdk_x86-32_windows_hotspot_8u432b06.zip'


ls jdk > $null 2> $null
if(!$?) {
	# https://stackoverflow.com/a/61396489
	$is64Bit = Test-Path 'Env:ProgramFiles(x86)'
	if($is64Bit) {
		$url = $X64_JDK_URL
	} else {
		$url = $X86_JDK_URL
	}
	
	echo "Downloading JDK..."
	Invoke-WebRequest $url -OutFile jdk.zip
	Expand-Archive jdk.zip -DestinationPath .
	mv jdk8* jdk
	rm jdk.zip
}

mkdir run > $null 2> $null

cd run
../jdk/bin/java.exe -jar ../launcher.jar
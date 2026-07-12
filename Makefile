.PHONY: release debug pull tasks find repo one install clean

JAVA_HOME := /Library/Java/JavaVirtualMachines/temurin-21.jdk/Contents/Home

release:
	./gradlew --no-daemon assembleRelease

debug:
	./gradlew --no-daemon assembleDebug

repo:
	open https://github.com/Wavesonics/FastTrack
tasks:
	./gradlew --no-daemon -q :tasks

find:
	find app/build/outputs -iname '*.apk'
	open -R app/build/outputs/apk/release/*.apk

one:
	adb -s 321c9dca install app/build/outputs/apk/release/*.apk

install:
	adb install app/build/outputs/apk/release/*.apk

install-debug:
	adb install app/build/outputs/apk/debug/*.apk

clean:
	./gradlew --no-daemon clean

check-jetifier:
	 ./gradlew checkJetifier

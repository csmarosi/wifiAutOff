.PHONY : all clean eclipse sdk16

all:
	android update project --path . --name WifiAutOff --target android-23
	ant debug

clean:
	git ls-files -o | xargs rm -rf

eclipse: all
	eclipse -nosplash

sdk16: clean
	android update project --path . --name WifiAutOff --target android-16
	ant debug

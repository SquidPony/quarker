SHELL := /usr/bin/env bash

MAIN_CLASS := my.quarker.Quarker
APP_NAME := Quarker
SRC_DIR := src/main/java
RES_DIR := src/main/resources

BUILD_DIR := build
CLASSES_DIR := $(BUILD_DIR)/classes
RES_BUILD_DIR := $(BUILD_DIR)/resources
WEB_CLASSES_DIR := $(BUILD_DIR)/classes-web
WEB_RES_BUILD_DIR := $(BUILD_DIR)/resources-web
DIST_ROOT := $(BUILD_DIR)/dist
ARTIFACTS_DIR := $(BUILD_DIR)/artifacts
NATIVE_ROOT := $(BUILD_DIR)/native
WEB_ROOT := $(BUILD_DIR)/web
WIN_JPACKAGE ?= jpackage.exe
PYTHON ?= /home/rogue/code/quarker/.venv/bin/python
WEB_JAVA_RELEASE := 17
WEB_JAVA_SRC_DIR := src/web-java
DESKTOP_SWING_UI := $(SRC_DIR)/net/slashie/libjcsi/wswing/WSwingConsoleInterface.java
WEB_JAVA_FILES := $(shell find $(SRC_DIR) -name '*.java' ! -path '$(DESKTOP_SWING_UI)' | sort)
WEB_OVERRIDE_FILES := $(shell find $(WEB_JAVA_SRC_DIR) -name '*.java' 2>/dev/null | sort)
MAVEN_VERSION := 3.9.9
MAVEN_HOME := $(BUILD_DIR)/tools/apache-maven-$(MAVEN_VERSION)
MAVEN := $(MAVEN_HOME)/bin/mvn

VERSION ?= $(shell git describe --tags --always --dirty 2>/dev/null || echo local)
override VERSION := $(subst /,-,$(VERSION))
DIST_NAME := quarker-$(VERSION)
DIST_DIR := $(DIST_ROOT)/$(DIST_NAME)
JAR_NAME := quarker.jar
JAR_PATH := $(DIST_DIR)/$(JAR_NAME)
WEB_DIR := $(WEB_ROOT)/$(DIST_NAME)

JAVA_FILES := $(shell find $(SRC_DIR) -name '*.java' | sort)

.PHONY: help clean build run jar build-web-classes web web-serve package-web package package-all package-native package-native-current package-native-linux package-native-macos package-native-windows package-native-wsl release-artifacts

help:
	@echo "Targets:"
	@echo "  make build            Compile all Java sources"
	@echo "  make run              Build and run the game locally"
	@echo "  make jar              Build runnable JAR"
	@echo "  make web              Build browser bundle via TeaVM (open-source, self-hosted)"
	@echo "  make web-serve        Build browser bundle and serve locally"
	@echo "  make package-web      Build browser ZIP release bundle"
	@echo "  make package          Build ZIP and TAR.GZ release bundles"
	@echo "  make package-all      Build release bundles + native executable(s)"
	@echo "  make package-native   Build native executable(s) for this environment"
	@echo "  make package-native-linux    Build Linux app image"
	@echo "  make package-native-macos    Build macOS app image"
	@echo "  make package-native-windows  Build Windows app image"
	@echo "  make release-artifacts Alias for package"
	@echo "  make clean            Remove build output"

clean:
	rm -rf $(BUILD_DIR)

build:
	mkdir -p $(CLASSES_DIR)
	javac -d $(CLASSES_DIR) $(JAVA_FILES)
	mkdir -p $(RES_BUILD_DIR)
	if [[ -d "$(RES_DIR)" ]]; then \
	  cp -R $(RES_DIR)/. $(RES_BUILD_DIR)/; \
	fi
	echo "version=$(VERSION)" > $(RES_BUILD_DIR)/version.properties

run: build
	@if [[ -d "$(RES_BUILD_DIR)" ]]; then \
	  java -cp "$(CLASSES_DIR):$(RES_BUILD_DIR)" $(MAIN_CLASS); \
	else \
	  java -cp "$(CLASSES_DIR)" $(MAIN_CLASS); \
	fi

jar: build
	mkdir -p $(DIST_DIR)
	jar --create --file "$(JAR_PATH)" --main-class $(MAIN_CLASS) -C "$(CLASSES_DIR)" .
	if [[ -d "$(RES_BUILD_DIR)" ]]; then \
	  jar --update --file "$(JAR_PATH)" -C "$(RES_BUILD_DIR)" .; \
	fi

build-web-classes:
	rm -rf "$(WEB_CLASSES_DIR)" "$(WEB_RES_BUILD_DIR)"
	mkdir -p "$(WEB_CLASSES_DIR)"
	@if [[ -z "$(WEB_OVERRIDE_FILES)" ]]; then \
	  echo "Missing web override sources in $(WEB_JAVA_SRC_DIR)."; \
	  exit 1; \
	fi
	javac --release $(WEB_JAVA_RELEASE) -d "$(WEB_CLASSES_DIR)" $(WEB_JAVA_FILES) $(WEB_OVERRIDE_FILES)
	mkdir -p "$(WEB_RES_BUILD_DIR)"
	if [[ -d "$(RES_DIR)" ]]; then \
	  cp -R $(RES_DIR)/. "$(WEB_RES_BUILD_DIR)/"; \
	fi
	echo "version=$(VERSION)" > "$(WEB_RES_BUILD_DIR)/version.properties"

$(MAVEN):
	bash scripts/bootstrap_maven.sh $(MAVEN_VERSION) "$(MAVEN_HOME)"

web: build-web-classes $(MAVEN)
	rm -rf "$(WEB_DIR)"
	mkdir -p "$(WEB_DIR)"
	"$(MAVEN)" -q -f tools/teavm/pom.xml org.teavm:teavm-maven-plugin:0.14.0:compile \
	  -Dquarker.web.classFiles="$(PWD)/$(WEB_CLASSES_DIR)" \
	  -Dquarker.web.targetDirectory="$(PWD)/$(WEB_DIR)/assets"
	cp -R src/web/. "$(WEB_DIR)/"
	printf '%s\n' "$(VERSION)" > "$(WEB_DIR)/version.txt"
	printf '%s\n' "Web bundle created in $(WEB_DIR)"

web-serve: web
	$(PYTHON) scripts/web_server.py --directory "$(WEB_DIR)" --port 8000

package-web: web
	mkdir -p $(ARTIFACTS_DIR)
	cd "$(WEB_ROOT)" && zip -rq "../artifacts/quarker-$(VERSION)-web.zip" "$(DIST_NAME)"

package: jar
	mkdir -p $(ARTIFACTS_DIR)
	printf "Quarker\n\nRun:\n  java -jar quarker.jar\n\nNotes:\n  Requires Java 25 or newer.\n" > "$(DIST_DIR)/README.txt"
	cd "$(DIST_ROOT)" && zip -rq "../artifacts/quarker-$(VERSION)-universal.zip" "$(DIST_NAME)"
	cd "$(DIST_ROOT)" && tar -czf "../artifacts/quarker-$(VERSION)-universal.tar.gz" "$(DIST_NAME)"

package-all: package package-native

package-native: jar
	@if grep -qiE "(microsoft|wsl)" /proc/version 2>/dev/null; then \
	  $(MAKE) package-native-wsl VERSION="$(VERSION)"; \
	else \
	  $(MAKE) package-native-current VERSION="$(VERSION)"; \
	fi

package-native-current: jar
	@platform=""; \
	case "$$(uname -s 2>/dev/null || echo $$OS)" in \
	  Darwin*) platform="package-native-macos" ;; \
	  Linux*) platform="package-native-linux" ;; \
	  MINGW*|MSYS*|CYGWIN*|Windows_NT) platform="package-native-windows" ;; \
	  *) echo "Unsupported OS for native packaging."; exit 1 ;; \
	esac; \
	$(MAKE) "$$platform" VERSION="$(VERSION)"

package-native-linux: jar
	@out_dir="$(NATIVE_ROOT)/linux"; \
	rm -rf "$$out_dir"; \
	mkdir -p "$$out_dir"; \
	jpackage \
	  --type app-image \
	  --name "$(APP_NAME)" \
	  --dest "$$out_dir" \
	  --input "$(DIST_DIR)" \
	  --main-jar "$(JAR_NAME)" \
	  --main-class "$(MAIN_CLASS)"; \
	echo "Native app image created in $$out_dir"

package-native-macos: jar
	@out_dir="$(NATIVE_ROOT)/macos"; \
	rm -rf "$$out_dir"; \
	mkdir -p "$$out_dir"; \
	jpackage \
	  --type app-image \
	  --name "$(APP_NAME)" \
	  --dest "$$out_dir" \
	  --input "$(DIST_DIR)" \
	  --main-jar "$(JAR_NAME)" \
	  --main-class "$(MAIN_CLASS)"; \
	echo "Native app image created in $$out_dir"

package-native-windows: jar
	@if grep -qiE "(microsoft|wsl)" /proc/version 2>/dev/null; then \
	  command -v "$(WIN_JPACKAGE)" >/dev/null 2>&1 || { echo "$(WIN_JPACKAGE) not found; cannot build Windows app image from WSL."; exit 1; }; \
	  dist_input="$$(wslpath -w "$(DIST_DIR)")"; \
	  out_dir_wsl="$(NATIVE_ROOT)/windows"; \
	  out_dir_win="$$(wslpath -w "$$out_dir_wsl")"; \
	  rm -rf "$$out_dir_wsl"; \
	  mkdir -p "$$out_dir_wsl"; \
	  "$(WIN_JPACKAGE)" \
	    --type app-image \
	    --name "$(APP_NAME)" \
	    --dest "$$out_dir_win" \
	    --input "$$dist_input" \
	    --main-jar "$(JAR_NAME)" \
	    --main-class "$(MAIN_CLASS)"; \
	  echo "Windows app image created in $$out_dir_wsl"; \
	else \
	  out_dir="$(NATIVE_ROOT)/windows"; \
	rm -rf "$$out_dir"; \
	mkdir -p "$$out_dir"; \
	jpackage \
	  --type app-image \
	  --name "$(APP_NAME)" \
	  --dest "$$out_dir" \
	  --input "$(DIST_DIR)" \
	  --main-jar "$(JAR_NAME)" \
	  --main-class "$(MAIN_CLASS)"; \
	  echo "Windows app image created in $$out_dir"; \
	fi

package-native-wsl: jar
	@echo "WSL detected: building Linux app image..."; \
	$(MAKE) package-native-linux VERSION="$(VERSION)"; \
	if command -v "$(WIN_JPACKAGE)" >/dev/null 2>&1; then \
	  echo "WSL detected: building Windows app image via $(WIN_JPACKAGE)..."; \
	  $(MAKE) package-native-windows VERSION="$(VERSION)"; \
	else \
	  echo "Skipping Windows app image in WSL: $(WIN_JPACKAGE) not found in PATH."; \
	  echo "Install or expose Windows JDK jpackage.exe to PATH to enable it."; \
	fi

release-artifacts: package package-web

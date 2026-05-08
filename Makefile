SHELL := /usr/bin/env bash

MAIN_CLASS := my.quarker.Quarker
SRC_DIR := src/main/java
RES_DIR := src/main/resources

BUILD_DIR := build
CLASSES_DIR := $(BUILD_DIR)/classes
RES_BUILD_DIR := $(BUILD_DIR)/resources
DIST_ROOT := $(BUILD_DIR)/dist
ARTIFACTS_DIR := $(BUILD_DIR)/artifacts

VERSION ?= $(shell git describe --tags --always --dirty 2>/dev/null || echo local)
DIST_NAME := quarker-$(VERSION)
DIST_DIR := $(DIST_ROOT)/$(DIST_NAME)
JAR_NAME := quarker.jar
JAR_PATH := $(DIST_DIR)/$(JAR_NAME)

JAVA_FILES := $(shell find $(SRC_DIR) -name '*.java' | sort)

.PHONY: help clean build run jar package release-artifacts

help:
	@echo "Targets:"
	@echo "  make build            Compile all Java sources"
	@echo "  make run              Build and run the game locally"
	@echo "  make jar              Build runnable JAR"
	@echo "  make package          Build ZIP and TAR.GZ release bundles"
	@echo "  make release-artifacts Alias for package"
	@echo "  make clean            Remove build output"

clean:
	rm -rf $(BUILD_DIR)

build:
	mkdir -p $(CLASSES_DIR)
	javac -d $(CLASSES_DIR) $(JAVA_FILES)
	if [[ -d "$(RES_DIR)" ]]; then \
	  mkdir -p $(RES_BUILD_DIR); \
	  cp -R $(RES_DIR)/. $(RES_BUILD_DIR)/; \
	fi

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

package: jar
	mkdir -p $(ARTIFACTS_DIR)
	printf "Quarker\n\nRun:\n  java -jar quarker.jar\n\nNotes:\n  Requires Java 25 or newer.\n" > "$(DIST_DIR)/README.txt"
	cd "$(DIST_ROOT)" && zip -rq "../artifacts/$(DIST_NAME).zip" "$(DIST_NAME)"
	cd "$(DIST_ROOT)" && tar -czf "../artifacts/$(DIST_NAME).tar.gz" "$(DIST_NAME)"

release-artifacts: package

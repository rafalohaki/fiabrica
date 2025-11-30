#!/bin/bash

# Fiabrica Build Script (Linux/macOS)
# Auto-downloads gradle-wrapper.jar, detects setup, and builds project

set -e  # Exit on error

echo "========================================"
echo "  Fiabrica - Automated Build Script"
echo "========================================"
echo ""

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Check if gradle-wrapper.jar exists
WRAPPER_JAR="gradle/wrapper/gradle-wrapper.jar"

if [ ! -f "$WRAPPER_JAR" ]; then
    echo -e "${YELLOW}[INFO]${NC} gradle-wrapper.jar not found. Downloading..."
    
    # Create wrapper directory if not exists
    mkdir -p gradle/wrapper
    
    # Download from official Gradle GitHub (Gradle 8.10)
    WRAPPER_URL="https://raw.githubusercontent.com/gradle/gradle/v8.10.2/gradle/wrapper/gradle-wrapper.jar"
    
    if command -v curl &> /dev/null; then
        echo -e "${GREEN}[OK]${NC} Using curl to download wrapper..."
        curl -L "$WRAPPER_URL" -o "$WRAPPER_JAR" --silent --show-error
    elif command -v wget &> /dev/null; then
        echo -e "${GREEN}[OK]${NC} Using wget to download wrapper..."
        wget "$WRAPPER_URL" -O "$WRAPPER_JAR" -q
    else
        echo -e "${RED}[ERROR]${NC} Neither curl nor wget found. Please install one of them."
        exit 1
    fi
    
    if [ -f "$WRAPPER_JAR" ]; then
        echo -e "${GREEN}[OK]${NC} gradle-wrapper.jar downloaded successfully!"
    else
        echo -e "${RED}[ERROR]${NC} Failed to download gradle-wrapper.jar"
        exit 1
    fi
else
    echo -e "${GREEN}[OK]${NC} gradle-wrapper.jar already exists."
fi

# Make gradlew executable
if [ -f "gradlew" ]; then
    chmod +x gradlew
    echo -e "${GREEN}[OK]${NC} Made gradlew executable."
else
    echo -e "${RED}[ERROR]${NC} gradlew script not found!"
    exit 1
fi

# Check Java version
echo ""
echo -e "${YELLOW}[INFO]${NC} Checking Java version..."
if command -v java &> /dev/null; then
    JAVA_VERSION=$(java -version 2>&1 | awk -F '"' '/version/ {print $2}' | cut -d'.' -f1)
    if [ "$JAVA_VERSION" -ge 21 ]; then
        echo -e "${GREEN}[OK]${NC} Java $JAVA_VERSION detected (required: 21+)"
    else
        echo -e "${RED}[ERROR]${NC} Java $JAVA_VERSION detected, but Java 21+ is required!"
        echo -e "${YELLOW}[INFO]${NC} Please install Java 21 or higher."
        exit 1
    fi
else
    echo -e "${RED}[ERROR]${NC} Java not found! Please install Java 21 or higher."
    exit 1
fi

# Clean previous builds (optional)
echo ""
read -p "Clean previous builds? (y/N): " -n 1 -r CLEAN_BUILD
echo
if [[ $CLEAN_BUILD =~ ^[Yy]$ ]]; then
    echo -e "${YELLOW}[INFO]${NC} Cleaning build directory..."
    ./gradlew clean
    echo -e "${GREEN}[OK]${NC} Build cleaned."
fi

# Build the project
echo ""
echo -e "${YELLOW}[INFO]${NC} Building fiabrica..."
echo "========================================"
./gradlew build --no-daemon

# Check if build was successful
if [ $? -eq 0 ]; then
    echo ""
    echo "========================================"
    echo -e "${GREEN}[SUCCESS]${NC} Build completed successfully!"
    echo "========================================"
    echo ""
    
    # Find built jar
    JAR_FILE=$(find build/libs -name "fiabrica-*.jar" ! -name "*-sources.jar" | head -n 1)
    
    if [ -n "$JAR_FILE" ]; then
        echo -e "${GREEN}[OK]${NC} Built JAR: $JAR_FILE"
        JAR_SIZE=$(du -h "$JAR_FILE" | cut -f1)
        echo -e "${GREEN}[OK]${NC} Size: $JAR_SIZE"
        echo ""
        echo -e "${YELLOW}[INFO]${NC} Place this JAR in your Minecraft mods folder:"
        echo "         ~/.minecraft/mods/ (Linux/macOS)"
        echo "         %APPDATA%\\.minecraft\\mods\\ (Windows)"
    fi
else
    echo ""
    echo "========================================"
    echo -e "${RED}[FAILED]${NC} Build failed! Check errors above."
    echo "========================================"
    exit 1
fi

echo ""
echo -e "${GREEN}Done!${NC}"

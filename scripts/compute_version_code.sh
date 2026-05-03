#!/bin/bash
#
# Compute versionCode from semantic version string
# Format: MAJOR.MINOR.PATCH[-PRERELEASE]
# versionCode = MAJOR*10000000 + MINOR*10000 + PATCH*100 + BUILD_NUMBER
#
# Example:
#   2.1.3     -> 20103100
#   2.1.3-rc1 -> 20103101 (rc increments build)
#

VERSION="${1:?Usage: $0 <version_string>]}"

# Extract semantic version parts
if [[ $VERSION =~ ^([0-9]+)\.([0-9]+)\.([0-9]+)(-([a-zA-Z]+)(([0-9]+)?))? ]]; then
    MAJOR="${BASH_REMATCH[1]}"
    MINOR="${BASH_REMATCH[2]}"
    PATCH="${BASH_REMATCH[3]}"
    PRERELEASE="${BASH_REMATCH[5]}"
    PRERELEASE_NUM="${BASH_REMATCH[6]}"

    # Base versionCode
    VERSION_CODE=$((MAJOR * 10000000 + MINOR * 10000 + PATCH * 100))

    # Increment based on pre-release type
    case "$PRERELEASE" in
        alpha)   VERSION_CODE=$((VERSION_CODE + 1)) ;;
        beta)    VERSION_CODE=$((VERSION_CODE + 2)) ;;
        rc)      VERSION_CODE=$((VERSION_CODE + 3 + (PRERELEASE_NUM ? PRERELEASE_NUM : 0))) ;;
        *)       VERSION_CODE=$((VERSION_CODE + 99)) ;;  # dev builds
    esac

    echo "$VERSION_CODE"
else
    echo "ERROR: Invalid semantic version format: $VERSION" >&2
    exit 1
fi

require conf/distro/include/security_flags.inc
include recipes-core/images/core-image-base.bb

COMPATIBLE_MACHINE = "^rpi$"
INHERIT += "create-spdx cve-check"

LICENSE = "MIT"

IMAGE_INSTALL:append = "\
	${CORE_IMAGE_BASE_INSTALL} \
	util-linux-agetty \
	packagegroup-core-full-cmdline \
    wolfssl \
	networkmanager \
"

IMAGE_INSTALL:remove = "\
	wpa-supplicant \
"
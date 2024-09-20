require conf/distro/include/security_flags.inc
inherit core-image

COMPATIBLE_MACHINE = "^rpi$"
INHERIT += "create-spdx cve-check"

LICENSE = "MIT"

IMAGE_FEATURES += "read-only-rootfs"

IMAGE_INSTALL:append = "\
	util-linux-agetty \
    wolfssl \
	networkmanager \
	base-files \
    base-passwd \
    busybox \
    cryptsetup \
    initramfs-module-dmverity \
    initramfs-module-udev \
    lvm2-udevrules \
    udev \
    util-linux-mount \
	os-release \
	${@bb.utils.contains('DISTRO_FEATURES', 'selinux', 'packagegroup-core-selinux', '', d)} \
"

IMAGE_INSTALL:remove = "\
	wpa-supplicant \
	alsa* \
"

# Ensure dm-verity.env is updated also when rebuilding DM_VERITY_IMAGE
do_image[nostamp] = "1"

deploy_verity_hash() {
    install -D -m 0644 \
        ${STAGING_VERITY_DIR}/${DM_VERITY_IMAGE}.${DM_VERITY_IMAGE_TYPE}.verity.env \
        ${IMAGE_ROOTFS}${datadir}/misc/dm-verity.env
}
IMAGE_PREPROCESS_COMMAND += "deploy_verity_hash;"

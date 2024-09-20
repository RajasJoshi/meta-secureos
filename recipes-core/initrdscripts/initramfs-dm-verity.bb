SUMMARY = "Simple init script that uses devmapper to mount the rootfs in read-only mode protected by dm-verity"
LICENSE = "CLOSED"

SRC_URI = "file://init-dm-verity.sh"

do_install() {
    install -m 0755 ${WORKDIR}/init-dm-verity.sh ${D}/init
    install -d ${D}/dev
    mknod -m 622 ${D}/dev/console c 5 1
}

FILES:${PN} = "/init /dev/console"
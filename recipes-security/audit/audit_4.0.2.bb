SUMMARY = "User space tools for kernel auditing"
DESCRIPTION = "The audit package contains the user space utilities for \
storing and searching the audit records generated by the audit subsystem \
in the Linux kernel."
HOMEPAGE = "http://people.redhat.com/sgrubb/audit/"
SECTION = "base"
LICENSE = "GPL-2.0-or-later & LGPL-2.0-or-later"
LIC_FILES_CHKSUM = "file://COPYING;md5=94d55d512a9ba36caa9b7df079bae19f"

SRC_URI = "git://github.com/linux-audit/${BPN}-userspace.git;branch=master;protocol=https \
           file://0001-Fixed-swig-host-contamination-issue.patch \
           file://auditd \
           file://audit-volatile.conf \
          "

SRC_URI:append:libc-musl = " file://0001-Replace-__attribute_malloc__-with-__attribute__-__ma.patch"

S = "${WORKDIR}/git"
SRCREV = "4e6deae41d4646d28bb3ba9524a8a227a38ccd0b"

inherit autotools python3targetconfig update-rc.d systemd

UPDATERCPN = "auditd"
INITSCRIPT_NAME = "auditd"
INITSCRIPT_PARAMS = "defaults"

SYSTEMD_PACKAGES = "auditd"
SYSTEMD_SERVICE:auditd = "auditd.service audit-rules.service"

DEPENDS = "python3 tcp-wrappers libcap-ng linux-libc-headers swig-native python3-setuptools-native coreutils-native"

EXTRA_OECONF = " \
        --with-libwrap \
        --with-libcap-ng \
        --with-python3 \
        --with-arm \
        --with-aarch64 \
        --without-golang \
        --disable-gssapi-krb5 \
        --disable-zos-remote \
        --sbindir=${base_sbindir} \
        --runstatedir=/run \
        "

EXTRA_OEMAKE = " \
        PYTHON=python3 \
        pythondir=${PYTHON_SITEPACKAGES_DIR} \
        pyexecdir=${PYTHON_SITEPACKAGES_DIR} \
        STDINC='${STAGING_INCDIR}' \
        "

SUMMARY:audispd-plugins = "Plugins for the audit event dispatcher"
DESCRIPTION:audispd-plugins = "The audispd-plugins package provides plugins for the real-time \
interface to the audit system, audispd. These plugins can do things \
like relay events to remote machines or analyze events for suspicious \
behavior."

PACKAGES =+ "audispd-plugins"
PACKAGES += "auditd ${PN}-python"

FILES:${PN} = "${sysconfdir}/libaudit.conf ${libdir}/libau*.so.*"
FILES:auditd = "${bindir}/* ${base_sbindir}/* ${sysconfdir}/* ${datadir}/audit-rules/* ${libexecdir}/*"
FILES:audispd-plugins = "${sysconfdir}/audit/audisp-remote.conf \
        ${sysconfdir}/audit/plugins.d/au-remote.conf \
        ${sysconfdir}/audit/plugins.d/syslog.conf \
        ${base_sbindir}/audisp-remote \
        ${base_sbindir}/audisp-syslog \
        ${localstatedir}/spool/audit \
        "
FILES:${PN}-dbg += "${libdir}/python${PYTHON_BASEVERSION}/*/.debug"
FILES:${PN}-python = "${libdir}/python${PYTHON_BASEVERSION}"

CONFFILES:auditd = "${sysconfdir}/audit/audit.rules"

do_configure:prepend() {
    sed -e 's|buf\[];|buf[0];|g'  ${STAGING_INCDIR}/linux/audit.h > ${S}/lib/audit.h
    sed -i -e 's|#include <linux/audit.h>|#include "audit.h"|g' ${S}/lib/libaudit.h
}

do_install:append() {
    sed -i -e 's|#include "audit.h"|#include <linux/audit.h>|g' ${D}${includedir}/libaudit.h

    # Install default rules
    install -d -m 750 ${D}/etc/audit
    install -d -m 750 ${D}/etc/audit/rules.d

    install -m 0640 ${S}/rules/10-base-config.rules ${D}/etc/audit/rules.d/audit.rules

    # Based on the audit.spec "Copy default rules into place on new installation"
    install -m 0640 ${D}/etc/audit/rules.d/audit.rules ${D}/etc/audit/audit.rules

    if ${@bb.utils.contains('DISTRO_FEATURES', 'systemd', 'true', 'false', d)}; then
        install -D -m 0644 ${WORKDIR}/audit-volatile.conf ${D}${sysconfdir}/tmpfiles.d/audit.conf
    fi

    if ${@bb.utils.contains('DISTRO_FEATURES', 'sysvinit', 'true', 'false', d)}; then
        install -D -m 0755 ${WORKDIR}/auditd ${D}/etc/init.d/auditd
        if ! ${@bb.utils.contains('DISTRO_FEATURES', 'systemd', 'true', 'false', d)}; then
            rm -rf ${D}${libdir}/systemd
        fi
    fi

    # Create /var/spool/audit directory for audisp-remote
    install -d -m 0700 ${D}${localstatedir}/spool/audit
}

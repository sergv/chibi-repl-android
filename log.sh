#!/bin/bash
#
# File: log.sh
#
# Created: Monday,  1 April 2013
#

adb logcat -c
adb logcat | awk '/AndroidRuntime|DEBUG|chibi/ && !/Multiwindow|MultiWindowManagerService|(AndroidRuntime.*(>>>>>> AndroidRuntime START com\.android\.internal\.os\.RuntimeInit <<<<<<|CheckJNI is OFF|setted country_code = Ukraine|setted countryiso_code = UA|setted sales_code = SEK|readGMSProperty: start|readGMSProperty: already setted!!|readGMSProperty: end|Calling main entry com\.android\.commands\.am\.Am))|BackupManagerService/'





exit 0


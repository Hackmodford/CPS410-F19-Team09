from os import path
Import("env")

platform = env.PioPlatform()

env.Prepend(
    UPLOADERFLAGS=["-s", path.join(platform.get_package_dir("tool-openocd"), "scripts") or "", 
                   "-f", "interface/cmsis-dap.cfg",
                   "-c", 'set CHIPNAME at91sam3X8E', 
                   '-c', 'source [find target/at91sam3ax_8x.cfg]']
)
env.Append(
    UPLOADERFLAGS=["-c", "telnet_port disabled; program {$SOURCE} 0x80000 verify reset; shutdown"]
)
env.Replace(
    UPLOADER="openocd",
    UPLOADCMD="$UPLOADER $UPLOADERFLAGS"
)
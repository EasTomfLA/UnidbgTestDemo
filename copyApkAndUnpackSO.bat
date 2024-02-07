set "apkFinalParentPath=D:\OpenSource\unidbg\unidbg-android\src\test\resources\unidbgtestdemo"
set "apkFinalPath=%apkFinalParentPath%\app-debug.apk"
copy app\build\intermediates\apk\debug\app-debug.apk %apkFinalPath%
::rem 将当前目录及目录子文件的zip文件解压到C:\Users\libin\Desktop\人人中，然后删除zip文件
if  exist %apkFinalPath% (
    "C:\Program Files\Bandizip\Bandizip.exe" x -aoa %apkFinalPath% libnativetest.so
    copy %apkFinalParentPath%\lib\arm64-v8a\libnativetest.so %apkFinalParentPath%\
    rmdir /S /Q %apkFinalParentPath%\lib\
)
#include <jni.h>
#include <string>
#include <android/log.h>
#include <link.h>
// for task name
#include <dirent.h>
#include <unistd.h>

#define TAG "nativetest"

#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG,  TAG, __VA_ARGS__)
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO,  TAG, __VA_ARGS__)
#define LOGW(...) __android_log_print(ANDROID_LOG_WARN,  TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, TAG, __VA_ARGS__)

void testEgid();
void get_ld_debug_env();

using namespace std;
string& trim(string &s) {
    if (s.empty())
        return s;
//    log_dbg("sss:%d", s.at(s.size() -1) == 10);
    s = s.at(s.size() -1) == 10 ? s.substr(0, s.size() - 1) : s;
    string::iterator c;
    for (c = s.begin(); c!= s.end() && iswspace(*c++);)
        s.erase(s.begin(), --c);
    for (c = s.begin(); c!= s.end() && iswspace(*--c);)
        s.erase(++c, s.end());
    return s;
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_netease_unidbgtestdemo_MainActivity_usingRefJava(
        JNIEnv* env,
        jclass jclz) {
//    testEgid();
    get_ld_debug_env();
    jclass at = env->FindClass("android/app/ActivityThread");
    jmethodID id = env->GetStaticMethodID(at, "currentPackageName", "()Ljava/lang/String;");
    jstring name = (jstring )env->CallStaticObjectMethod(at, id);
    const char* pName = (const char* )env->GetStringUTFChars(name, nullptr);
    LOGD("currentPackageName is %s", pName);
    std::string hello = "Hello from C++";

    jclass sp = env->FindClass("android/os/SystemProperties");
    jmethodID idGet = env->GetStaticMethodID(sp, "get", "(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;");
//    jmethodID idGet = env->GetStaticMethodID(sp, "get", "(Ljava/lang/String;)Ljava/lang/String;");
    jstring xx = env->NewStringUTF("ro.build.display.id");
    jstring xx2 = env->NewStringUTF("unknow");
    jstring name2 = (jstring )env->CallStaticObjectMethod(sp, idGet, xx, xx2);
    const char* pROID = (const char* )env->GetStringUTFChars(name2, nullptr);
    LOGD("roid is %s", pROID);
    return env->NewStringUTF(pName);
}

static jint myAddNoExport(int n1, int n2) {
    LOGD("myAddNoExport in");
    return n1 + n2;
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_netease_unidbgtestdemo_MainActivity_getAllTaskName(
        JNIEnv* env,
        jclass jclz) {
    std::string hello = "tid ==> taskName";
    LOGD("showTaskName start");

    char tmp[100] = {0};
    pid_t pid = getpid();
    LOGD("my pid is:%d", pid);
    sprintf(tmp, "/proc/%d/task/", pid);
    DIR* dir = opendir(tmp);

    if (NULL == dir) {
        LOGE("opendir %s field", tmp);
        return env->NewStringUTF("");
    }
    while(true) {
        dirent *pDirent = NULL;
        pDirent = readdir(dir);
        if (NULL == pDirent) {
            LOGD("readdir reach NULL, going to break");
            break;
        }

        if (!strcmp(pDirent->d_name, "..") || !strcmp(pDirent->d_name, "."))
            continue;
        if (pDirent->d_type != DT_DIR) {
            continue;
        }

        char taskName[100] = {0};
        sprintf(taskName, "%s%s/status", tmp, pDirent->d_name);

        FILE* statusFile = fopen(taskName, "r");
        char buf[128] = {0};
        if(statusFile) {
            memset(buf, 0, 128);
            fgets(buf, 128, statusFile);
            if (strcmp(buf, "Name:") >= 0) {
                string buf2 = buf;
                buf2 = trim(buf2);
                const char* ret = strstr(buf2.c_str(), "JDWP");
                LOGD("found Name:%8s ==> %-30s, jdwp exist:%s", pDirent->d_name, buf2.c_str(), ret);
                char tmpFormat[0x100] = {0};
                snprintf(tmpFormat, 0x100, "tid:%8s ==> name:%-30s", pDirent->d_name, buf2.c_str());
                hello = hello.append("\r\n").append(tmpFormat);
            }
            fclose(statusFile);
        }
    }

    closedir(dir);
    LOGD("showTaskName end");
    return env->NewStringUTF(hello.c_str());
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_netease_unidbgtestdemo_MainActivity_stringFromJNI(
        JNIEnv* env,
        jclass jclz) {
    std::string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());
}

jstring stringFromJNIDynReg(JNIEnv *env, jclass jclz) {
    std::string hello = "Hello from C++ dyn register";
    return env->NewStringUTF(hello.c_str());
}

jint getStringLen(JNIEnv *env, jclass jclz, jstring str) {
    std::string hello = "Hello from C++ dyn register";
    const char* strC = env->GetStringUTFChars(str, NULL);
    int len = strlen(strC);
    return len;
}

#include <unistd.h>
#define AID_ROOT 0
#define AID_SHELL 2000 /* adb and debug shell user */
#define AID_PACKAGE_INFO 1032    /* access to installed package details */

void testEgid() {
    LOGD("test egid start");
//    setuid(AID_SHELL);
    setuid( AID_ROOT);
    LOGD("test egid start 1");
    setegid(AID_PACKAGE_INFO);
    LOGD("test egid end");
}

void get_ld_debug_env() {
    char *ld_debug = getenv("LD_DEBUG");
    if (ld_debug == nullptr) {
        LOGD("LD_DEBUG environment variable is not set.");
    } else {
        LOGD("LD_DEBUG environment variable is set to: %s", ld_debug);
        // process the value of LD_DEBUG here
    }
}

static const char *classPathName = "com/netease/unidbgtestdemo/MainActivity";
static JNINativeMethod methods[] = {
        {"stringFromJNIDynReg", "()Ljava/lang/String;", (void*)stringFromJNIDynReg },
        {"getStringLen", "(Ljava/lang/String;)I", (void*)getStringLen },
};

static int registerNativeMethods(JNIEnv* env, const char* className,
                                 JNINativeMethod* gMethods, int numMethods)
{
    jclass clazz;
    clazz = env->FindClass(className);
    if (clazz == NULL) {
        LOGE("Native registration unable to find class '%s'", className);
        return JNI_FALSE;
    }
    if ((env)->RegisterNatives(clazz, gMethods, numMethods) < 0) {
        LOGE("RegisterNatives failed for '%s'", className);
        return JNI_FALSE;
    }
    return JNI_TRUE;
}

jint myAddImpl(JNIEnv *env, jobject obj, jint n1, jint n2) {
    return n1 + n2;
}

static const char *classPathDemoTest = "com/netease/unidbgtestdemo/DemoTest";
static JNINativeMethod methodsDemoTest[] = {
        {"myAdd", "(II)I", (void*) myAddImpl },
};

static int registerNatives(JNIEnv* env)
{
    if (!registerNativeMethods(env, classPathName,
                               methods, sizeof(methods) / sizeof(methods[0]))) {
        return JNI_FALSE;
    }
    if (!registerNativeMethods(env, classPathDemoTest,
                               methodsDemoTest, sizeof(methodsDemoTest) / sizeof(methodsDemoTest[0]))) {
        return JNI_FALSE;
    }
    return JNI_TRUE;
}

jint JNI_OnLoad(JavaVM* vm, void* nothing)
{
    JNIEnv* env = NULL;
    jint result = -1;

    LOGD("JNI_OnLoad");
    if ((vm)->GetEnv((void**)&env, JNI_VERSION_1_4) != JNI_OK) {
        LOGD("ERROR: GetEnv failed");
        goto bail;
    }

    if (registerNatives(env) != JNI_TRUE) {
        LOGD("ERROR: registerNatives failed");
        goto bail;
    }

    result = JNI_VERSION_1_4;

    bail:

    LOGD("lalala:%d", myAddNoExport(1, 3));
    return result;
}

extern "C" JNIEXPORT void exportFunction() {
    LOGD("hello im exportFunction");
}
extern "C"
JNIEXPORT jstring JNICALL
Java_com_netease_acsdk_Utils_accessTest(JNIEnv *env, jclass clazz, jstring path) {
    const char* pPath = env->GetStringUTFChars(path, NULL);

    int accRet = access(pPath, F_OK);
    LOGD("access %s ret:%d", pPath, accRet);
    char tmp[0x10] = {0};
    sprintf(tmp, "%d", accRet);
    return env->NewStringUTF(tmp);
}
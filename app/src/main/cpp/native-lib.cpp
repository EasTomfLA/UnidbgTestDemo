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

extern "C" {
// https://ckcat.github.io/2021/02/22/%E5%8A%A8%E6%80%81%E8%B0%83%E8%AF%95so/
// .init_proc => .init_array => JNI_OnLoad
void _init(void) { // EXPORT .init_proc
    LOGD("_init");
}
}
__attribute__((constructor))
void test_constructor() { // .init_array
    LOGD("test_constructor");
}

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

jint initImpl(JNIEnv *env, jclass clz, jint cmdId, jobjectArray objects) {
    LOGD("initImpl start");

    // 获取数组长度
    jsize length = env->GetArrayLength(objects);

    // 获取数组元素
    jstring keyString = (jstring) (env)->GetObjectArrayElement(objects, 0);
    jobject objInt = (jobject) env->GetObjectArrayElement(objects, 1);
    jclass clzInt = env->GetObjectClass(objInt);
    jint intValue = env->CallIntMethod(objInt, env->GetMethodID(clzInt, "intValue", "()I"));

    // 将字符串对象转换为 C 字符串
    const char *key = (env)->GetStringUTFChars(keyString, NULL);
    LOGD("init p1=%s p2=%d", key, intValue);

    // 使用参数值进行后续处理
    // ...

    // 释放资源
    (env)->ReleaseStringUTFChars(keyString, key);
    env->DeleteLocalRef(keyString);
    env->DeleteLocalRef(objInt);

    timeval tv;
    gettimeofday(&tv, nullptr);
    LOGD("initImpl gettimeofday.tv_sec:%ld", tv.tv_sec);
    LOGD("initImpl gettimeofday.tv_usec:%ld", tv.tv_usec);

    // 获取 ActivityThread 类的 Class 对象
    jclass activityThreadCls = env->FindClass("android/app/ActivityThread");

    // 获取 currentActivityThread 方法的 ID
    jmethodID currentActivityThreadMethodId = env->GetStaticMethodID(activityThreadCls, "currentActivityThread", "()Landroid/app/ActivityThread;");

    // 调用 currentActivityThread 方法
    jobject activityThreadObj = env->CallStaticObjectMethod(activityThreadCls, currentActivityThreadMethodId);

    // 获取 getApplication 方法的 ID
    jmethodID getApplicationMethodId = env->GetMethodID(activityThreadCls, "getApplication", "()Landroid/app/Application;");

    // 调用 getApplication 方法
    jobject applicationObj = env->CallObjectMethod(activityThreadObj, getApplicationMethodId);
    jclass clzApplication = env->GetObjectClass(applicationObj);

    // 获取 getApplicationContext 方法的 ID
    jclass contextCls = env->FindClass("android/content/Context");
    jmethodID getApplicationContextMethodId = env->GetMethodID(clzApplication, "getApplicationContext", "()Landroid/content/Context;");

    // 调用 getApplicationContext 方法
    jobject contextObj = env->CallObjectMethod(applicationObj, getApplicationContextMethodId);

    // getPackageCodePath
    jmethodID mIdGPCP = env->GetMethodID(contextCls, "getPackageCodePath", "()Ljava/lang/String;");
    jstring jsPCP = (jstring)env->CallObjectMethod(contextObj, mIdGPCP);
    const char* packageCodePath = env->GetStringUTFChars(jsPCP, NULL);
    LOGD("package code path=%s", packageCodePath);

    jmethodID mIdGPN = env->GetMethodID(contextCls, "getPackageName", "()Ljava/lang/String;");
    jstring jsPN = (jstring)env->CallObjectMethod(contextObj, mIdGPN);
    const char* packageName = env->GetStringUTFChars(jsPN, NULL);
    LOGD("package name=%s", packageName);
    jmethodID mIdGAssets = env->GetMethodID(contextCls, "getAssets", "()Landroid/content/res/AssetManager;");
    jobject objAssets = env->CallObjectMethod(contextObj, mIdGAssets);
    jclass clzAssets = env->GetObjectClass(objAssets);
    jmethodID mIdToString = env->GetMethodID(clzAssets, "toString", "()Ljava/lang/String;");
    jstring jsAsserts = (jstring)env->CallObjectMethod(objAssets, mIdToString);
    const char* assetsName = env->GetStringUTFChars(jsAsserts, NULL);
    LOGD("asstes name=%s", assetsName);
    return 0;
}

static const char *classPathDemoTest = "com/netease/unidbgtestdemo/DemoTest";
static JNINativeMethod methodsDemoTest[] = {
        {"myAdd", "(II)I", (void*) myAddImpl },
        {"init", "(I[Ljava/lang/Object;)I", (void*) initImpl },
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
JavaVM *gJvm;
jclass gClzMainActivity;
jint JNI_OnLoad(JavaVM* vm, void* nothing)
{
    JNIEnv* env = NULL;
    jint result = -1;
    gJvm = vm;
    LOGD("JNI_OnLoad");
    if ((vm)->GetEnv((void**)&env, JNI_VERSION_1_4) != JNI_OK) {
        LOGD("ERROR: GetEnv failed");
        goto bail;
    }

    if (gClzMainActivity == nullptr) {
        jclass clz = env->FindClass("com/netease/unidbgtestdemo/MainActivity");
        gClzMainActivity = static_cast<jclass>(env->NewGlobalRef(clz));
    }
    if (registerNatives(env) != JNI_TRUE) {
        LOGD("ERROR: registerNatives failed");
        goto bail;
    }

    result = JNI_VERSION_1_4;

    bail:

    LOGD("lalala myAddNoExport(1,2)=%d", myAddNoExport(1, 2));
    return result;
}

extern "C" JNIEXPORT void exportFunction() {
    LOGD("hello im exportFunction");
}

#include <sys/system_properties.h>

__attribute__((noinline))  void exitiii() {
    exit(11);
}

extern "C"
JNIEXPORT void JNICALL
Java_com_netease_acsdk_Utils_exit(JNIEnv *env, jclass clazz) {
    // https://bbs.kanxue.com/thread-280754.htm
    exitiii();
}

extern "C"
JNIEXPORT void JNICALL
Java_com_netease_acsdk_Utils_kill(JNIEnv *env, jclass clazz) {
    kill(getpid(), 999);
}

extern "C"
JNIEXPORT void JNICALL
Java_com_netease_acsdk_Utils_abort(JNIEnv *env, jclass clazz) {
    abort();
}

extern "C"
JNIEXPORT void JNICALL
Java_com_netease_acsdk_Utils_property(JNIEnv *env, jclass clazz) {
    char value[PROP_VALUE_MAX] = {0};
    int nnn = __system_property_get("ro.debuggable", value);
    LOGD("ro.debuggable addr:%p %s", &value, value);
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


#include <errno.h>
#include <sys/inotify.h>
#include <pthread.h>
#include <map>
#define EVENT_SIZE (sizeof (struct inotify_event))
#define EVENT_BUFF_LEN (1024 * EVENT_SIZE)
typedef void (*start_inotify_callback)(const char *path, inotify_event *event);
#define HIDDEN_API __attribute__((visibility("hidden")))
class Watch {
    struct wd_elem{
        int pd;
        string name;
    };
    map<int, wd_elem> watch;
public:
    Watch() {
        LOGD("inotify watch init");
    }
    void insert(int pd, const string &name, int wd) {
        wd_elem elem = {pd, name};
        watch[wd] = elem;
    }
    void cleanUp(int fd) {
        for (map<int, wd_elem>::iterator wi = watch.begin(); wi != watch.end(); wi++) {
            inotify_rm_watch( fd, wi->first );
            watch.erase(wi);
        }
    }
    string get(int wd) {
        wd_elem elem;
        {
            auto it = watch.find(wd);
            if(it != watch.end()){
                elem = it->second;
            }else{
                return "";
            }
        }

        return elem.pd == -1 ? elem.name : this->get(elem.pd) + "/" + elem.name;
    }
};
Watch watch;
bool inotifyWatchSwitch = false;
int inotifyFd = -1;
void *inotifyEntry(void* callback) {
    int event_len;                  //事件长度
    char buffer[EVENT_BUFF_LEN];    //事件buffer
//    LOGD("inotify_block start by block switchStatus:%d inotifyFd:%d", inotifyWatchSwitch, inotifyFd);
    start_inotify_callback callbackImpl = (start_inotify_callback)callback;
    int count = 0;
    while (inotifyWatchSwitch) {
//        LOGD("inotify_block read start");
        event_len = read(inotifyFd, buffer, EVENT_BUFF_LEN);   //读取事件
//         LOGD("inotify_block read end event len:%d", event_len);
        if (event_len < 0) {
//            if ((++count)%10 == 0)
//             LOGE("inotify_block inotify_event read failed [errno:%d, desc:%s]", errno, strerror(errno));
            continue;
        }
        int i = 0;
//        LOGD("inotify_block i %d event_len %d",i,event_len);
        while (i < event_len) {
            struct inotify_event *event = (struct inotify_event *) &buffer[i];
//            LOGD("inotify_block event %p event->len:%d",event, event->len);
            if (event->len) {
//                LOGD("inotify_block event len:%d ",event->len);
                if(callback)
                {
//                    LOGD("inotify_block callback ok:%p ",callback);
                    callbackImpl(watch.get(event->wd).c_str(),event);
                }
            }
            i += EVENT_SIZE + event->len;
            // LOGD("inotify_block next i %d event_len %d",i,event_len);
        }
    }
}
JNIEnv *get_env(int *attach) {
    if (gJvm == NULL) return NULL;

    *attach = 0;
    JNIEnv *jni_env = NULL;

    int status = gJvm->GetEnv((void **)&jni_env, JNI_VERSION_1_6);

    if (status == JNI_EDETACHED || jni_env == NULL) {
        status = gJvm->AttachCurrentThread(&jni_env, NULL);
        if (status < 0) {
            jni_env = NULL;
        } else {
            *attach = 1;
        }
    }
    return jni_env;
}
void del_env() {
    gJvm->DetachCurrentThread();
}
void sendToLog(const char* head, const char* content) {
    int attach = 0;
    JNIEnv *env = get_env(&attach);
    jclass clz = gClzMainActivity;
    jmethodID mid = env->GetStaticMethodID(clz, "sendLogData", "(Ljava/lang/String;Ljava/lang/String;)V");
    jstring h = env->NewStringUTF(head);
    jstring data = env->NewStringUTF(content);
    env->CallStaticVoidMethod(clz, mid, h, data);
    if (attach == 1) {
        del_env();
    }
}
void onFileOpen(const char* dir, inotify_event *event) {
//    LOGE("onFileOpen:%s %p", dir, event);
    if (event->mask & IN_OPEN) {
        char tmp[PATH_MAX] = {0};
        if (sprintf(tmp, "%s/%s", dir, event->name) > 0) {
//            LOGE("inotify onFileOpen: %s", tmp);
            sendToLog("watch file result", tmp);
//            int attach = 0;
//            JNIEnv *env = get_env(&attach);
//            jclass clz = gClzMainActivity;
//            jmethodID mid = env->GetStaticMethodID(clz, "sendLogData", "(Ljava/lang/String;Ljava/lang/String;)V");
//            jstring h = env->NewStringUTF("watch file result");
//            jstring data = env->NewStringUTF(tmp);
//            env->CallStaticVoidMethod(clz, mid, h, data);
//            if (attach == 1) {
//                del_env();
//            }
        }
    }
}
int inotifyInit()
{
    if (inotifyFd == -1)
    {
        inotifyFd = inotify_init();
        if (inotifyFd == -1)
        {
            LOGE("inotify init errno:%d desc:%s", errno, strerror(errno));
            return -1;
        }else {
            LOGD("inotify init suss fd:%d", inotifyFd);
        }
    }
    return 0;
}

extern "C"
JNIEXPORT jint JNICALL
Java_com_netease_acsdk_Utils_inotifyAddWatchPath(JNIEnv *env, jclass clazz, jstring path) {
    LOGD("inotifyFd:%d", inotifyFd);
    int intRet = inotifyInit();
    if (-1 == intRet) {
        LOGE("inotify init failed");
        return intRet;
    }
    const char* pPath = env->GetStringUTFChars(path, NULL);
    if (inotifyFd > 0) {
        auto wd = inotify_add_watch(inotifyFd, pPath, IN_ALL_EVENTS);
        if (wd > 0) {
            watch.insert(-1, pPath, wd);
            LOGD("inotify add watch file suss:%s inotifyFd:%d", pPath, inotifyFd);
            return 1;
        }
    }
    LOGE("inotify add watch file fail:%s errno:%d errstr:%s", pPath, errno, strerror(errno));
    return 0;
}
extern "C"
JNIEXPORT jint JNICALL
Java_com_netease_acsdk_Utils_inotifyWatchStart(JNIEnv *env, jclass clazz) {
    pthread_t thread;
    inotifyWatchSwitch = true;
    int ret = pthread_create(&thread, NULL, inotifyEntry, (void*) onFileOpen);
    if (ret != 0) {
        LOGE("inotify watch thread create fail errno:%d desc:%s", errno, strerror(errno));
        return 0;
    }else {
        LOGE("inotify watch thread create succ");
        return 1;
    }
}
extern "C"
JNIEXPORT jint JNICALL
Java_com_netease_acsdk_Utils_inotifyWatchStop(JNIEnv *env, jclass clazz) {
    inotifyWatchSwitch = false;
    close(inotifyFd);
    inotifyFd = -1;
    return 0;
}

void *antiThread(void* callback) {
    for (int i = 0; i < 10; i++) {
        char buf[255] = {0};
        sprintf(buf, "anti thread running %d", i + 1);
        LOGD("%s", buf);
        sendToLog("antiThread", buf);
        sleep(1);
    }
    LOGD("anti thread exit");
    return nullptr;
}

extern "C"
JNIEXPORT void JNICALL
Java_com_netease_unidbgtestdemo_MainActivity_pthreadTest(JNIEnv *env, jclass clazz) {
    pthread_t thread;
    // https://bbs.kanxue.com/thread-277034-1.htm
    int ret = pthread_create(&thread, NULL, antiThread, nullptr);
    if (ret != 0) {
        LOGE("inotify watch thread create fail errno:%d desc:%s", errno, strerror(errno));
    }else {
        LOGD("inotify watch thread create succ");
    }
}
extern "C"
JNIEXPORT jboolean JNICALL
Java_com_netease_unidbgtestdemo_DemoTest_BoolTest(JNIEnv *env, jobject thiz) {

    jclass clzDemoTest = env->GetObjectClass(thiz);
    jfieldID fIdBooleanInstan = env->GetFieldID(clzDemoTest, "aBooleanInstan", "Z");

    jboolean objBooleanInstan = env->GetBooleanField(thiz, fIdBooleanInstan);
    if (objBooleanInstan == JNI_TRUE) {
        LOGD("aBooleanInstan value=true");
    } else {
        LOGD("aBooleanInstan value=false");
    }
    return objBooleanInstan;
}
extern "C"
JNIEXPORT jbyteArray JNICALL
Java_com_netease_unidbgtestdemo_DemoTest_encrypt(JNIEnv *env, jclass clazz, jbyteArray date_input,
                                                 jint n) {
    // 获取数组长度
    jsize length = (env)->GetArrayLength(date_input);

    // 分配内存来保存数组数据
    jbyte *data = (env)->GetByteArrayElements(date_input, NULL);

    // 将 jbyte 数组转换为 C++ 的 byte 数组
    unsigned char *byteData = reinterpret_cast<unsigned char *>(data);

    // 使用 byte 数组进行加密操作
    // ...

    // 创建新的字节数组来保存加密后的数据
    jbyteArray encrypted_data = (env)->NewByteArray(length);

    // 将加密后的数据复制到新的字节数组中
    (env)->SetByteArrayRegion(encrypted_data, 0, length, reinterpret_cast<jbyte *>(byteData));

    // 释放内存
    (env)->ReleaseByteArrayElements(date_input, data, 0);

    return encrypted_data;

}
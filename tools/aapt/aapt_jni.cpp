#include <stdio.h>
#include <stdlib.h>
#include <fcntl.h>
#include <time.h>
#include <string.h>
#include <libgen.h>
#include <jni.h>
#include <android/asset_manager.h>
#include <android/asset_manager_jni.h>
#include <android/log.h>
#include "Main.h"
#include "aapt_jni.h"

#define DEBUG_TAG "SubsAapt"

extern "C" jint Java_projekt_substratum_compiler_aapt_Aapt_JNImain(JNIEnv *env, jobject , jstring args) {
	const char *sz = env->GetStringUTFChars(args, JNI_FALSE);
	char *ptr1, *ptr2;
	int i, idx, argc=1, len;
        jint rc = 99;

	__android_log_print(ANDROID_LOG_DEBUG, DEBUG_TAG, "Native method call: aapt_jni (%s)", sz);
	len= static_cast<int>(strlen(sz));
	for (i=0; i<len; i++) if (sz[i]=='\t') argc++;
	char * argv[argc];
	ptr1 = ptr2 = (char*) sz;
	idx = 0;
	for (i=0; i<len; i++)
	{
		if (*ptr2=='\t')
		{
			*ptr2=0;
			argv[idx]=ptr1;
			idx++;
			ptr1=ptr2+1;
		}
		ptr2++;
	} // for
	argv[idx]=ptr1;

	// redirect stderr and stdout
	freopen ("/sdcard/.substratum/native_stderr.txt", "w", stderr);
	freopen ("/sdcard/.substratum/native_stdout.txt", "w", stdout);

	fprintf (stdout, "Aapt arguments:\n");
	for (i=1; i<argc; i++) fprintf (stdout, "%s\n",argv[i]);

	// call aapt
	rc = main(argc, argv);

	// stopping the redirection
	fclose (stderr);
	fclose (stdout);

	return rc;
}


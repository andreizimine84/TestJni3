#include <jni.h>
#include <string.h>
#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <sys/un.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <netinet/in.h>
#include <netdb.h>
#include <android/log.h>
#include <arpa/inet.h>
#include <unistd.h>
#include <errno.h>
#include <android/log.h>

#define  LOG_TAG    "NSocket"
#define  LOGD(...)  __android_log_print(ANDROID_LOG_DEBUG,LOG_TAG,__VA_ARGS__)
#define  LOGE(...)  __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)
#define  BUFFER_SIZE 2000

int dataLengthRest = 0;
int count = 0;
extern "C" {
	JNIEXPORT jstring JNICALL Java_com_testjni3_SelectAndShare_connectToHostJNICPP3(JNIEnv * env, jobject obj, jstring fileName);
};

JNIEXPORT jstring JNICALL Java_com_testjni3_SelectAndShare_connectToHostJNICPP3(
		JNIEnv *env, jobject obj, jstring fileName) {
	char *buffer = ( char *) malloc(60);
	char *bufferFin = ( char *) malloc(BUFFER_SIZE);
	if (buffer == NULL)
		return NULL;
	ssize_t bytes_read;
	FILE *fd;
	const char *name = env->GetStringUTFChars(fileName, NULL);
	if ((fd = fopen(name, "r")) == NULL) {
        env->ReleaseStringUTFChars(fileName, name);
		free(buffer);
		free(bufferFin);
		return NULL;
	}
    env->ReleaseStringUTFChars(fileName, name);
	while( fgets (buffer, 60, fd)!=NULL ) {
		size_t len = strlen(buffer);
		int totalSizeRead = len + strlen(bufferFin);
		if (len > 0 && buffer[len-1] == '\n') {
			buffer[--len] = '\0';
		}
		if(totalSizeRead < BUFFER_SIZE)
			strcat(bufferFin, buffer);
		else {
			bufferFin = (char *) realloc(bufferFin, totalSizeRead + BUFFER_SIZE);
		}
	}
	//buffer[totalSizeRead] = '\0';
	//fseek(fd, 0, SEEK_END);
	//int sizeRead = ftell(fd);
	//fseek(fd, 0, SEEK_SET);
	/*int sizeRead = fread(buffer, 1, BUFFER_SIZE - 1, fd);
	int totalSizeRead = sizeRead;
	while (sizeRead == (BUFFER_SIZE - 1)) // expecting 1 element of size BUFFER_SIZE
	{
		LOGD("Reading from file to buffer");

		buffer = (char *) realloc(buffer, totalSizeRead + BUFFER_SIZE);
		if (buffer == NULL)
		{
			free(buffer);
			return NULL;
		}
		totalSizeRead += (sizeRead = fread((buffer + totalSizeRead), 1, BUFFER_SIZE - 1, fd));
	}
	*/

	jstring jstrBuf = env->NewStringUTF(bufferFin);

	fflush(fd);
	fclose(fd);
	free(bufferFin);
	free(buffer);
return jstrBuf;
}

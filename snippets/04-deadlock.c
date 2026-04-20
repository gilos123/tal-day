/*
 * 04-deadlock.c — Lesson 4.
 *
 * Classical AB/BA deadlock. Thread 1 grabs a then b. Thread 2 grabs
 * b then a. With unlucky scheduling, both threads hold one lock and
 * wait forever for the other.
 *
 * This program WILL HANG on most runs. The test harness (make run-04)
 * kills it after 2 seconds and reports progress.
 *
 * Compile: make build/04-deadlock
 * Run:     timeout 2 ./build/04-deadlock || echo "(deadlocked — expected)"
 */
#include <pthread.h>
#include <stdio.h>
#include <unistd.h>

static pthread_mutex_t a = PTHREAD_MUTEX_INITIALIZER;
static pthread_mutex_t b = PTHREAD_MUTEX_INITIALIZER;

static void *t1(void *arg) {
    (void)arg;
    for (;;) {
        pthread_mutex_lock(&a);
        usleep(1);                 /* widen the window */
        pthread_mutex_lock(&b);
        pthread_mutex_unlock(&b);
        pthread_mutex_unlock(&a);
    }
}

static void *t2(void *arg) {
    (void)arg;
    for (;;) {
        pthread_mutex_lock(&b);    /* reversed order */
        usleep(1);
        pthread_mutex_lock(&a);
        pthread_mutex_unlock(&a);
        pthread_mutex_unlock(&b);
    }
}

int main(void) {
    pthread_t x, y;
    pthread_create(&x, NULL, t1, NULL);
    pthread_create(&y, NULL, t2, NULL);
    pthread_join(x, NULL);
    pthread_join(y, NULL);
    return 0;
}

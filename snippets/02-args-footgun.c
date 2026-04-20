/*
 * 02-args-footgun.c — Lesson 2.
 *
 * THE classic beginner bug: sharing one local variable as the
 * argument to every pthread_create. Each thread reads `i` at some
 * unspecified point, and the loop is probably already past that
 * value. Prints garbage — often a run of 4s (value of i after the
 * loop) or a mix.
 *
 * Compile: make build/02-args-footgun
 * Run:     make run-02-args-footgun
 */
#include <pthread.h>
#include <stdio.h>

#define N 4

static void *worker(void *arg) {
    int *p = (int *)arg;
    printf("thread sees id = %d\n", *p);
    return NULL;
}

int main(void) {
    pthread_t ts[N];
    for (int i = 0; i < N; i++) {
        /* BUG: every thread gets the address of the SAME i.
         * Fix: pass an int* into a per-thread slot (see 02-create-join.c). */
        pthread_create(&ts[i], NULL, worker, &i);
    }
    for (int i = 0; i < N; i++) pthread_join(ts[i], NULL);
    return 0;
}

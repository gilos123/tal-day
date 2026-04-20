/*
 * 02-create-join.c — Lesson 2.
 *
 * Spawn N worker threads, each given a unique integer. Collect the
 * per-thread return value via pthread_join's second argument.
 *
 * Compile: make build/02-create-join
 * Run:     make run-02-create-join
 */
#include <pthread.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#define N 4

typedef struct {
    int id;
    int input;
} task_t;

static void *worker(void *arg) {
    task_t *t = (task_t *)arg;
    long result = (long)t->id * t->input;
    printf("[worker %d] input=%d -> result=%ld\n", t->id, t->input, result);
    /* The returned pointer is visible to pthread_join — but only if we
     * return storage that outlives this thread. Casting an integer to
     * void* avoids the malloc-and-leak pattern. */
    return (void *)result;
}

int main(void) {
    pthread_t threads[N];
    task_t tasks[N];

    for (int i = 0; i < N; i++) {
        tasks[i] = (task_t){ .id = i, .input = (i + 1) * 10 };
        int rc = pthread_create(&threads[i], NULL, worker, &tasks[i]);
        if (rc != 0) {
            fprintf(stderr, "pthread_create #%d: %s\n", i, strerror(rc));
            exit(1);
        }
    }

    long total = 0;
    for (int i = 0; i < N; i++) {
        void *ret = NULL;
        pthread_join(threads[i], &ret);  /* join establishes a synchronizes-with edge */
        total += (long)ret;
    }
    printf("sum of returned values = %ld\n", total);
    return 0;
}

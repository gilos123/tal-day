/*
 * 18-threadpool.c — Lesson 18.
 *
 * Minimal mutex+condvar thread pool. N workers pull tasks from a
 * shared FIFO; submit() pushes, shutdown() drains and joins.
 * Not work-stealing — the single queue is a known scalability
 * bottleneck. We fix that in lesson 14 (Chase-Lev).
 *
 * Compile: make build/18-threadpool
 * Run:     make run-18-threadpool
 */
#include <pthread.h>
#include <stdio.h>
#include <stdlib.h>
#include <stdatomic.h>

typedef void (*task_fn)(void *);

typedef struct node {
    task_fn fn;
    void *arg;
    struct node *next;
} node_t;

typedef struct {
    pthread_mutex_t mu;
    pthread_cond_t more;
    node_t *head, *tail;
    int shutdown;
    pthread_t *workers;
    int n_workers;
} pool_t;

static void *worker_loop(void *arg) {
    pool_t *p = (pool_t *)arg;
    for (;;) {
        pthread_mutex_lock(&p->mu);
        while (p->head == NULL && !p->shutdown) {
            pthread_cond_wait(&p->more, &p->mu);
        }
        if (p->head == NULL && p->shutdown) {
            pthread_mutex_unlock(&p->mu);
            return NULL;
        }
        node_t *n = p->head;
        p->head = n->next;
        if (p->head == NULL) p->tail = NULL;
        pthread_mutex_unlock(&p->mu);

        n->fn(n->arg);
        free(n);
    }
}

static void pool_init(pool_t *p, int n_workers) {
    pthread_mutex_init(&p->mu, NULL);
    pthread_cond_init(&p->more, NULL);
    p->head = p->tail = NULL;
    p->shutdown = 0;
    p->n_workers = n_workers;
    p->workers = malloc(sizeof(pthread_t) * n_workers);
    for (int i = 0; i < n_workers; i++) {
        pthread_create(&p->workers[i], NULL, worker_loop, p);
    }
}

static void pool_submit(pool_t *p, task_fn fn, void *arg) {
    node_t *n = malloc(sizeof *n);
    n->fn = fn;
    n->arg = arg;
    n->next = NULL;
    pthread_mutex_lock(&p->mu);
    if (p->tail) p->tail->next = n;
    else         p->head = n;
    p->tail = n;
    pthread_cond_signal(&p->more);
    pthread_mutex_unlock(&p->mu);
}

static void pool_shutdown(pool_t *p) {
    pthread_mutex_lock(&p->mu);
    p->shutdown = 1;
    pthread_cond_broadcast(&p->more);
    pthread_mutex_unlock(&p->mu);
    for (int i = 0; i < p->n_workers; i++) pthread_join(p->workers[i], NULL);
    free(p->workers);
}

/* --- demo --- */
static atomic_long processed = 0;

static void demo_task(void *arg) {
    long id = (long)arg;
    /* cheap synthetic work: sum 0..999 */
    long s = 0;
    for (int i = 0; i < 1000; i++) s += i;
    atomic_fetch_add(&processed, 1);
    if (id < 5 || id % 1000 == 0) {
        printf("[task %4ld] sum=%ld\n", id, s);
    }
}

int main(void) {
    pool_t p;
    pool_init(&p, 4);
    const long N = 10000;
    for (long i = 0; i < N; i++) pool_submit(&p, demo_task, (void *)i);
    pool_shutdown(&p);
    printf("processed %ld tasks across 4 workers\n", atomic_load(&processed));
    return atomic_load(&processed) == N ? 0 : 1;
}

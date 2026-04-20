/*
 * 03-producer-consumer.c — Lesson 3.
 *
 * Bounded FIFO queue shared by one producer and one consumer,
 * synchronized by a mutex + two condition variables. The producer
 * waits on `not_full` when the queue is at capacity; the consumer
 * waits on `not_empty` when the queue is empty.
 *
 * Compile: make build/03-producer-consumer
 * Run:     make run-03-producer-consumer
 */
#include <pthread.h>
#include <stdio.h>
#include <stdlib.h>

#define CAP 8
#define TOTAL 20

typedef struct {
    int buf[CAP];
    int head, tail, count;
    pthread_mutex_t mu;
    pthread_cond_t not_full, not_empty;
} queue_t;

static void q_init(queue_t *q) {
    q->head = q->tail = q->count = 0;
    pthread_mutex_init(&q->mu, NULL);
    pthread_cond_init(&q->not_full, NULL);
    pthread_cond_init(&q->not_empty, NULL);
}

static void q_push(queue_t *q, int v) {
    pthread_mutex_lock(&q->mu);
    while (q->count == CAP) {                     /* predicate loop — spurious wakeups */
        pthread_cond_wait(&q->not_full, &q->mu);
    }
    q->buf[q->tail] = v;
    q->tail = (q->tail + 1) % CAP;
    q->count++;
    pthread_cond_signal(&q->not_empty);           /* wake one waiter */
    pthread_mutex_unlock(&q->mu);
}

static int q_pop(queue_t *q) {
    pthread_mutex_lock(&q->mu);
    while (q->count == 0) {
        pthread_cond_wait(&q->not_empty, &q->mu);
    }
    int v = q->buf[q->head];
    q->head = (q->head + 1) % CAP;
    q->count--;
    pthread_cond_signal(&q->not_full);
    pthread_mutex_unlock(&q->mu);
    return v;
}

static queue_t Q;

static void *producer(void *arg) {
    (void)arg;
    for (int i = 0; i < TOTAL; i++) {
        q_push(&Q, i);
        printf("[producer] pushed %d\n", i);
    }
    return NULL;
}

static void *consumer(void *arg) {
    (void)arg;
    for (int i = 0; i < TOTAL; i++) {
        int v = q_pop(&Q);
        printf("                        [consumer] popped %d\n", v);
    }
    return NULL;
}

int main(void) {
    q_init(&Q);
    pthread_t p, c;
    pthread_create(&p, NULL, producer, NULL);
    pthread_create(&c, NULL, consumer, NULL);
    pthread_join(p, NULL);
    pthread_join(c, NULL);
    return 0;
}

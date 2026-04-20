/*
 * 21-tcpserver.c — Lesson 21.
 *
 * Minimal multi-client TCP echo server. The accept loop runs on the
 * main thread; each accepted connection is handled by a newly spawned
 * detached worker thread. No thread pool — lesson 18 fixes that.
 *
 * Compile: make build/21-tcpserver
 * Run:     ./build/21-tcpserver [port]       (defaults to 7878)
 * Test:    nc localhost 7878                 (type, see lines echoed)
 *          Open a second nc to prove concurrency.
 */
#include <arpa/inet.h>
#include <errno.h>
#include <netinet/in.h>
#include <pthread.h>
#include <signal.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sys/socket.h>
#include <sys/types.h>
#include <unistd.h>

#define BACKLOG 32
#define BUFSZ 1024

static void die(const char *what) {
    perror(what);
    exit(1);
}

static void *handle_client(void *arg) {
    int fd = (int)(long)arg;

    /* Identify the peer for logging */
    struct sockaddr_in peer;
    socklen_t plen = sizeof peer;
    getpeername(fd, (struct sockaddr *)&peer, &plen);
    char ip[INET_ADDRSTRLEN];
    inet_ntop(AF_INET, &peer.sin_addr, ip, sizeof ip);
    printf("[+] %s:%d connected (fd=%d)\n", ip, ntohs(peer.sin_port), fd);

    char buf[BUFSZ];
    ssize_t n;
    while ((n = recv(fd, buf, sizeof buf, 0)) > 0) {
        /* echo back everything we read — robust against short writes */
        ssize_t sent = 0;
        while (sent < n) {
            ssize_t w = send(fd, buf + sent, (size_t)(n - sent), 0);
            if (w < 0) {
                if (errno == EINTR) continue;
                perror("send");
                goto done;
            }
            sent += w;
        }
    }
done:
    printf("[-] %s:%d disconnected\n", ip, ntohs(peer.sin_port));
    close(fd);
    return NULL;
}

int main(int argc, char **argv) {
    /* ignore SIGPIPE — otherwise a client disconnect mid-send kills us */
    signal(SIGPIPE, SIG_IGN);

    unsigned short port = 7878;
    if (argc > 1) port = (unsigned short)atoi(argv[1]);

    int lfd = socket(AF_INET, SOCK_STREAM, 0);
    if (lfd < 0) die("socket");

    int one = 1;
    setsockopt(lfd, SOL_SOCKET, SO_REUSEADDR, &one, sizeof one);

    struct sockaddr_in addr = {
        .sin_family = AF_INET,
        .sin_port = htons(port),
        .sin_addr.s_addr = htonl(INADDR_ANY),
    };
    if (bind(lfd, (struct sockaddr *)&addr, sizeof addr) < 0) die("bind");
    if (listen(lfd, BACKLOG) < 0) die("listen");

    printf("echo server listening on port %u\n", port);

    for (;;) {
        int cfd = accept(lfd, NULL, NULL);
        if (cfd < 0) {
            if (errno == EINTR) continue;
            perror("accept");
            continue;
        }
        pthread_t tid;
        /* detached: we don't join, the thread frees itself on return */
        pthread_attr_t attr;
        pthread_attr_init(&attr);
        pthread_attr_setdetachstate(&attr, PTHREAD_CREATE_DETACHED);
        if (pthread_create(&tid, &attr, handle_client, (void *)(long)cfd) != 0) {
            perror("pthread_create");
            close(cfd);
        }
        pthread_attr_destroy(&attr);
    }
}

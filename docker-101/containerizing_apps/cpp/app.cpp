#include <memory>
#include <signal.h>
#include <cstdint>
#include <iostream>
#include <evhttp.h>

void sighandler(int signal, short events, void *base) {
  std::cerr << "Exiting" << std::endl;
  std::exit(3);
}

int main() {
  using namespace std;
  cout.setf(ios_base::unitbuf);

  cout << "Listening on 0.0.0.0:8000" << endl;

  struct event_base *ev_base = event_init();
  if (!ev_base) {
    cerr << "Failed to init libevent." << endl;
    return -1;
  }

  struct event ev_sigint;
  evsignal_set(&ev_sigint, SIGINT, sighandler, ev_base);
  evsignal_add(&ev_sigint, NULL);

  char const SrvAddress[] = "0.0.0.0";
  uint16_t SrvPort = 8000;
  unique_ptr<evhttp, decltype(&evhttp_free)> Server(evhttp_start(SrvAddress, SrvPort), &evhttp_free);

  if (!Server) {
    cerr << "Failed to init http server." << endl;
    return -1;
  }

  void (*OnReq)(evhttp_request *req, void *) = [] (evhttp_request *req, void *) {
    auto *OutBuf = evhttp_request_get_output_buffer(req);

    if (!OutBuf)
      return;

    cout << "Handling request." << endl;
    evbuffer_add_printf(OutBuf, "<html><body><h1>Hello World!</h1></body></html>\n");

    evhttp_send_reply(req, HTTP_OK, "", OutBuf);
  };

  evhttp_set_gencb(Server.get(), OnReq, nullptr);

  if (event_dispatch() == -1) {
    cerr << "Failed to run messahe loop." << endl;
    return -1;
  }
  return 0;
}

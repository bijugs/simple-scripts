reciprocal: main.o reciprocal.o
	gcc $(CFLAGS) -o reciprocal main.o reciprocal.o

main.o: main.c reciprocal.hpp
	gcc $(CFLAGS) -c main.c

reciprocal.o: reciprocal.cpp reciprocal.hpp
	gcc $(CFLAGS) -c reciprocal.cpp

clean:
	rm -f *.o reciprocal

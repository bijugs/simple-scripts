FROM python:3.7.3

RUN pip3 install flask==1.0.2
RUN mkdir /data

WORKDIR /db

COPY db.py .

ENV FLASK_DEBUG=1
EXPOSE 8000

CMD ["./db.py"]

FROM python:3.7.3

RUN pip3 install flask==1.0.2

WORKDIR /app

COPY app.py .

EXPOSE 8000
ENV PORT=8000

CMD ["./app.py"]

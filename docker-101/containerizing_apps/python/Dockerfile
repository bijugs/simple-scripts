FROM python:3.7.3
RUN apt-get update
RUN apt-get -y install sassc
WORKDIR /myapp
COPY requirements.txt .
RUN pip install -r requirements.txt
COPY . .
RUN sassc styles.scss > static/styles.css
CMD ["python","app.py"]

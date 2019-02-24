# Base image is Python
FROM python

# Copy all the files
COPY . .

# Install all needed requirements
RUN pip install -r requirements.txt

# You need to specify Python file we're running
ENTRYPOINT ["python", $APPLICATION_FILE]

# You need to specify port where application is listening
EXPOSE $APPLICATION_PORT

FROM ghcr.io/sytone/obsidian-remote:latest

RUN sudo apt update && \
    sudo apt install -y python3 python3-pip && \

# Create a directory in the Docker container
RUN mkdir /python

# Copy the contents of the python directory into the new directory
COPY python/ /python/

# Install Python dependencies
RUN pip3 install -r /python/requirements.txt

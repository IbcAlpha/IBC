FROM ubuntu:18.04


WORKDIR /work/setup
RUN apt-get update
RUN DEBIAN_FRONTEND=noninteractive apt-get install -yq \
    wget \
    openjdk-8-jre-headless \
    openjdk-8-jdk-headless \
    ant \
    xserver-xorg-video-dummy \
    socat \
    supervisor

RUN wget -q https://download2.interactivebrokers.com/installers/ibgateway/stable-standalone/ibgateway-stable-standalone-linux-x64.sh
RUN echo 'abe5d3a39e07b2d46d7e0d661c0e8b5e4923c2c3  ibgateway-stable-standalone-linux-x64.sh' > sha1sum.txt
RUN sha1sum -c sha1sum.txt
RUN chmod 755 ibgateway-stable-standalone-linux-x64.sh && yes n | ./ibgateway-stable-standalone-linux-x64.sh

WORKDIR /work/ibc
COPY . .
RUN mkdir lib && cp /usr/local/i4j_jres/1.8.0_152-tzdata2019c/lib/ext/jfxrt.jar lib
RUN IBC_BIN=/root/Jts/ibgateway/972/jars/ ant

WORKDIR /root/ibc
RUN cp /work/ibc/resources/IBC.jar . && cp /work/ibc/resources/scripts/ibcstart.sh . && chmod 755 ibcstart.sh
COPY docker/jts.ini .
COPY config.ini .

WORKDIR /root
# RUN wget -q http://xpra.org/xorg.conf
COPY docker/xorg.conf .
COPY docker/supervisord.conf .

CMD /usr/bin/supervisord -c /root/supervisord.conf

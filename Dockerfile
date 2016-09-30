FROM fedora:23

MAINTAINER Paolo Patierno <ppatierno@live.com>

# install java, gettext (for envsubst)
RUN dnf -y install java-1.8.0-openjdk libaio python gettext

ENV ARTEMIS_VERSION 1.4.0

# download and extract ActiveMQ Artemis
RUN dnf -y install wget
RUN dnf -y install tar
RUN wget https://apache.org/dist/activemq/activemq-artemis/$ARTEMIS_VERSION/apache-artemis-$ARTEMIS_VERSION-bin.tar.gz --no-check-certificate
RUN tar xvfz apache-artemis-$ARTEMIS_VERSION-bin.tar.gz -C /opt

ENV ARTEMIS_HOME=/opt/apache-artemis-$ARTEMIS_VERSION 
ENV PATH=$ARTEMIS_HOME/bin:$PATH

COPY ./utils/run_artemis.sh $ARTEMIS_HOME/bin/
COPY ./config_templates /config_templates

RUN chmod +x $ARTEMIS_HOME/bin/run_artemis.sh

EXPOSE 5672

CMD ["/opt/apache-artemis-1.4.0/bin/run_artemis.sh"]

FROM openjdk:8u141-jre

COPY target/f5-marathon-autoscale.jar /
ADD tools/startup.sh /startup.sh
RUN chmod +x /startup.sh

ENTRYPOINT ["/startup.sh"]
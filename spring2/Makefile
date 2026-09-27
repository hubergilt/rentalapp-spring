# Convenience targets for building the WAR and deploying it to a local
# Tomcat 9 or JBoss EAP 7.4 install (legacy-container build - see README).
# Neither target starts/stops the container itself - do that with the
# container's own scripts.
#
# Override on the command line, e.g.:
#   make deploy-tomcat TOMCAT_HOME=/opt/tomcat/9.0.90
#   make deploy-jboss JBOSS_HOME=/opt/jboss-eap-7.4

TOMCAT_HOME ?= /opt/tomcat/9.0.90
JBOSS_HOME  ?= /opt/jboss-eap-7.4

.PHONY: war deploy-tomcat undeploy-tomcat redeploy-tomcat \
        deploy-jboss undeploy-jboss redeploy-jboss pass

war:
	mvn clean package
	@echo "Built target/rentalapp.war"

deploy-tomcat: war
	cp target/rentalapp.war $(TOMCAT_HOME)/webapps/rentalapp.war
	@echo "Deployed to $(TOMCAT_HOME)/webapps/rentalapp.war"

undeploy-tomcat:
	rm -f $(TOMCAT_HOME)/webapps/rentalapp.war
	rm -rf $(TOMCAT_HOME)/webapps/rentalapp

redeploy-tomcat: undeploy-tomcat deploy-tomcat

deploy-jboss: war
	cp target/rentalapp.war $(JBOSS_HOME)/standalone/deployments/rentalapp.war
	@echo "Deployed to $(JBOSS_HOME)/standalone/deployments/rentalapp.war"
	@echo "JBoss will marker-file deploy it automatically; watch server.log for 'deployed'"

undeploy-jboss:
	rm -f $(JBOSS_HOME)/standalone/deployments/rentalapp.war
	rm -f $(JBOSS_HOME)/standalone/deployments/rentalapp.war.*

redeploy-jboss: undeploy-jboss deploy-jboss

# Generates a BCrypt hash for ADMIN_PASSWORD_HASH. Does NOT boot Spring -
# see HashPasswordTool for why that matters.
# Usage: make pass PASSWD='your-real-password'
pass:
	mvn -q compile exec:java -Dexec.args="$(PASSWD)"

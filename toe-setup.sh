# Expect java to be installed; sudo apt install openjdk-8-jdk

JAVA_HOME=/usr/lib/jvm/java-8-openjdk-amd64
export JAVA_HOME

MAVEN_OPTS="-Xmx1024m"
export MAVEN_OPTS

M2_HOME=/opt/java/apache-maven-3.6.3/
export M2_HOME

export PATH=$JAVA_HOME/bin:$M2_HOME/bin:$PATH

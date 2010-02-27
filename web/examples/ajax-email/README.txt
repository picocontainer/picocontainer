Add profile to your ~/.m2/settings.xml

<settings>
  <profiles>
    <profile>
      <id>datanucleus</id>
      <repositories>
        <repository>
          <id>datanucleus-downloads</id>
          <name>DataNucleus Repository</name>
          <url>http://www.datanucleus.org/downloads/maven2</url>
        </repository>
      </repositories>
      <pluginRepositories>
        <pluginRepository>
          <id>datanucleus-downloads</id>
          <url>http://www.datanucleus.org/downloads/maven2/</url>
        </pluginRepository>
      </pluginRepositories>
    </profile>
  </profiles>
</settings>

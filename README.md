GOAL Grammar Tools
==================

The GOAL grammar, and tools for parsing and semantic analysis of GOAL agent program code.

Build
=====
Run 'mvn package' to generate an executable jar with dependencies.

Usage
=====
Usage: java -jar grammar-1.1.3-SNAPSHOT-jar-with-dependencies.jar < <filename>.goal.

Dependency information 
=====================

```
<repository>
 <id>goalhub-mvn-repo</id>
 <url>https://raw.github.com/goalhub/mvn-repo/master</url>
</repository>
```
	
```	
<dependency>
  <groupId>com.github.goalhub.grammar</groupId>
  <artifactId>languageTools</artifactId>
  <version>1.1.4</version>
</dependency>
```	

Release Procedure
=============

Ensure your ~/.m2/settings.xml file is as follows:

```
<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0"
      xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
      xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0
                          http://maven.apache.org/xsd/settings-1.0.0.xsd">
	<servers>
		<server>
   			<id>github</id>
   			<username>YOUR_USERNAME</username>
   			<password>YOUR_PASSWORD</password>
		</server>
	</servers>
</settings>
```

Then call:

```
mvn versions:use-latest-versions -DallowSnapshots=true -DexcludeReactor=false && mvn deploy -DcreateChecksum=true
```

Note that you must have a public name and e-mail address set on GitHub for this to work correctly (https://github.com/settings/profile)

Link to more information about GOAL
===================================
See also: http://ii.tudelft.nl/trac/goal/.

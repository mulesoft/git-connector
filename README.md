Mule Git Connector
==================

Git is a free and open source, distributed version control system designed to handle everything from small to very large projects with speed and efficiency. This connector will allow to interact with Git repositories.

Installation
------------

The connector can either be installed for all applications running within the Mule instance or can be setup to be used
for a single application.

*All Applications*

Download the connector from the link above and place the resulting jar file in
/lib/user directory of the Mule installation folder.

*Single Application*

To make the connector available only to single application then place it in the
lib directory of the application otherwise if using Maven to compile and deploy
your application the following can be done:

Add the connector's maven repo to your pom.xml:

    <repositories>
        <repository>
            <id>muleforge-releases</id>
            <name>MuleForge Releases Repository</name>
            <url>https://repository.muleforge.org/releases/</url>
            <layout>default</layout>
        </repsitory>
    </repositories>

Add the connector as a dependency to your project. This can be done by adding
the following under the dependencies element in the pom.xml file of the
application:

    <dependency>
        <groupId>org.mule.modules</groupId>
        <artifactId>mule-module-git</artifactId>
        <version>1.4-SNAPSHOT</version>
    </dependency>

Configuration
-------------

You can configure the connector as follows:

    <git:config directory="value"/>

Here is detailed list of all the configuration attributes:

| attribute | description | optional | default value |
|:-----------|:-----------|:---------|:--------------|
|name|Give a name to this configuration so it can be later referenced by config-ref.|yes||
|directory|Directory of your git repository|no|


Clone
-----

Clone a repository into a new directory



    
    <git:clone config-ref="s3repo" uri="git@github.com:mulesoft/s3-connector.git"/>
    

| attribute | description | optional | default value | possible values |
|:-----------|:-----------|:---------|:--------------|:----------------|
|config-ref|Specify which configuration to use for this invocation|yes||
|uri| The (possibly remote) repository to clone from.|no||
|bare| True if you want a bare Git repository, false otherwise.|yes|false|
|remote| Name of the remote to keep track of the upstream repository.|yes|origin|
|branch| Name of the local branch into which the remote will be cloned.|yes|HEAD|
|overrideDirectory| Name of the directory to use for git repository|yes||

Add
---

Add file contents to the index



    
    <git:add config-ref="s3repo" filePattern="README.txt"/>
    

| attribute | description | optional | default value | possible values |
|:-----------|:-----------|:---------|:--------------|:----------------|
|config-ref|Specify which configuration to use for this invocation|yes||
|filePattern| File to add content from. Also a leading directory name (e.g. dir to add dir/file1 and dir/file2) can be given to add all files in the directory, recursively.|no||
|overrideDirectory| Name of the directory to use for git repository|yes||

Create Branch
-------------

Create a local branch



    
    <git:create-branch config-ref="s3repo" name="myexperiment"
    startPoint="bd1c1156a06576f4339af4cb9a5cfddfcc80154e">
    

| attribute | description | optional | default value | possible values |
|:-----------|:-----------|:---------|:--------------|:----------------|
|config-ref|Specify which configuration to use for this invocation|yes||
|name|       Name of the new branch|no||
|force|      If true and the branch with the given name already exists, the start-point of an existing branch will be set to a new start-point; if false, the existing branch will not be changed.|yes|false|
|startPoint| The new branch head will point to this commit. It may be given as a branch name, a commit-id, or a tag. If this option is omitted, the current HEAD will be used instead.|yes|HEAD|
|overrideDirectory| Name of the directory to use for git repository|yes||

Delete Branch
-------------

Delete local branch



    
    <git:delete-branch config-ref="s3repo" name="myexperiment"/>
    

| attribute | description | optional | default value | possible values |
|:-----------|:-----------|:---------|:--------------|:----------------|
|config-ref|Specify which configuration to use for this invocation|yes||
|name|  Name of the branch to delete|no||
|force| If false a check will be performed whether the branch to be deleted is already merged into the current branch and deletion will be refused in this case|no||
|overrideDirectory| Name of the directory to use for git repository|yes||

Commit
------

Record changes to the repository



    
    <git:commit config-ref="s3repo" msg="Updated README.txt" committerName="John Doe"
                committerEmail="john@doe.net">
    

| attribute | description | optional | default value | possible values |
|:-----------|:-----------|:---------|:--------------|:----------------|
|config-ref|Specify which configuration to use for this invocation|yes||
|msg|            Commit message|no||
|committerName|  Name of the person performing this commit|no||
|committerEmail| Email of the person performing this commit|no||
|authorName|     Name of the author of the changes to commit|yes||
|authorEmail|    Email of the author of the changes to commit|yes||
|overrideDirectory| Name of the directory to use for git repository|yes||

Push
----

Update remote refs along with associated objects



    
    <git:push config-ref="s3repo" remote="origin"/>
    

| attribute | description | optional | default value | possible values |
|:-----------|:-----------|:---------|:--------------|:----------------|
|config-ref|Specify which configuration to use for this invocation|yes||
|remote| The remote (uri or name) used for the push operation.|yes|origin|
|force|  Sets the force preference for push operation|yes|false|
|overrideDirectory| Name of the directory to use for git repository|yes||

Pull
----

Fetch from and merge with another repository or a local branch



    
    <git:pull config-ref="s3repo"/>
    

| attribute | description | optional | default value | possible values |
|:-----------|:-----------|:---------|:--------------|:----------------|
|config-ref|Specify which configuration to use for this invocation|yes||
|overrideDirectory| Name of the directory to use for git repository|yes||

Fetch
-----

Fetch changes from another repository 



    
    <git:fetch config-ref="s3repo"/>
    

| attribute | description | optional | default value | possible values |
|:-----------|:-----------|:---------|:--------------|:----------------|
|config-ref|Specify which configuration to use for this invocation|yes||
|overrideDirectory| Name of the directory to use for git repository|yes||

Checkout
--------

Checkout a local branch or create a local branch from a remote branch



or 



    
    <git:checkout config-ref="s3repo" branch="my-topic-branch" startPoint="origin/my-topic-branch"/>
    

| attribute | description | optional | default value | possible values |
|:-----------|:-----------|:---------|:--------------|:----------------|
|config-ref|Specify which configuration to use for this invocation|yes||
|branch| Name of the branch to checkout|no||
|startPoint| If specified creates a new branch pointing to this startPoint|yes||
|overrideDirectory| Name of the directory to use for git repository|yes||
















# class-reloader
> reload java class to a Running JVM

_background_

> in some situation, we found some bug in our running java code, but re-deploy the whole project is too expensive,
> instead we can replace the jar file's entry with new class file
> and let running jvm use the new class
> this tool provide facility to accomplish such work.

# build
mvn package

will produce : target/class-reloader.jar 

# usage
1. copy class-reloader.jar to the target machine running Jvm
2. prepare the compiled class file to some path : `/tmp/Hello.class`
3. running the reload:
         
         # assume the class is : com.world.Hello
         # assume the jvm process id is : 2340
         JAVA_HOME=/usr/local/java $JAVA_HOME/bin/java -cp class-reloader.jar 2340 com.world.Hello /tmp/Hello.class
4. you will see some log indicate the result of success or failure.
5. on the target jvm's stdout, you will see some message indicate the success or failure.
         


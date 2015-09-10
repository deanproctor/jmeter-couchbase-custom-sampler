# Custom JMeter sampler for Couchbase

This is a custom Java sampler class that can be used to benchmark Couchbase.
It was tested against Couchbase Enterprise 4.0.0-rc0.

**Version** 0.3 (alpha)

Originally written by: Alex Bordei, Bigstep  
(alex at bigstep dot com)

Modified by: Jordan Moore, Avalon Consulting, LLC  
(moorej at avalonconsult dot com)

## Dependencies:
* Apache JMeter Sources 2.13 - [Link](http://archive.apache.org/dist/jmeter/source/apache-jmeter-2.13_src.zip)
* Couchbase Java Client 2.2.0 - [Link](http://packages.couchbase.com/clients/java/2.2.0/Couchbase-Java-Client-2.2.0.zip)

## Install

The compilation step only needs ran once, so the JMeter binary may also work.  
If code modifications are made to this sampler, repeat steps 2-4 and recreate the sampler in JMeter.  

1. Compile and build the JMeter source:

        $JMETER_HOME/ant download_jars
        $JMETER_HOME/ant

2. Build the extension:

        mvn clean package

3. Install the extension into `$JMETER_HOME/lib/ext`:

        cp target/couchbasemeter-x.y.z.jar $JMETER_HOME/lib/ext

4. Run JMeter as usual from the newly created bin file:

        sh $JMETER_HOME/bin/jmeter.sh

## How to use

Add a new JMeter Java sampler, use the com.avalonconsult.sampler.CBSampler class.
![Alt text](/img/jmeter1.png?raw=true "Select jmeter custom sampler")

Configure your Couchbase credentials and additional parameters
![Alt text](/img/jmeter2.png?raw=true "Configure jmeter sampler")

The response times of most Couchbase installations are sub-millisecond and thus JMeter by default will only record 0 for sub millisecond samples making any graphing or higher resolution analysis useless. What we did was to implement a nanosecond time counter inside the sampler that is returned as a message on the 5th column of the output csv. We use System.nanoTime() for this.

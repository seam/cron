# Seam Cron

Simplify all of your background code execution using CDI.

## Elegant Scheduling.

Seam Cron is a CDI portable extension which allows you to 
elegantly execute scheduled methods from your CDI project.
Observe:

    public void generateReports(@Observes @Scheduled("4:00") Trigger trigger) {
        // do it
    }

This will cause reports to be generated at 4am every day. The @Scheduled annotation is a CDI qualifier defined by the Seam Cron API.
Instead of `"4:00"` you could use full cron-style syntax (eg: `@Scheduled("0 0 4 ? * *")`)
or you could use a property name (eg: `@Scheduled("offpeak")`), which would then 
be resolved into a schedule using a simple `cron.properties` file at the root of your classpath:

    # cron.properties
    offpeak=4:00

Externalising the schedule is a good idea, but with CDI we can take it a step further by associating the named schedule with a typesafe
CDI qualifier. In this case, we could introduce a custom qualifier `@Offpeak` like so:

    @Scheduled("offpeak")
    @Qualifier
    @Retention( RUNTIME )
    @Target( { PARAMETER })
    public @interface Offpeak
    {
    }

Now we can refer to the schedule in a typesafe way throughout our codebase:

    public void generateReports(@Observes @Offpeak Trigger trigger) {
        // do it
    }

    public void emailInvoices(@Observes @Offpeak Trigger trigger) {
        // do it
    }

If your requirements are fairly simple, for example running a task every hour, 
then you can use the special `@Every` qualifier like so:

    public void clockChimes(@Observes @Every(HOUR) Trigger t) { 
        int chimes = t.getValue() % 12;
        if (chimes == 0) { chimes = 12; }
        for (int i=0; i<chimes; i++) {
            bellTower.getRope().pull();
        }
    }

Note that the Trigger instance provides details of the interval for which it was fired - in this case the specific hour.

## Slick Asynchronous Method Invocation

Check it out:

    @Inject @LoggedIn User user;

    public String requestReceipt() {
        generateReceiptForUser(user);
        return "Generating your receipt...";
    }

    @Asynchronous
    public Receipt generateReceiptForUser(user) {
        // heavy lifting
        // ...
        return receipt;
    }

Note that the asynchronous method 'generateReceiptForUser(...)' returns an instance of `Receipt`. Once the method returns, the result will be fired as a CDI 
event. That way you can perform further processing on the result by observing events according to the method return type, like so:

    public void notifyUserOfNewReceipt(@Observes Receipt receipt, @LoggedIn User user) {
        notificationService.send("New receipt available: " + receipt.getId(), user);
    }

The rules concerning return types of @Asynchronous methods are as follows:

* If the method return type is void, no event will be fired
* If the method invocation returns a value of null, no event will be fired. Be careful of this!

You would typically want one dedicated return type per asynchronous method invocation
for a one-to-one mapping between methods and their observers, but there may be use
cases for having multiple asynchronous methods all reporting their results to a single
observer, and Cron supports that use-case as well. You could also introduce some 
additional CDI qualifiers into the mix for some really convenient, event-driven processing:

    @Asynchronous @Credit
    public Balance addCredit(int dollars) {
        ...
        return new Ballance();
    }

    @Asynchronous @Debit
    public Balance addDebit(int dollars) {
        ...
        return new Ballance();
    }

    /**
     * Always report the new balance, for both debits and credits.
     */
    public void reportNewBalance(@Observes Balance balance) {
        log.report(balance.amount());
    }

    /**
     * Track spending habits by listening only to debits.
     */
    public void trackSpending(@Observes @Debit Balance balance) {
        db.saveDebit(balance);
    }

You also have the option of specifying a return type of Future<T> and using the `AsyncResult` helper to return the result
of your method call. Seam Cron will automatically wrap this in a useful Future<T> implementation
which the calling code can use as expected, immediately.

    @Asynchronous
    public Future<Box> doSomeHeavyLiftingInTheBackground() {
        ...
        return new AsyncResult(new Box());
    }

And the calling code:

    @Inject LiftingBean liftingBean;

    public void someMethod() {
        Future<Box> future = liftingBean.doSomeHeavyLiftingInTheBackground();
        // blocks until async method returns or gives up
        Box result = future.get(10, SECONDS);
    }

## Scheduling in Java EE

Since Java EE has its own scheduling API in the form of TimerService, Seam Cron provides a simple implementation
which utilises TimerService, while still providing elegant observer based configuration.

## Scheduling in a Java EE HA Cluster

If you deploy an application containing the Seam Cron TimerService provider to a HA cluster, it will detect the availability of HA services
and deploy as a HA Singleton automatically. That way each scheduled observer method will only fire on a single server in the cluster, 
as opposed to firing on all server instances at once. For this to work there are two extra configuration steps, depending on your 
application server vendor:

JBoss AS/EAP:
* Specify your deployment's name in `cron.properties` (at the root of your classpath) as `ha.singleton.module.name`. For example if your war is called business-app.war, your entry would look like `ha.singleton.module.name=business-app`. 
* Add the following JBoss modules to your deployment's dependencies by adding them to `jboss-deployment-structure.xml` in your WEB-INF directory. For example:
```xml
    <jboss-deployment-structure>
        <deployment>
            <dependencies>
                <module name="org.jboss.msc" />
                <module name="org.jboss.as.clustering.singleton" />
            </dependencies>
        </deployment>
    </jboss-deployment-structure>
```
* Note that HA Singleton mode will only be activated when using the standalone-ha.xml or standalone-full-ha.xml server configurations. 
Otherwise it will default to the non-HA behavior of executing each job on all available nodes at once. To force HA mode set 
"ha.singleton.mode=mandatory" as a system property or in your cron.properties file, which will cause the deployment to fail if the
HA singleton timer service fails to come online within 5 seconds of deployment.

Note: This is only supported in JBoss AS/EAP at the moment.
On other application servers each scheduled observer method will be executed on all server instances at the same time.
If you are interested in support for HA cluster deployment on other application servers please get in touch and/or submit a pull request.

## Quick Start

To use Seam Cron in your Maven project, include the following dependencies in your pom:
```xml

        <dependency>
            <groupId>org.jboss.seam.cron</groupId>
            <artifactId>seam-cron-api</artifactId>
            <version>3.1.1-SNAPSHOT</version>
            <scope>compile</scope>
        </dependency>
        <!-- For scheduled jobs. Choose between Quartz, Queuej and TimerService providers. The TimerService providers are recommended for EE environments. -->
        <dependency>
            <groupId>org.jboss.seam.cron</groupId>
            <artifactId>seam-cron-scheduling-{quartz/queuej/timerservice/timerservice-jboss-ha-singleton}</artifactId>
            <version>3.1.1-SNAPSHOT</version>
            <scope>runtime</scope>
        </dependency>
        <!-- For asynchronous method execution. Choose between Quartz, Queuej and Java threads providers. -->
        <dependency>
            <groupId>org.jboss.seam.cron</groupId>
            <artifactId>seam-cron-asynchronous-{quartz/queuej/threads}</artifactId>
            <version>3.1.1-SNAPSHOT</version>
            <scope>runtime</scope>
        </dependency>
```
## Seam Cron is good, but not great.

It's true. But you can help. If you know exactly what you need and have 
the skillpower to get it done, then please fork this project and submit a pull 
request. Alternatively submit a feature request or bug report on the GitHub
issues page.

## Building From Source:

### Prerequisites: 

 * JDK 5 or above
 * Maven 3 build tool
 * Git version control system

### Method:

    git clone git://github.com/seam/cron.git
    cd cron
    mvn clean install
or
    mvn clean install -s settings.xml

The above commands will build and install Cron into your local Maven repository. 
If you want to run a nifty little example swing app use the following `mvn` command: 

    mvn install -Drun -Dswing-example

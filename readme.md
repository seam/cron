# Seam Cron

## Quick Start

To use Seam Cron in your Maven project, include the following dependencies in your pom:

        <dependency>
            <groupId>org.jboss.seam.cron</groupId>
            <artifactId>seam-cron-api</artifactId>
            <version>3.0.0.Alpha1</version>
            <scope>compile</scope>
        </dependency>
        <dependency>
            <groupId>org.jboss.seam.cron</groupId>
            <artifactId>seam-cron-scheduling-quartz</artifactId>
            <version>3.0.0.Alpha1</version>
            <scope>runtime</scope>
        </dependency>
        <dependency>
            <groupId>org.jboss.seam.cron</groupId>
            <artifactId>seam-cron-asynchronous-quartz</artifactId>
            <version>3.0.0.Alpha1</version>
            <scope>runtime</scope>
        </dependency>

## What is Seam Cron?

Seam Cron is a CDI portable extension which allows you to 
elegantly execute scheduled and asynchronous methods from your CDI project.
Here's a glimpse of what's possible:

    public void howlAtTheMoon(@Observes @AtMidnight CronEvent event) {
        wolf.howl();
    }

`@AtMidnight` is a CDI-style custom qualifier which might look a little like this:

    @Scheduled("00:00")
    @Qualifier
    @Retention( RUNTIME )
    @Target( { PARAMETER })
    public @interface AtMidnight
    {
    }

Instead of `"00:00"` you could use full cron-style syntax (eg: `@Scheduled("0 0 0 ? * *")`)
or you could use an arbitrary name (eg: `@Scheduled("at.midnight")`), which would then 
be resolved into a time using the `cron.properties` file at the root of your classpath:

    # cron.properties
    at.midnight=00:00

Alternatively you could just put the schedule definition directly into the `@Scheduled` 
annotation on the method to be scheduled, but that would be a rather masochistic thing to do.

If your requirements are fairly simple, for example running a task repeatedly at 
a specific interval, then you can use the `@Every` qualifier like so:

    public void clockChimes(@Observes @Every(HOUR) Trigger t) { 
        int chimes = t.getValue() % 12;
        if (chimes == 0) { chimes = 12; }
        for (int i=0; i<chimes; i++) {
            bellTower.getRope().pull();
        }
    }

## MEH. What else you got?

You're kidding right?

OK well, there's also this:

    @Inject @HumanSeeking Missile missile;

    public String destroyAllHumans() {
        initiateRatherDrawnOutMissileLaunchSequence();
        return "Those humans be good as dead";
    }

    @Asynchronous
    public MissileDeployment initiateRatherDrawnOutMissileLaunchSequence() {
        return missile.launchViaSOAPWebServicesDeployedOnAPentiumIIRunningWindowsNTAndNortonAntiVirus();
    }

OK, so that asynchronous method returns an instance of `MissileDeployment`. 
So how do you get your hands on it? Easy!

    public void verifyDeployment(@Observes MissileDeployment deployment) {
        if ("EPIC FAIL".equals(deployment.getStatus())) {
            henchmen.head().fire();
        } else {
            champagne.pop();
        }
    }

The rules concerning return types of @Asynchronous methods are as follows:

* If method return type is void, no event will be fired
* If the method invocation returns a value of null, no event will be fired. Be careful of this!

You would typically want one dedicated return type per asynchronous method invocation
for a one-to-one mapping between methods and their observers, but there may be use
cases for having multiple asynchronous methods all reporting their results to a single
observer, and Cron would be totally cool with that. Alternatively you might wish
to introduce some additional CDI-style qualifiers like so:

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

    public void reportNewBalance(@Observes Balance balance) {
        log.report(balance.amount());
    }

    public void trackSpending(@Observes @Debit Balance balance) {
        db.saveSomething();
    }

Finally, if you prefer a more traditional, EJB-esque approach then you can specify
a return type of Future<T> and use the `AsyncResult` helper to return the result
of your method call. Seam Cron will automatically wrap this in a legit Future<T>
which the calling code can use as expected immediately.

    @Asynchronous
    public Future<Box> doSomeHeavyLiftingInTheBackground() {
        ...
        return new AsyncResult(new Box());
    }

And the calling code:

    @Inject LiftingBean liftingBean;

    public void someMethod() {
        Future<Box> future = liftingBean.doSomeHeavyLiftingInTheBackground();
        // blocks until asynch method returns or gives up
        Box result = future.get(10, SECONDS);
    }

## This is awesome but not awesome enough yet.

I know, it's true. But you can help. If you know exactly what you need and have 
the skillpower to get it done, then please fork this project and submit a pull 
request. Alternatively submit a feature request or bug report over at JIRA:
https://issues.jboss.org/browse/SEAMCRON

## Building From Source:

### Prerequisites: 

 * JDK 5 or above
 * Maven 3 build tool
 * Git version control system

### Method:

    git clone git://github.com/seam/cron.git
    cd cron
    mvn clean install

The above commands will build and install Cron into your local Maven repository. 
If you want to run a nifty little example swing app use the following `mvn` command: 

    mvn install -Drun -Dswing-example

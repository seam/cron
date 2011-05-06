# Seam Cron

## Huh??

Glad you asked. Seam Cron is a CDI portable extension which allows you to 
elegantly execute scheduled and asynchronous methods from your CDI project.
Here's a glimpse of what's possible:

public void howlAtTheMoon(@Observes @AtMidnight CronEvent event) {
    wolf.howl();
}

@AtMidnight is a CDI-style custom qualifier which might look a little like this:

@Scheduled("00:00")
@Qualifier
@Retention( RUNTIME )
@Target( { PARAMETER })
public @interface AtMidnight
{
}

Instead of "00:00" you could instead use full cron-style syntax (eg: @Scheduled("0 0 0 ? * *"))
or you could use an arbitrary name (eg: @Scheduled("at.midnight")), which would then 
be resolved into a time using the scheduler.properties file at the root of your classpath:

at.midnight=00:00

Alternatively you could just put the schedule definition directly into the @Scheduled 
annotation, but that would be a rather masochistic thing to do.

## MEH. What else you got?

You're kidding right?

OK well, there's also this:

@Inject @HumanSeeking Missile missile;

public String destroyAllHumans() {
    initiateRatherDrawnOutMissileLaunchSequence();
    return "Those humans be good as dead";
}

@Asynchronous
public void initiateRatherDrawnOutMissileLaunchSequence() {
    missile.launchViaSOAPWebServicesDeployedOnAPentiumIIRunningWindowsNTAndNortonAntiVirus();
}

## ENOUGH!! How do get it?

Well it's alpha software so it's not available in any Maven repository just yet.
I know I know, it's a crime. But you can install it into your local maven repository in just
3 easy steps:

git clone git://github.com/seam/cron.git
cd cron
mvn clean install -Popenwebbeans-embedded-1

## OPEN WEB BEANS!?!!?

Yes well. There's a bit of a bug in Weld at the moment (WELD-862) which stops 
@Asynchronous from working properly. You're welcome to go ahead and use Seam Cron
in your project and all the scheduling stuff will work just tickety boo. But if
you're hoping to use @Asynchronous and you're deploying to JBoss AS or Glassfish
you're gonna be out of luck. In that case you'd better head straight over to 
https://issues.jboss.org/browse/WELD-862 and moan loudly until it gets sorted out.

## This is awesome but not awesome enough yet.

I know, it's true. But you can help. If you know exactly what you need and have 
the skillpower to get it done, then please fork this project and submit a pull 
request. Alternatively submit a feature request or bug report over at JIRA:
https://issues.jboss.org/browse/SEAMCRON
